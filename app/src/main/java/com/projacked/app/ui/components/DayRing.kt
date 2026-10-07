package com.projacked.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projacked.app.ui.theme.ProJackedTheme

private val RingSize = 45.dp
private val RingWidth = 6.dp
private val LoadingOutlineWidth = 1.dp
private const val NUMBER_SIZE_SP = 18f
private const val MAX_NUMBER_SCALE = 1.3f

/**
 * The old `CircularTextView`: the day number in a white circle with a ring drawn on a `Canvas`.
 *
 * @param ringColor the ring colour, or null while the date is loading (a thin grey outline instead).
 * @param isToday draws the number in heavy type.
 */
@Composable
fun DayRing(
    dayNumber: Int,
    ringColor: Color?,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val loadingColor = ProJackedTheme.extendedColors.attendanceNone
    // The number stops growing with the font size once it would no longer fit inside the ring.
    val fontScale = LocalDensity.current.fontScale
    val numberSize = (NUMBER_SIZE_SP * minOf(fontScale, MAX_NUMBER_SCALE) / fontScale).sp
    Box(modifier.size(RingSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(RingSize)) {
            drawCircle(color = surface)
            if (ringColor != null) {
                val width = RingWidth.toPx()
                drawCircle(color = ringColor, radius = (size.minDimension - width) / 2f, style = Stroke(width))
            } else {
                val width = LoadingOutlineWidth.toPx()
                drawCircle(color = loadingColor, radius = (size.minDimension - width) / 2f, style = Stroke(width))
            }
        }
        Text(
            text = dayNumber.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = numberSize,
            fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayRingPreview() {
    ProJackedTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayRing(6, parseTemplateColor("#e53935"), isToday = false)
            DayRing(7, ProJackedTheme.extendedColors.restDay, isToday = false)
            DayRing(8, parseTemplateColor("#FFFFFF"), isToday = false)
            DayRing(9, parseTemplateColor("#1E88E5"), isToday = true)
            DayRing(10, null, isToday = false)
        }
    }
}
