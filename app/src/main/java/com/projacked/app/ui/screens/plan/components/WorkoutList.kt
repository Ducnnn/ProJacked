package com.projacked.app.ui.screens.plan.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.screens.plan.TemplatesState
import com.projacked.app.ui.theme.CardBorderWidth
import com.projacked.app.ui.theme.ProJackedTheme

private val PanelCorner = 10.dp

/**
 * One slice of the lilac "List of workouts" panel. The panel is drawn across separate lazy items: each slice is a
 * white band with the lilac content inset by the border width, and only the first slice ([top]) and the last
 * ([bottom]) get the rounded corners and the top and bottom border, so the slices read as one bordered card.
 */
@Composable
fun PanelSegment(
    top: Boolean,
    bottom: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = CardBorderWidth
    val outer = RoundedCornerShape(
        topStart = if (top) PanelCorner else 0.dp,
        topEnd = if (top) PanelCorner else 0.dp,
        bottomStart = if (bottom) PanelCorner else 0.dp,
        bottomEnd = if (bottom) PanelCorner else 0.dp,
    )
    val innerRadius: Dp = PanelCorner - border
    val inner = RoundedCornerShape(
        topStart = if (top) innerRadius else 0.dp,
        topEnd = if (top) innerRadius else 0.dp,
        bottomStart = if (bottom) innerRadius else 0.dp,
        bottomEnd = if (bottom) innerRadius else 0.dp,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ProJackedTheme.extendedColors.cardBorder, outer)
            .padding(start = border, end = border, top = if (top) border else 0.dp, bottom = if (bottom) border else 0.dp)
            .clip(inner)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        content = content,
    )
}

/** The periwinkle "List of workouts" header at the top of the panel. */
@Composable
fun PanelHeader(modifier: Modifier = Modifier) {
    PanelSegment(top = true, bottom = false, modifier = modifier) {
        Text(
            text = stringResource(R.string.plan_list_header),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * One workout: the name on a lilac pill and a plain square in the workout's colour. A plain tap does nothing;
 * a long press (or the screen reader's Delete action) asks to delete it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutRow(
    workout: TrainingDay,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val deleteLabel = stringResource(R.string.action_delete)
    val squareColor = ringColorFor(workout)
    PanelSegment(top = false, bottom = false, modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                )
                .semantics { customActions = listOf(CustomAccessibilityAction(deleteLabel) { onLongPress(); true }) }
                .heightIn(min = 60.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = workout.name,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(squareColor, MaterialTheme.shapes.extraSmall),
            )
        }
    }
}

/** What the panel shows in place of rows: loading, empty or error. */
@Composable
fun PanelMessage(templates: TemplatesState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    PanelSegment(top = false, bottom = false, modifier = modifier) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            when (templates) {
                TemplatesState.Loading -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                TemplatesState.Error -> TemplatesErrorText(onRetry, onSurface = false)
                is TemplatesState.Loaded -> Text(
                    text = stringResource(R.string.plan_list_empty),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

/** "Couldn't load your workouts" with a Retry button; [onSurface] picks the text colour for dialogs vs the panel. */
@Composable
fun TemplatesErrorText(onRetry: () -> Unit, onSurface: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.plan_list_error),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = if (onSurface) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

/** The closing slice: the panel's bottom border and rounded corners. */
@Composable
fun PanelFooter(modifier: Modifier = Modifier) {
    PanelSegment(top = false, bottom = true, modifier = modifier) {
        Box(Modifier.fillMaxWidth().height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPanelPreview() {
    ProJackedTheme {
        Column(Modifier.padding(16.dp)) {
            PanelHeader()
            WorkoutRow(TrainingDay("Push", "#e53935"), onLongPress = {})
            WorkoutRow(TrainingDay("A very long workout name that should be cut off with an ellipsis", "#FFFFFF"), onLongPress = {})
            PanelFooter()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPanelPreview() {
    ProJackedTheme {
        Column(Modifier.padding(16.dp)) {
            PanelHeader()
            PanelMessage(TemplatesState.Loaded(emptyList()), onRetry = {})
            PanelFooter()
        }
    }
}
