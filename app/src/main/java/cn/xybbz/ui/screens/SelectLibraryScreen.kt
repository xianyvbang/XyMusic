package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.viewmodel.SelectLibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLibraryScreen(
    connectionId: Long,
    thisLibraryId: String?,
    selectLibraryViewModel: SelectLibraryViewModel = hiltViewModel<SelectLibraryViewModel, SelectLibraryViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                connectionId = connectionId,
                thisLibraryId = thisLibraryId
            )
        }
    )
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()


    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = selectLibraryViewModel.backgroundConfig.selectLibraryBrash[0],
            bottomVerticalColor = selectLibraryViewModel.backgroundConfig.selectLibraryBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.media_library_selection)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_connection_info)
                    )
                }
            })

        LazyColumnNotComponent(modifier = Modifier) {
            items(selectLibraryViewModel.libraryList) { library ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .debounceClickable {
                                coroutineScope.launch {
                                    selectLibraryViewModel.updateLibraryId(library.id)
                                }
                            }
                            .padding(
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding / 2
                            )
                    ) {
                        XyItemText(
                            text = if (library.id == Constants.MINUS_ONE_INT.toString())
                                stringResource(library.name.toInt())
                            else library.name,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = selectLibraryViewModel.libraryId == library.id,
                            onClick = {
                                coroutineScope.launch {
                                    selectLibraryViewModel.updateLibraryId(library.id)

                                }
                            },
                            modifier = Modifier
                                .semantics {
                                    contentDescription = if (library.id == Constants.MINUS_ONE_INT.toString())
                                        context.getString(library.name.toInt())
                                    else library.name
                                }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                }
            }
        }
    }
}