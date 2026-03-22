package cn.xybbz.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import coil.request.ImageRequest

@Composable
fun textToBitmap(name: String, url: String? = null): Any {
    return if (!url.isNullOrBlank()) url else {
        val context = LocalContext.current
        val letter = name.firstOrNull()?.uppercase() ?: "?"

        // Remember bitmap so it's not recreated every recomposition
        val bitmap = remember(letter) { generateInitialBitmap(letter) }
        ImageRequest.Builder(context)
            .data(bitmap)
            .build()
    }
}

// Function to generate a Bitmap with a single character
fun generateInitialBitmap(letter: String): Bitmap {
    val size = 256
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    // Background
    canvas.drawColor(Color.argb(100,64,112,74))

    // Text paint
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = 64f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // Draw text centered
    val xPos = size / 2f
    val yPos = size / 2f - (paint.descent() + paint.ascent()) / 2

    canvas.drawText(letter, xPos, yPos, paint)

    return bitmap
}
