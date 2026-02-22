/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
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
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyTextSubSmall
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
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val allLibraryName = stringResource(R.string.all_media_libraries)

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = selectLibraryViewModel.backgroundConfig.selectLibraryBrash[0],
            bottomVerticalColor = selectLibraryViewModel.backgroundConfig.selectLibraryBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.media_library_selection)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_connection_info)
                    )
                }
            })

        ScreenLazyColumn(modifier = Modifier) {
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

                        XyTextSubSmall(
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
                                    contentDescription =
                                        if (library.id == Constants.MINUS_ONE_INT.toString())
                                            allLibraryName
                                        else library.name
                                }
                        )
                    }
                }
            }
        }
    }
}