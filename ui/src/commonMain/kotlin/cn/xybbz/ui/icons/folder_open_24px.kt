package cn.xybbz.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 「打开文件所在位置」按钮使用的文件夹图标。
 *
 * 来自 Material Symbols Rounded 的 folder_open（24dp，默认填充样式）。
 */
@Suppress("CheckReturnValue")
public val folder_open_24px: ImageVector
  get() {
    if (_folder_open_24px != null) {
      return _folder_open_24px!!
    }
    _folder_open_24px =
      ImageVector.Builder(
          name = "folder_open_24px",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 960f,
          viewportHeight = 960f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(160f, 800f)
            quadTo(127f, 800f, 103.5f, 776.5f)
            quadTo(80f, 753f, 80f, 720f)
            lineTo(80f, 240f)
            quadTo(80f, 207f, 103.5f, 183.5f)
            quadTo(127f, 160f, 160f, 160f)
            lineTo(367f, 160f)
            quadTo(383f, 160f, 397.5f, 166f)
            quadTo(412f, 172f, 423f, 183f)
            lineTo(480f, 240f)
            lineTo(840f, 240f)
            quadTo(857f, 240f, 868.5f, 251.5f)
            quadTo(880f, 263f, 880f, 280f)
            quadTo(880f, 297f, 868.5f, 308.5f)
            quadTo(857f, 320f, 840f, 320f)
            lineTo(447f, 320f)
            lineTo(367f, 240f)
            lineTo(160f, 240f)
            quadTo(160f, 240f, 160f, 240f)
            quadTo(160f, 240f, 160f, 240f)
            lineTo(160f, 720f)
            quadTo(160f, 720f, 160f, 720f)
            quadTo(160f, 720f, 160f, 720f)
            lineTo(239f, 457f)
            quadTo(247f, 431f, 268.5f, 415.5f)
            quadTo(290f, 400f, 316f, 400f)
            lineTo(832f, 400f)
            quadTo(873f, 400f, 896.5f, 432.5f)
            quadTo(920f, 465f, 909f, 503f)
            lineTo(837f, 743f)
            quadTo(829f, 769f, 807.5f, 784.5f)
            quadTo(786f, 800f, 760f, 800f)
            lineTo(160f, 800f)
            close()
            moveTo(244f, 720f)
            lineTo(760f, 720f)
            quadTo(760f, 720f, 760f, 720f)
            quadTo(760f, 720f, 760f, 720f)
            lineTo(832f, 480f)
            quadTo(832f, 480f, 832f, 480f)
            quadTo(832f, 480f, 832f, 480f)
            lineTo(316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            lineTo(244f, 720f)
            close()
            moveTo(160f, 458f)
            lineTo(160f, 240f)
            quadTo(160f, 240f, 160f, 240f)
            quadTo(160f, 240f, 160f, 240f)
            lineTo(160f, 240f)
            lineTo(160f, 320f)
            lineTo(160f, 320f)
            quadTo(160f, 320f, 160f, 359f)
            quadTo(160f, 398f, 160f, 458f)
            close()
            moveTo(244f, 720f)
            lineTo(316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            lineTo(316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            quadTo(316f, 480f, 316f, 480f)
            lineTo(244f, 720f)
            quadTo(244f, 720f, 244f, 720f)
            quadTo(244f, 720f, 244f, 720f)
            close()
          }
        }
        .build()
    return _folder_open_24px!!
  }

private var _folder_open_24px: ImageVector? = null
