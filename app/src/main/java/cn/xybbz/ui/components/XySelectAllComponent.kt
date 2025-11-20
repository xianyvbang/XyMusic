package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.R

@Composable
fun XySelectAllComponent(
    isSelectAll: Boolean = false,
    onSelectAll: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        IconButton(
            modifier = Modifier.offset(x = (-10).dp),
            onClick = {
                onSelectAll()
            },
        ) {
            RadioButton(selected = isSelectAll, onClick = {
                onSelectAll()
            })
        }
        Text(
            text = if (isSelectAll) stringResource(R.string.deselect_all) else stringResource(
                R.string.select_all
            ), fontWeight = FontWeight.W600
        )

    }
}