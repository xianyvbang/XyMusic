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

package cn.xybbz.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cn.xybbz.R
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyTextSubSmall


var loadingObjectList = mutableStateListOf<LoadingObject>()

@Immutable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
data class LoadingObject constructor(
    val id: String,
    val messageIsStringRes: Boolean = true,
    val loadingCompose: @Composable (Float?) -> Unit = {
        if (it == null) {
            LoadingIndicator()
        } else {
            LoadingIndicator(progress = { it })
        }
    }
) {
    var message by mutableIntStateOf(R.string.loading)
        private set
    var progress: Float? by mutableStateOf(null)
        private set


    fun updateProgress(progress: Float, index: Int) {
        this.progress = progress
        this.message = index
    }

    fun updateMessageProgress(message: Int) {
        this.message = message
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingCompose(modifier: Modifier = Modifier) {
    loadingObjectList.forEach { it ->
        //禁用返回
        BackHandler(enabled = true) {}
        Box(
            modifier = Modifier
                .fillMaxSize()
                .debounceClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}, contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xff10b981), Color(0xff06b6d4)),
                            start = Offset(x = Float.POSITIVE_INFINITY, y = 0f), // 右上角
                            end = Offset(x = 0f, y = Float.POSITIVE_INFINITY)   // 左下角
                        ),
                        RoundedCornerShape(XyTheme.dimens.corner)
                    )
                    .padding(
                        end = XyTheme.dimens.outerHorizontalPadding
                    )
                    .zIndex(Float.MAX_VALUE),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                    it.loadingCompose(it.progress)
                }
                Spacer(modifier = Modifier.width(5.dp))
                XyTextSubSmall(text = if (it.messageIsStringRes) stringResource(it.message) else it.message.toString())
            }
        }

    }

}

/**
 * 显示Loading
 */
fun LoadingObject.show() {
    loadingObjectList.add(this@show)
}

/**
 * 关闭Loading
 */
fun LoadingObject.dismiss() = apply {
    loadingObjectList.remove(this@dismiss)
}
