package com.projacked.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.ui.theme.CardBorderWidth
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * The old app's card: a rounded (10dp) container with a 3dp white border. [containerColor] is periwinkle
 * (`colorScheme.primaryContainer`, the default) or lilac (`colorScheme.secondaryContainer`).
 */
@Composable
fun BorderedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(CardBorderWidth, ProJackedTheme.extendedColors.cardBorder),
    ) {
        Column(content = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun BorderedCardPreview() {
    ProJackedTheme {
        Column(Modifier.padding(16.dp)) {
            BorderedCard {
                Text("Periwinkle card", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            BorderedCard(
                modifier = Modifier.padding(top = 12.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text("Lilac card", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}
