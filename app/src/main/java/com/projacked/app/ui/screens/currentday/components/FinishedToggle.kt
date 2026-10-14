package com.projacked.app.ui.screens.currentday.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.theme.CardBorderWidth
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * The Finished toggle: a lilac pill with an icon and a label. Screen readers hear a "Finished" checkbox that is
 * checked or not, whatever the visible label says.
 */
@Composable
fun FinishedToggle(
    finished: Boolean,
    onFinishedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.logger_finished)
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(CardBorderWidth, ProJackedTheme.extendedColors.cardBorder),
        modifier = modifier
            .heightIn(min = 48.dp)
            .toggleable(value = finished, role = Role.Checkbox, onValueChange = onFinishedChange)
            .semantics { contentDescription = description },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                painter = painterResource(if (finished) R.drawable.ic_check_circle else R.drawable.ic_check_circle_outline),
                contentDescription = null,
            )
            Text(
                text = stringResource(if (finished) R.string.logger_finished else R.string.logger_not_finished),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FinishedToggleOffPreview() {
    ProJackedTheme { FinishedToggle(finished = false, onFinishedChange = {}, modifier = Modifier.padding(16.dp)) }
}

@Preview(showBackground = true)
@Composable
private fun FinishedToggleOnPreview() {
    ProJackedTheme { FinishedToggle(finished = true, onFinishedChange = {}, modifier = Modifier.padding(16.dp)) }
}
