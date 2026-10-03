package com.projacked.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.theme.ProJackedTheme

/** One button on a placeholder screen. */
data class PlaceholderAction(val label: String, val onClick: () -> Unit)

/**
 * Temporary screen used until each feature is built (Phases 3–9). Shows the screen's title and buttons for
 * its outgoing navigation, so the whole graph can be clicked through.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    actions: List<PlaceholderAction> = emptyList(),
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.placeholder_note), style = MaterialTheme.typography.bodyMedium)
            actions.forEach { action ->
                Button(onClick = action.onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(action.label)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    ProJackedTheme {
        PlaceholderScreen(
            title = "Screen title",
            actions = listOf(PlaceholderAction("First action") {}, PlaceholderAction("Second action") {}),
        )
    }
}
