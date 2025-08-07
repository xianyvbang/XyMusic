package cn.xybbz.ui.xy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun XyLoadingItem(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = Modifier
            .then(modifier)
            .height(80.dp)
            .fillMaxWidth()
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                modifier = Modifier,
                fontSize = 28.sp,
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = Color.Gray,
                strokeWidth = 4.dp
            )
        }
    }
}