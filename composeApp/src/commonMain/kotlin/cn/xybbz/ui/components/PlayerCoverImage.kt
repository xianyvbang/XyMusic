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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import cn.xybbz.config.image.CoverImageUrls
import cn.xybbz.ui.xy.XyImage
import com.github.panpf.sketch.request.LoadState
import org.jetbrains.compose.resources.painterResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.disc_placeholder

internal data class PlayerCoverModels(
    val model: Any?,
    val backModel: Any?
)

internal fun resolvePlayerCoverModels(
    coverUrls: CoverImageUrls,
    picByte: ByteArray?
): PlayerCoverModels {
    val model = coverUrls.primaryUrl ?: coverUrls.fallbackUrl ?: picByte
    val backModel = if (model == picByte) {
        null
    } else {
        coverUrls.fallbackUrl ?: picByte
    }
    return PlayerCoverModels(
        model = model,
        backModel = backModel
    )
}

@Composable
internal fun PlayerCoverImage(
    modifier: Modifier = Modifier,
    model: Any?,
    backModel: Any? = null,
    requestSize: IntSize? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    showPlaceholder: Boolean = true,
    contentDescription: String? = null,
    onSuccess: ((LoadState.Success) -> Unit)? = null,
    onLoading: ((LoadState.Started) -> Unit)? = null,
    onError: ((LoadState.Error) -> Unit)? = null,
) {
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
        }
    ) {
        if (showPlaceholder) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(Res.drawable.disc_placeholder),
                contentDescription = contentDescription,
                contentScale = contentScale
            )
        }
        XyImage(
            modifier = Modifier.fillMaxSize(),
            model = model,
            backModel = backModel,
            requestSize = requestSize,
            contentScale = contentScale,
            contentDescription = contentDescription,
            onSuccess = onSuccess,
            onLoading = onLoading,
            onError = onError
        )
    }
}
