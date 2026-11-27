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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
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

    /** 菜单线程等待请求的轮询间隔（毫秒），期间持续处理宿主窗口消息。 */
    private const val MENU_POLL_MILLIS = 20L

    /** 存放 Win32 MSG 结构的缓冲区大小（字节），足够覆盖 32/64 位结构体。 */
    private const val MESSAGE_BUFFER_SIZE = 64L

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

    /** Win32 空消息，用于菜单关闭后让宿主窗口退出菜单模式。 */
    private const val WM_NULL = 0x0000

    /** Win32 消息取出方式：取出后从队列移除。 */
    private const val PM_REMOVE = 0x0001

    /** 待处理的托盘菜单请求。 */
    private val menuRequests = LinkedBlockingQueue<MenuRequest>()

    /** 上次弹出菜单的时间戳，用于屏蔽重复的弹出触发。 */
    private var lastMenuShownAt = 0L

    /** 菜单线程是否已经启动。 */
    @Volatile
    private var isMenuThreadStarted = false

    /** user32 动态库代理，延迟加载，避免未使用托盘时也加载本地库。 */
    private val user32: User32Library by lazy { Native.load("user32", User32Library::class.java) }

    /** kernel32 动态库代理，用于获取线程 ID。 */
    private val kernel32: Kernel32Library by lazy { Native.load("kernel32", Kernel32Library::class.java) }

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

    /** 带防抖地把菜单请求投递给菜单线程。 */
    private fun showMenuSafely(onShowWindow: () -> Unit, onExit: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastMenuShownAt < MENU_DEBOUNCE_MILLIS) {
            return
        }
        lastMenuShownAt = now
        ensureMenuThreadStarted()
        menuRequests.offer(MenuRequest(onShowWindow, onExit))
    }

    /** 保证菜单线程已经启动。 */
    private fun ensureMenuThreadStarted() {
        if (isMenuThreadStarted) return
        synchronized(this) {
            if (isMenuThreadStarted) return
            isMenuThreadStarted = true
            Thread({ menuLoop() }, "tray-menu").apply { isDaemon = true }.start()
        }
    }

    /**
     * 菜单线程主循环。
     *
     * 宿主窗口常驻并持续处理消息：TrackPopupMenu 要求调用线程拥有前台窗口和可用的消息队列，
     * 如果每次弹完就销毁窗口、线程也不处理消息，第二次弹出的菜单会立刻被取消。
     */
    private fun menuLoop() {
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
        ) ?: run {
            isMenuThreadStarted = false
            return
        }
        try {
            user32.ShowWindow(owner, SW_SHOWNOACTIVATE)
            val message = Memory(MESSAGE_BUFFER_SIZE)
            while (true) {
                // 等待请求的同时处理窗口消息，保证宿主窗口的消息循环一直可用。
                val request = try {
                    menuRequests.poll(MENU_POLL_MILLIS, TimeUnit.MILLISECONDS)
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
                pumpMessages(message)
                if (request == null) continue
                // 先清空上一次菜单可能残留的消息，避免新菜单被立刻取消。
                pumpMessages(message)
                val command = runCatching { showNativeMenu(owner) }.getOrDefault(0)
                // 菜单回调统一回到 AWT 事件线程，保证窗口操作线程安全。
                when (command) {
                    COMMAND_SHOW_WINDOW -> EventQueue.invokeLater(request.onShowWindow)
                    COMMAND_EXIT -> EventQueue.invokeLater(request.onExit)
                }
            }
        } finally {
            user32.DestroyWindow(owner)
        }
    }

    /** 处理线程消息队列中的全部消息，避免宿主窗口消息堆积。 */
    private fun pumpMessages(message: Pointer) {
        while (user32.PeekMessageW(message, null, 0, 0, PM_REMOVE)) {
            user32.TranslateMessage(message)
            user32.DispatchMessageW(message)
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
    private fun showNativeMenu(owner: Pointer): Int {
        bringToForeground(owner)
        val command = trackPopupMenu(owner)
        // 菜单关闭后补发空消息，让宿主窗口退出菜单模式。
        user32.PostMessageW(owner, WM_NULL, 0L, 0L)
        return command
    }

    /**
     * 把宿主窗口设为前台窗口。
     *
     * Windows 的前台锁会让 SetForegroundWindow 直接失败，此时临时附加到当前前台线程
     * 再设置一次，保证后续 TrackPopupMenu 能正常弹出并把焦点交给菜单。
     */
    private fun bringToForeground(owner: Pointer) {
        if (user32.SetForegroundWindow(owner)) return
        val foregroundWindow = user32.GetForegroundWindow() ?: return
        val foregroundThreadId = user32.GetWindowThreadProcessId(foregroundWindow, null)
        val currentThreadId = kernel32.GetCurrentThreadId()
        if (foregroundThreadId == 0 || foregroundThreadId == currentThreadId) return
        if (user32.AttachThreadInput(foregroundThreadId, currentThreadId, true)) {
            user32.SetForegroundWindow(owner)
            user32.AttachThreadInput(foregroundThreadId, currentThreadId, false)
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
            return user32.TrackPopupMenu(
                menu,
                TPM_RETURNCMD or TPM_RIGHTBUTTON or TPM_BOTTOMALIGN or TPM_RIGHTALIGN,
                cursor.getInt(0),
                cursor.getInt(4),
                0,
                owner,
                null,
            )
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

    /** 一次托盘菜单请求，携带菜单选中后要执行的回调。 */
    private class MenuRequest(
        /** 恢复主窗口回调。 */
        val onShowWindow: () -> Unit,
        /** 退出应用回调。 */
        val onExit: () -> Unit,
    )

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

        /** 获取当前前台窗口。 */
        fun GetForegroundWindow(): Pointer?

        /** 获取窗口所属线程 ID。 */
        fun GetWindowThreadProcessId(hWnd: Pointer, processId: Pointer?): Int

        /** 附加或解除线程输入状态。 */
        fun AttachThreadInput(idAttach: Int, idAttachTo: Int, attach: Boolean): Boolean

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

        /** 从消息队列取出消息。 */
        fun PeekMessageW(msg: Pointer, hWnd: Pointer?, msgFilterMin: Int, msgFilterMax: Int, removeMsg: Int): Boolean

        /** 翻译消息。 */
        fun TranslateMessage(msg: Pointer): Boolean

        /** 分发消息。 */
        fun DispatchMessageW(msg: Pointer): Long

        /** 获取光标的屏幕物理像素坐标，内存布局为两个 int（x、y）。 */
        fun GetCursorPos(point: Pointer): Boolean
    }

    /** 托盘菜单需要用到的 kernel32 函数。 */
    internal interface Kernel32Library : Library {
        /** 获取当前线程 ID。 */
        fun GetCurrentThreadId(): Int
    }
}
