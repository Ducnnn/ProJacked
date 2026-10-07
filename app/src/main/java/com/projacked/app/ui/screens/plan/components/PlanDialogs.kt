package com.projacked.app.ui.screens.plan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.domain.model.TrainingDay
import com.projacked.app.ui.screens.plan.PlanDialog
import com.projacked.app.ui.screens.plan.TemplatesState
import com.projacked.app.ui.theme.ProJackedTheme
import java.time.LocalDate

/**
 * One dialog for assigning a date: "Rest day", then each workout with its colour, then Cancel.
 * [current] is what the date holds now (null or a rest workout reads as "Rest day").
 */
@Composable
fun AssignDialog(
    date: LocalDate,
    current: TrainingDay?,
    templates: TemplatesState,
    onChoose: (TrainingDay) -> Unit,
    onRetryTemplates: () -> Unit,
    onDismiss: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val restName = stringResource(R.string.home_rest_day)
    val currentName = if (current == null || current.isRest) restName else current.name
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(formatPlanDate(date, locale))
                Text(
                    text = stringResource(R.string.plan_assign_now, currentName),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OptionRow(name = restName, color = ringColorFor(null), onClick = { onChoose(TrainingDay.REST) })
                when (templates) {
                    TemplatesState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp).size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    TemplatesState.Error -> TemplatesErrorText(onRetryTemplates, onSurface = true)
                    is TemplatesState.Loaded ->
                        if (templates.templates.isEmpty()) {
                            Text(
                                text = stringResource(R.string.plan_no_workouts_yet),
                                modifier = Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            templates.templates.forEach { template ->
                                OptionRow(
                                    name = template.name,
                                    color = ringColorFor(template),
                                    onClick = { onChoose(template) },
                                )
                            }
                        }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun OptionRow(name: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(color, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
        Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Replace confirmation (P5-6): the date has logged sets. */
@Composable
fun ConfirmReplaceDialog(
    date: LocalDate,
    currentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    ConfirmDialog(
        title = stringResource(R.string.plan_replace_title, currentName),
        text = stringResource(R.string.plan_replace_text, formatPlanDate(date, locale)),
        confirmLabel = stringResource(R.string.action_replace),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/** Copy confirmation (P5-7): the week shown already has a workout. */
@Composable
fun ConfirmCopyDialog(weekStart: LocalDate, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    ConfirmDialog(
        title = stringResource(R.string.plan_copy_title),
        text = stringResource(
            R.string.plan_copy_text,
            formatWeekRange(context, weekStart, locale),
            formatWeekRange(context, weekStart.minusDays(7), locale),
        ),
        confirmLabel = stringResource(R.string.action_copy),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/** Delete confirmation (P5-8). */
@Composable
fun ConfirmDeleteDialog(templateName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.plan_delete_title, templateName),
        text = stringResource(R.string.plan_delete_text),
        confirmLabel = stringResource(R.string.action_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Shows the dialog the state says is open. */
@Composable
fun PlanDialogHost(
    dialog: PlanDialog?,
    days: Map<LocalDate, TrainingDay>,
    templates: TemplatesState,
    onChoose: (TrainingDay) -> Unit,
    onRetryTemplates: () -> Unit,
    onConfirmReplace: () -> Unit,
    onConfirmCopy: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        null -> Unit
        is PlanDialog.Assign -> AssignDialog(
            date = dialog.date,
            current = days[dialog.date],
            templates = templates,
            onChoose = onChoose,
            onRetryTemplates = onRetryTemplates,
            onDismiss = onDismiss,
        )
        is PlanDialog.ConfirmReplace -> ConfirmReplaceDialog(
            date = dialog.date,
            currentName = days[dialog.date]?.name ?: TrainingDay.REST_NAME,
            onConfirm = onConfirmReplace,
            onDismiss = onDismiss,
        )
        is PlanDialog.ConfirmCopy -> ConfirmCopyDialog(dialog.weekStart, onConfirmCopy, onDismiss)
        is PlanDialog.ConfirmDelete -> ConfirmDeleteDialog(dialog.templateName, onConfirmDelete, onDismiss)
    }
}

private val PreviewTemplates = TemplatesState.Loaded(
    listOf(TrainingDay("Legs", "#FFFFFF"), TrainingDay("pull", "#1E88E5"), TrainingDay("Push", "#e53935")),
)

@Preview(showBackground = true)
@Composable
private fun AssignDialogPreview() {
    ProJackedTheme {
        AssignDialog(LocalDate.of(2026, 10, 6), TrainingDay("pull", "#1E88E5"), PreviewTemplates, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AssignDialogNoWorkoutsPreview() {
    ProJackedTheme {
        AssignDialog(LocalDate.of(2026, 10, 6), null, TemplatesState.Loaded(emptyList()), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmReplaceDialogPreview() {
    ProJackedTheme { ConfirmReplaceDialog(LocalDate.of(2026, 10, 6), "Push", {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmCopyDialogPreview() {
    ProJackedTheme { ConfirmCopyDialog(LocalDate.of(2026, 10, 5), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDeleteDialogPreview() {
    ProJackedTheme { ConfirmDeleteDialog("Push", {}, {}) }
}
