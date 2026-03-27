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
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.ui.generated.resources.Res
import xymusic_kmp.ui.generated.resources.default_placeholder

@Composable
fun XySmallImage(
    modifier: Modifier = Modifier,
    model: Any?,
    backModel: Any? = null,
    size: Dp = 50.dp,
    contentDescription: String? = null,
    placeholder: Painter? = painterResource(resource = Res.drawable.default_placeholder),
    error: Painter? = painterResource(resource = Res.drawable.default_placeholder),
    fallback: Painter? = painterResource(resource = Res.drawable.default_placeholder),
) {
    XyImage(
        modifier = Modifier
            .then(modifier)
            .size(size)
            .aspectRatio(1F)
            .clip(RoundedCornerShape(8.dp)),
        model = model,
        backModel = backModel,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        contentDescription = contentDescription,
    )
}

@Composable
fun XyImage(
    modifier: Modifier = Modifier,
    model: Any?,
    backModel: Any? = null,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    contentDescription: String? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
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

        else -> AsyncImage(
            modifier = Modifier
                .then(modifier),
            placeholder = placeholder,
            error = error,
            fallback = fallback,
            model = tempModel,
            contentDescription = contentDescription,
            alpha = alpha,
            contentScale = contentScale,
            onSuccess = onSuccess,
            onLoading = onLoading,
            onError = {
                if (fallbackModel != null && tempModel != fallbackModel) {
                    tempModel = fallbackModel
                } else {
                    onError?.invoke(it)
                }
            }
        )
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
