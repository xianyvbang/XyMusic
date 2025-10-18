package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.localdata.data.search.SearchHistory
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyItemText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchRecordComponent(
    searchListData: List<SearchHistory>,
    title: String,
    onClick: (String) -> Unit,
    onClear: () -> Unit
) {

    val searchList by remember {
        mutableStateOf(searchListData)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = {
                onClear()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "${stringResource(R.string.delete_prefix)}${title}",
                    modifier = Modifier
                        .size(20.dp)
                )
            }

        }

        if (searchList.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerHorizontalPadding / 2),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                searchList.forEachIndexed { _, value ->
                    SuggestionChip(
                        onClick = {
                            onClick(value.searchQuery)
                        }, label = {
                            XyItemText(text = value.searchQuery)
                        })
                }

            }

        } else {

            InputChip(
                selected = false,
                onClick = {

                },
                label = {
                    Text(
                        text = stringResource(R.string.no_search_history),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent)
            )
        }
    }
}
