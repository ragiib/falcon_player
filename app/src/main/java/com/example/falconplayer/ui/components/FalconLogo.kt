package com.example.falconplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun FalconLogo(modifier: Modifier = Modifier.size(32.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Upper Wing Feather
        val feather1 = Path().apply {
            moveTo(w * (30f / 108f), h * (34f / 108f))
            lineTo(w * (48f / 108f), h * (30f / 108f))
            lineTo(w * (40f / 108f), h * (42f / 108f))
            lineTo(w * (26f / 108f), h * (42f / 108f))
            close()
        }
        drawPath(feather1, color = Color(0xFFB3121C))

        // 2. Middle Wing Feather
        val feather2 = Path().apply {
            moveTo(w * (26f / 108f), h * (45f / 108f))
            lineTo(w * (44f / 108f), h * (43f / 108f))
            lineTo(w * (36f / 108f), h * (54f / 108f))
            lineTo(w * (22f / 108f), h * (54f / 108f))
            close()
        }
        drawPath(feather2, color = Color(0xFFD81722))

        // 3. Lower Wing Feather
        val feather3 = Path().apply {
            moveTo(w * (28f / 108f), h * (57f / 108f))
            lineTo(w * (44f / 108f), h * (55f / 108f))
            lineTo(w * (38f / 108f), h * (66f / 108f))
            lineTo(w * (24f / 108f), h * (66f / 108f))
            close()
        }
        drawPath(feather3, color = Color(0xFF960E16))

        // 4. Main Aerodynamic Falcon Play Head / Beak (Forward ▶)
        val mainBeak = Path().apply {
            moveTo(w * (38f / 108f), h * (30f / 108f))
            lineTo(w * (74f / 108f), h * (48f / 108f))
            lineTo(w * (86f / 108f), h * (54f / 108f))
            lineTo(w * (74f / 108f), h * (60f / 108f))
            lineTo(w * (38f / 108f), h * (78f / 108f))
            lineTo(w * (48f / 108f), h * (54f / 108f))
            close()
        }
        val beakGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFF3843), Color(0xFFE50914), Color(0xFFB80710)),
            start = Offset(w * (38f / 108f), h * (30f / 108f)),
            end = Offset(w * (86f / 108f), h * (54f / 108f))
        )
        drawPath(mainBeak, brush = beakGradient)

        // 5. Beak Upper Crest Highlight
        val highlight = Path().apply {
            moveTo(w * (48f / 108f), h * (35f / 108f))
            lineTo(w * (86f / 108f), h * (54f / 108f))
            lineTo(w * (52f / 108f), h * (54f / 108f))
            close()
        }
        drawPath(highlight, color = Color.White.copy(alpha = 0.22f))

        // 6. Piercing White Eye / Play Core
        val eye = Path().apply {
            moveTo(w * (52f / 108f), h * (47f / 108f))
            lineTo(w * (60f / 108f), h * (51f / 108f))
            lineTo(w * (52f / 108f), h * (54f / 108f))
            lineTo(w * (54f / 108f), h * (50f / 108f))
            close()
        }
        drawPath(eye, color = Color.White)
    }
}
