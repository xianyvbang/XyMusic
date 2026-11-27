package cn.xybbz

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import java.awt.EventQueue
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import kotlinx.coroutines.runBlocking
import xymusic.composeapp.generated.resources.Res

/**
 * 桌面端系统托盘图标。
 *
 * 在“关闭窗口最小化到托盘”开启时提供托盘入口，
 * 通过菜单恢复主窗口或真正退出应用。
 */
internal object DesktopTray {

    /** 托盘图标像素尺寸，Windows 会按任务栏缩放比例自动适配。 */
    private const val TRAY_ICON_SIZE = 32

    /** 弹出菜单的防抖间隔（毫秒），避免按下与释放都触发弹窗时重复弹出。 */
    private const val MENU_DEBOUNCE_MILLIS = 300L

    /** 菜单命令 ID：显示主界面。 */
    private const val COMMAND_SHOW_WINDOW = 1

    /** 菜单命令 ID：退出。 */
    private const val COMMAND_EXIT = 2

    /** Win32 菜单项标志：普通文本项。 */
    private const val MF_STRING = 0x0000

    /** Win32 菜单项标志：分隔线。 */
    private const val MF_SEPARATOR = 0x0800

    /** Win32 弹出菜单标志：返回被选中的命令 ID 而不是发送 WM_COMMAND。 */
    private const val TPM_RETURNCMD = 0x0100

    /** Win32 弹出菜单标志：允许右键选择。 */
    private const val TPM_RIGHTBUTTON = 0x0002

    /** Win32 弹出菜单标志：菜单底边与鼠标对齐（向上展开）。 */
    private const val TPM_BOTTOMALIGN = 0x0020

    /** Win32 弹出菜单标志：菜单右边与鼠标对齐（向左展开）。 */
    private const val TPM_RIGHTALIGN = 0x0008

    /** Win32 窗口样式：无边框弹出窗口。 */
    private const val WS_POPUP = 0x80000000.toInt()

    /** Win32 显示窗口方式：显示但不激活。 */
    private const val SW_SHOWNOACTIVATE = 4

    /** Win32 空消息，用于菜单关闭后让宿主窗口恢复消息循环。 */
    private const val WM_NULL = 0x0000

    /** 上次弹出菜单的时间戳，用于屏蔽重复的弹出触发。 */
    private var lastMenuShownAt = 0L

    /**
     * 托盘菜单专用线程。
     *
     * TrackPopupMenu 会阻塞调用线程直到菜单关闭，不能占用 AWT 事件线程，
     * 否则菜单打开期间整个应用界面都会被冻结。
     */
    private val menuExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "tray-menu").apply { isDaemon = true }
    }

    /** user32 动态库代理，延迟加载，避免未使用托盘时也加载本地库。 */
    private val user32: User32Library by lazy { Native.load("user32", User32Library::class.java) }

    /** 当前系统是否支持系统托盘。 */
    fun isSupported(): Boolean = SystemTray.isSupported()

    /**
     * 安装托盘图标。
     *
     * @param onShowWindow 点击“显示主界面”或双击托盘图标时回调。
     * @param onExit 点击“退出”时回调。
     * @return 安装成功返回托盘图标对象，失败返回 null。
     */
    fun install(onShowWindow: () -> Unit, onExit: () -> Unit): TrayIcon? {
        if (!SystemTray.isSupported()) {
            return null
        }
        return runCatching {
            val trayIcon = TrayIcon(createTrayImage(), "XyMusic").apply {
                isImageAutoSize = true
                addMouseListener(object : MouseAdapter() {
                    override fun mousePressed(event: MouseEvent) {
                        // 部分环境在按下时就标记为弹出菜单触发。
                        if (event.isPopupTrigger) showMenuSafely(onShowWindow, onExit)
                    }

                    override fun mouseReleased(event: MouseEvent) {
                        // Windows 托盘右键菜单在释放时触发。
                        if (event.isPopupTrigger) showMenuSafely(onShowWindow, onExit)
                    }

                    override fun mouseClicked(event: MouseEvent) {
                        // 双击托盘图标恢复主窗口，符合 Windows 托盘常见交互。
                        if (event.clickCount >= 2) {
                            onShowWindow()
                        }
                    }
                })
            }
            SystemTray.getSystemTray().add(trayIcon)
            trayIcon
        }.getOrNull()
    }

    /** 从系统托盘移除图标。 */
    fun dispose(trayIcon: TrayIcon?) {
        if (trayIcon == null) return
        runCatching { SystemTray.getSystemTray().remove(trayIcon) }
    }

    /** 带防抖地弹出托盘菜单，避免同一次右键重复弹出多个菜单。 */
    private fun showMenuSafely(onShowWindow: () -> Unit, onExit: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastMenuShownAt < MENU_DEBOUNCE_MILLIS) {
            return
        }
        lastMenuShownAt = now
        menuExecutor.execute {
            val command = runCatching { showNativeMenu() }.getOrDefault(0)
            // 菜单回调统一回到 AWT 事件线程，保证窗口操作线程安全。
            when (command) {
                COMMAND_SHOW_WINDOW -> EventQueue.invokeLater(onShowWindow)
                COMMAND_EXIT -> EventQueue.invokeLater(onExit)
            }
        }
    }

    /**
     * 弹出 Windows 原生托盘菜单。
     *
     * 这里直接调用 Win32 的 TrackPopupMenu，菜单完全由系统绘制与管理。
     * 不使用 AWT 的 PopupMenu：本机 JDK 21 + Windows 11 下它的中文会显示成方块，
     * 实测设置字体、切换编码都无效；直接走 Win32 则中文显示正常。
     *
     * @return 被选中的命令 ID，菜单被取消时返回 0。
     */
    private fun showNativeMenu(): Int {
        // 使用系统自带的 STATIC 窗口类创建 1x1 宿主窗口，避免注册自定义窗口类。
        val owner = user32.CreateWindowExW(
            0,
            WString("STATIC"),
            WString(""),
            WS_POPUP,
            0,
            0,
            1,
            1,
            null,
            null,
            null,
            null,
        ) ?: return 0
        try {
            user32.ShowWindow(owner, SW_SHOWNOACTIVATE)
            // 让宿主窗口成为前台窗口，保证点击菜单外部时菜单能正常关闭。
            user32.SetForegroundWindow(owner)
            return trackPopupMenu(owner)
        } finally {
            user32.DestroyWindow(owner)
        }
    }

    /** 创建并跟踪弹出菜单，返回被选中的命令 ID。 */
    private fun trackPopupMenu(owner: Pointer): Int {
        val menu = user32.CreatePopupMenu() ?: return 0
        try {
            user32.AppendMenuW(menu, MF_STRING, COMMAND_SHOW_WINDOW.toLong(), WString("显示主界面"))
            user32.AppendMenuW(menu, MF_SEPARATOR, 0L, null)
            user32.AppendMenuW(menu, MF_STRING, COMMAND_EXIT.toLong(), WString("退出"))

            // 使用 Win32 光标坐标（物理像素），避免高分屏下 AWT 逻辑坐标与系统坐标不一致。
            val cursor = Memory(8)
            if (!user32.GetCursorPos(cursor)) return 0
            val command = user32.TrackPopupMenu(
                menu,
                TPM_RETURNCMD or TPM_RIGHTBUTTON or TPM_BOTTOMALIGN or TPM_RIGHTALIGN,
                cursor.getInt(0),
                cursor.getInt(4),
                0,
                owner,
                null,
            )
            // 菜单关闭后补发空消息，避免菜单残留在前台窗口上。
            user32.PostMessageW(owner, WM_NULL, 0L, 0L)
            return command
        } finally {
            user32.DestroyMenu(menu)
        }
    }

    /**
     * 使用应用 logo 生成托盘图标。
     *
     * logo 自带白色背景，这里裁剪成圆角，保证在深浅色任务栏下都清晰可见。
     */
    private fun createTrayImage(): BufferedImage {
        val logo = loadLogoImage()
        val image = BufferedImage(TRAY_ICON_SIZE, TRAY_ICON_SIZE, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            )
            val radius = TRAY_ICON_SIZE * 0.28f
            graphics.clip = RoundRectangle2D.Float(
                0f,
                0f,
                TRAY_ICON_SIZE.toFloat(),
                TRAY_ICON_SIZE.toFloat(),
                radius,
                radius,
            )
            graphics.drawImage(logo, 0, 0, TRAY_ICON_SIZE, TRAY_ICON_SIZE, null)
        } finally {
            graphics.dispose()
        }
        return image
    }

    /** 从 compose 资源中读取打包的应用 logo。 */
    private fun loadLogoImage(): BufferedImage {
        val bytes = runBlocking { Res.readBytes("drawable/logo_new.png") }
        return requireNotNull(ImageIO.read(ByteArrayInputStream(bytes))) { "无法解析托盘图标资源" }
    }

    /** 托盘菜单需要用到的 user32 函数，W 结尾的接口一律使用宽字符（UTF-16）传参。 */
    internal interface User32Library : Library {
        /** 创建窗口。 */
        fun CreateWindowExW(
            dwExStyle: Int,
            lpClassName: WString,
            lpWindowName: WString,
            dwStyle: Int,
            x: Int,
            y: Int,
            nWidth: Int,
            nHeight: Int,
            hWndParent: Pointer?,
            hMenu: Pointer?,
            hInstance: Pointer?,
            lpParam: Pointer?,
        ): Pointer?

        /** 显示窗口。 */
        fun ShowWindow(hWnd: Pointer, nCmdShow: Int): Boolean

        /** 把窗口设为前台窗口。 */
        fun SetForegroundWindow(hWnd: Pointer): Boolean

        /** 创建空白的弹出菜单。 */
        fun CreatePopupMenu(): Pointer?

        /** 向弹出菜单追加菜单项。 */
        fun AppendMenuW(hMenu: Pointer, uFlags: Int, uIDNewItem: Long, lpNewItem: WString?): Boolean

        /** 在指定坐标弹出菜单并跟踪选择结果。 */
        fun TrackPopupMenu(
            hMenu: Pointer,
            uFlags: Int,
            x: Int,
            y: Int,
            nReserved: Int,
            hWnd: Pointer?,
            prcRect: Pointer?,
        ): Int

        /** 销毁菜单。 */
        fun DestroyMenu(hMenu: Pointer): Boolean

        /** 销毁窗口。 */
        fun DestroyWindow(hWnd: Pointer): Boolean

        /** 向窗口投递消息。 */
        fun PostMessageW(hWnd: Pointer, msg: Int, wParam: Long, lParam: Long): Boolean

        /** 获取光标的屏幕物理像素坐标，内存布局为两个 int（x、y）。 */
        fun GetCursorPos(point: Pointer): Boolean
    }
}
