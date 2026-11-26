package cn.xybbz

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JWindow
import javax.swing.UIManager
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
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

    /** 系统外观没有提供菜单字体时使用的兜底字号。 */
    private const val DEFAULT_MENU_FONT_SIZE = 12

    /** 弹出菜单的防抖间隔（毫秒），避免按下与释放都触发弹窗时重复弹出。 */
    private const val MENU_DEBOUNCE_MILLIS = 300L

    /** 系统中文字体候选列表，按优先级取实际安装的第一个。 */
    private val CHINESE_FONT_CANDIDATES = listOf(
        "Microsoft YaHei UI",
        "Microsoft YaHei",
        "SimHei",
        "SimSun",
        "DengXian",
        "Noto Sans CJK SC",
    )

    /** 上次弹出菜单的时间戳，用于屏蔽重复的弹出触发。 */
    private var lastMenuShownAt = 0L

    /** 是否已经切换到系统外观，避免重复设置外观与字体。 */
    private var isSystemLookAndFeelInstalled = false

    /** 菜单统一使用的字体，初始化系统外观时确定。 */
    private var menuFont: Font? = null

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
                        // 部分平台在按下时就标记为弹出菜单触发。
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
        showMenu(onShowWindow, onExit)
    }

    /**
     * 在鼠标位置弹出托盘菜单。
     *
     * 不使用 AWT 原生 [java.awt.PopupMenu]：Windows 11 + JDK 21 的原生菜单无法渲染中文，
     * 中文会显示成方块（口口口），实测设置菜单字体、切换 GBK 编码都无效。
     * 这里使用系统外观（Windows 下即 Windows L&F）的 Swing 菜单，
     * 外观、配色、交互与系统右键菜单一致，同时中文可以正常显示。
     */
    private fun showMenu(onShowWindow: () -> Unit, onExit: () -> Unit) {
        val pointer = MouseInfo.getPointerInfo()?.location ?: return
        installSystemLookAndFeel()

        val menu = JPopupMenu().apply {
            // 强制使用重量级弹窗，否则会被 1x1 的透明宿主窗口裁剪。
            isLightWeightPopupEnabled = false
        }
        menu.add(createMenuItem("显示主界面") { onShowWindow() })
        menu.addSeparator()
        menu.add(createMenuItem("退出") { onExit() })

        // Swing 弹窗要求 invoker 处于显示状态，这里用一个透明小窗口占位，
        // 并把菜单位置摆到鼠标左上方，避免菜单超出屏幕（托盘通常位于屏幕边缘）。
        val invoker = JWindow().apply {
            setFocusableWindowState(false)
            isAlwaysOnTop = true
            runCatching { opacity = 0.0f }
        }
        val menuSize = menu.preferredSize
        val screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val maxX = (screenBounds.x + screenBounds.width - menuSize.width).coerceAtLeast(screenBounds.x)
        val maxY = (screenBounds.y + screenBounds.height - menuSize.height).coerceAtLeast(screenBounds.y)
        invoker.setBounds(
            (pointer.x - menuSize.width).coerceIn(screenBounds.x, maxX),
            (pointer.y - menuSize.height).coerceIn(screenBounds.y, maxY),
            1,
            1,
        )
        invoker.isVisible = true
        menu.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuWillBecomeVisible(event: PopupMenuEvent) = Unit

            override fun popupMenuWillBecomeInvisible(event: PopupMenuEvent) {
                // 菜单关闭后释放占位窗口，避免残留窗口。
                invoker.dispose()
            }

            override fun popupMenuCanceled(event: PopupMenuEvent) {
                invoker.dispose()
            }
        })
        menu.show(invoker, 0, 0)
    }

    /**
     * 切换到系统外观，并补上中文菜单字体。
     *
     * 系统外观的默认菜单字体不一定包含中文字形，需要显式换成系统中文字体，
     * 字号沿用系统外观给出的菜单字号，保证和系统菜单观感一致。
     */
    private fun installSystemLookAndFeel() {
        if (isSystemLookAndFeelInstalled) return
        isSystemLookAndFeelInstalled = true
        runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }
        val size = UIManager.getFont("MenuItem.font")?.size ?: DEFAULT_MENU_FONT_SIZE
        val font = resolveChineseFont(size)
        menuFont = font
        UIManager.put("MenuItem.font", font)
        UIManager.put("Menu.font", font)
        UIManager.put("PopupMenu.font", font)
    }

    /** 创建单个托盘菜单项。 */
    private fun createMenuItem(text: String, onClick: () -> Unit): JMenuItem =
        JMenuItem(text).apply {
            menuFont?.let { font = it }
            // 菜单由 Swing 负责绘制背景与选中态，这里只挂点击行为。
            addActionListener { onClick() }
        }

    /**
     * 选择一个系统已安装且支持中文的字体。
     *
     * 不能依赖默认字体：部分机器的默认菜单字体缺少中文字形，
     * 中文会被渲染成方块。
     */
    private fun resolveChineseFont(size: Int): Font {
        val installedFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
        val family = CHINESE_FONT_CANDIDATES.firstOrNull { installedFamilies.contains(it) }
        return Font(family ?: Font.SANS_SERIF, Font.PLAIN, size)
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
}
