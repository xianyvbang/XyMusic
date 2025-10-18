package cn.xybbz.ui.xy

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    color: Color = Color.White,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = Modifier
            .then(modifier),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth(),
            color = color,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun XyButtonNotPadding(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(
            text = text,
            modifier = modifier,
            color = Color.White,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun XyButtonHorizontalPadding(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    XyButtonHorizontalPadding(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
        text = text,
        backgroundColor = backgroundColor
    )
}

@Composable
fun XyButtonHorizontalPadding(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = Modifier
            .then(modifier)
            .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(
            text = text,
            modifier = modifier,
            color = Color.White,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}