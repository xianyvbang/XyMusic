package cn.xybbz

import java.awt.Color
import java.awt.Font
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

/**
 * 桌面端系统托盘图标。
 *
 * 在“关闭窗口最小化到托盘”开启时提供托盘入口，
 * 通过菜单恢复主窗口或真正退出应用。
 */
internal object DesktopTray {

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
            val popupMenu = PopupMenu()
            val showItem = MenuItem("显示主界面").apply { addActionListener { onShowWindow() } }
            val exitItem = MenuItem("退出").apply { addActionListener { onExit() } }
            popupMenu.add(showItem)
            popupMenu.addSeparator()
            popupMenu.add(exitItem)

            val trayIcon = TrayIcon(createTrayImage(), "XyMusic", popupMenu).apply {
                isImageAutoSize = true
                addMouseListener(object : MouseAdapter() {
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

    /**
     * 生成托盘图标，避免额外依赖图片资源。
     */
    private fun createTrayImage(): BufferedImage {
        val size = 32
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.color = Color(0xF0, 0xA0, 0x3C)
            graphics.fillRoundRect(2, 2, size - 4, size - 4, 10, 10)
            graphics.color = Color(0x1A, 0x12, 0x06)
            graphics.font = Font("Segoe UI", Font.BOLD, 18)
            val metrics = graphics.fontMetrics
            val textX = (size - metrics.stringWidth("X")) / 2
            val textY = (size - metrics.height) / 2 + metrics.ascent
            graphics.drawString("X", textX, textY)
        } finally {
            graphics.dispose()
        }
        return image
    }
}
