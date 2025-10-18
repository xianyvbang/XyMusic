package cn.xybbz.ui.xy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import cn.xybbz.ui.theme.XyTheme


/**
 * 输入框
 *
 * @param text 文本
 * @param onChange 当文本改变时调用
 * @param modifier modifier
 * @param hint 提示
 * @param hintColor [hint] 文本颜色
 * @param readOnly 只读模式
 * @param paddingValues 边缘填充, 线条流畅的 IME
 * @param keyboardOptions 键盘选项
 * @param keyboardActions 键盘操作
 * @param visualTransformation 视觉信息
 * @param actionContent 行动内容
 */
@Composable
fun XyEdit(
    text: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
    hint: String? = null,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    readOnly: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.innerHorizontalPadding,
        vertical = XyTheme.dimens.innerVerticalPadding
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    BasicTextField(
        value = text,
        onValueChange = onChange,
        modifier = modifier
            .padding(paddingValues),
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .background(color = backgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = XyTheme.dimens.contentPadding)
                ) {
                    innerTextField()
                    if (hint != null && text.isEmpty()) {
                        Text(
                            text = hint,
                            color = hintColor
                        )
                    }
                }
                if (actionContent != null) {
                    actionContent()
                } else {
                    Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                }
            }
        }
    )
}