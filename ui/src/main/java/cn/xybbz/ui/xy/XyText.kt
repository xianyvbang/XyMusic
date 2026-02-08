package cn.xybbz.ui.xy

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * 主要信息使用的Text
 */
@Composable
fun XyText(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight = FontWeight.Bold,
    maxLines: Int = 1,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        maxLines = maxLines,
        style = style,
        overflow = overflow,
        color = color
    )
}


/**
 * 次要信息使用的Text
 */
@Composable
fun XyTextSub(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    style: TextStyle = MaterialTheme.typography.bodySmall,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        modifier = modifier,
        text = text,
        inlineContent = inlineContent,
        style = style,
        overflow = overflow,
        textAlign = textAlign,
        color = color,
    )
}


/**
 * 次要信息使用的Text
 */
@Composable
fun XyTextSub(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        overflow = overflow,
        textAlign = textAlign,
        color = color
    )
}

/**
 * 页面标题Txt
 */
@Composable
fun XyScreenTitle(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight? = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        color = color,
        style = MaterialTheme.typography.titleLarge,
    )
}