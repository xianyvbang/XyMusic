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

package cn.xybbz.ui.xy

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.github.panpf.sketch.request.repeatCount
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.ui.generated.resources.Res
import xymusic_kmp.ui.generated.resources.default_placeholder

@Composable
fun XySmallImage(
    modifier: Modifier = Modifier,
    model: Any?,
    backModel: Any? = null,
    shape: Shape = RoundedCornerShape(XyTheme.dimens.corner),
    size: Dp = 50.dp,
    contentDescription: String? = null,
    placeholder: DrawableResource? = null,
    error: DrawableResource? = null,
    fallback: DrawableResource? = null,
    onSuccess: ((LoadState.Success) -> Unit)? = null,
    onLoading: ((LoadState.Started) -> Unit)? = null,
    onError: ((LoadState.Error) -> Unit)? = null,
) {
    XyImage(
        modifier = Modifier.clip(shape)
            .then(modifier)
            .size(size)
            .aspectRatio(1F),
        model = model,
        backModel = backModel,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        contentDescription = contentDescription,
        onSuccess = onSuccess,
        onLoading = onLoading,
        onError = onError
    )
}

@Composable
fun XyImage(
    modifier: Modifier = Modifier,
    model: Any?,
    backModel: Any? = null,
    placeholder: DrawableResource? = null,
    error: DrawableResource? = null,
    fallback: DrawableResource? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    contentDescription: String? = null,
    onSuccess: ((LoadState.Success) -> Unit)? = null,
    onLoading: ((LoadState.Started) -> Unit)? = null,
    onError: ((LoadState.Error) -> Unit)? = null,
) {
    val fallbackModel = when (backModel) {
        is String -> backModel.trim().takeIf { it.isNotBlank() }
        else -> backModel
    }
    val primaryModel = when (model) {
        is String -> model.trim().takeIf { it.isNotBlank() }
        else -> model
    }
    var tempModel by remember(primaryModel, fallbackModel) {
        mutableStateOf(primaryModel ?: fallbackModel)
    }

    when (val currentModel = tempModel) {
        is Painter -> Image(
            modifier = Modifier.then(modifier),
            painter = currentModel,
            contentDescription = contentDescription,
            alpha = alpha,
            contentScale = contentScale
        )

        is String -> {
            val state = rememberAsyncImageState(ComposableImageOptions {
                placeholder?.let { placeholder(placeholder) }
                error?.let { error(error) }
                fallback?.let { fallback(fallback) }
                // There is a lot more...
                repeatCount(0)
            })

            state.onLoadState = { it ->
                when (it) {
                    is LoadState.Success -> {
                        onSuccess?.invoke(it)
                    }

                    is LoadState.Started -> {
                        onLoading?.invoke(it)
                    }

                    is LoadState.Error -> {
                        if (fallbackModel != null && tempModel != fallbackModel) {
                            tempModel = fallbackModel
                        } else {
                            onError?.invoke(it)
                        }
                    }

                    is LoadState.Canceled -> {

                    }
                }
            }

            AsyncImage(
                modifier = Modifier
                    .then(modifier),
                uri = currentModel,
                state = state,
                contentDescription = contentDescription,
                alpha = alpha,
                contentScale = contentScale,
            )
        }
        else -> {
            error?.let {
                Image(
                    modifier = Modifier.then(modifier),
                    painter = painterResource(error),
                    contentDescription = contentDescription,
                    alpha = alpha,
                    contentScale = contentScale
                )
            }

//            Res.drawable.music_xy_placeholder_foreground

        }
    }
}

@Composable
fun XySmallImage(
    modifier: Modifier = Modifier,
    model: Painter?,
    contentDescription: String? = null
) {
    XySmallImage(
        modifier = modifier.size(44.dp),
        model = model,
        shape = RoundedCornerShape(8.dp),
        contentDescription = contentDescription
    )

}


@Composable
fun XySmallImage(
    modifier: Modifier = Modifier,
    model: Painter?,
    shape: Shape,
    contentDescription: String? = null
) {
    if (model != null)
        Image(
            modifier = Modifier
                .then(modifier)
                .aspectRatio(1F)
                .clip(shape),
            painter = model,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    else {
        Image(
            modifier = Modifier
                .then(modifier)
                .aspectRatio(1F)
                .clip(shape),
            painter = painterResource(resource = Res.drawable.default_placeholder),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    }

}
