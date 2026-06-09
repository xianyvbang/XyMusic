package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.deselect_all
import xymusic_kmp.composeapp.generated.resources.select_all

@Composable
fun XySelectAllComponent(
    isSelectAll: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable {
                onSelectAll()
            },
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        RadioButton(selected = isSelectAll, onClick = {
            onSelectAll()
        })
        Text(
            text = if (isSelectAll) stringResource(Res.string.deselect_all) else stringResource(
                Res.string.select_all
            ), fontWeight = FontWeight.W600
        )

        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))

    }
}
