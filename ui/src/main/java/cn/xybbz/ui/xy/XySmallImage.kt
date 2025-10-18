package cn.xybbz.ui.xy

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.R
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State

@Composable
fun XySmallImage(
    modifier: Modifier = Modifier,
    model: Any?,
    contentDescription: String? = null
) {
    XyImage(
        modifier = Modifier
            .then(modifier)
            .size(44.dp)
            .aspectRatio(1F)
            .clip(RoundedCornerShape(8.dp)),
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun XyImage(
    modifier: Modifier = Modifier,
    model: Any?,
    placeholder: Painter? = painterResource(id = R.drawable.default_placeholder),
    error: Painter? = painterResource(id = R.drawable.default_placeholder),
    fallback: Painter? = painterResource(id = R.drawable.default_placeholder),
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    contentDescription: String? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onLoading: ((State.Loading) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
) {
    AsyncImage(
        modifier = Modifier
            .then(modifier),
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        model = model,
        contentDescription = contentDescription,
        alpha = alpha,
        contentScale = contentScale,
        onSuccess = onSuccess,
        onLoading = onLoading,
        onError = onError
    )
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
            painter = painterResource(id = R.drawable.default_placeholder),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    }

}


@Composable
fun XyImage(
    modifier: Modifier = Modifier,
    model: Painter? = null,
    placeholder: Painter,
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
            painter = placeholder,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    }

}