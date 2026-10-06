package com.projacked.app.ui.screens.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.components.BorderedCard
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * Placeholder pie values kept from the old app (P4-3): fixed 30 : 40 : 35 in the order protein, fats, carbs.
 * They aren't real nutrition data, which stays off the database (P4-12).
 */
private val PIE_VALUES = floatArrayOf(30f, 40f, 35f)

/** Placeholder water level kept from the old app (P4-3): the bottle is a fixed picture at 70%. */
private const val BOTTLE_FILL_FRACTION = 0.7f

private const val PIE_DRAW_IN_MILLIS = 1000

/**
 * The whole card is one button that opens Meals. The pie and bottle are decorative and fixed (see above).
 */
@Composable
fun NutritionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.action_meals)
    BorderedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroPie(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally))
                PieLegend()
            }
            WaterBottle(modifier = Modifier.height(200.dp).aspectRatio(0.5f))
        }
    }
}

@Composable
private fun MacroPie(modifier: Modifier = Modifier) {
    val colors = ProJackedTheme.extendedColors
    val sliceColors = listOf(colors.protein, colors.fats, colors.carbs)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(PIE_DRAW_IN_MILLIS)) }
    Canvas(modifier = modifier.clearAndSetSemantics { }) {
        val total = PIE_VALUES.sum()
        var start = -90f
        PIE_VALUES.forEachIndexed { i, value ->
            val sweep = value / total * 360f * progress.value
            drawArc(
                color = sliceColors[i],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = Fill,
            )
            start += sweep
        }
    }
}

@Composable
private fun PieLegend(modifier: Modifier = Modifier) {
    val colors = ProJackedTheme.extendedColors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LegendEntry(colors.protein, stringResource(R.string.home_pie_protein))
        LegendEntry(colors.fats, stringResource(R.string.home_pie_fats))
        LegendEntry(colors.carbs, stringResource(R.string.home_pie_carbs))
    }
}

@Composable
private fun LegendEntry(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** The empty bottle with the full bottle drawn on top, clipped to its bottom [BOTTLE_FILL_FRACTION]. */
@Composable
private fun WaterBottle(modifier: Modifier = Modifier) {
    val fillClip = remember {
        GenericShape { size, _ ->
            addRect(Rect(0f, size.height * (1f - BOTTLE_FILL_FRACTION), size.width, size.height))
        }
    }
    Box(modifier = modifier.clearAndSetSemantics { }) {
        Image(
            painter = painterResource(R.drawable.water_bottle_empty),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = painterResource(R.drawable.water_bottle_full),
            contentDescription = null,
            modifier = Modifier.matchParentSize().clip(fillClip),
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionCardPreview() {
    ProJackedTheme { NutritionCard(onClick = {}, modifier = Modifier.padding(16.dp)) }
}
