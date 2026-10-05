package com.projacked.app.ui.screens.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projacked.app.R
import com.projacked.app.ui.theme.ProJackedTheme

/**
 * A white text field. Its [errors] sit inside the same white box, under the input: red on white stays readable,
 * red on the sky-blue background does not. [contentType] lets password managers fill the field.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    contentType: ContentType,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errors: List<String> = emptyList(),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                isError = errors.isNotEmpty(),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { this.contentType = contentType },
            )
            errors.forEach { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthTextFieldPreview() {
    ProJackedTheme {
        AuthTextField(
            value = "gleb",
            onValueChange = {},
            label = stringResource(R.string.auth_hint_email),
            contentType = ContentType.EmailAddress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            errors = listOf(stringResource(R.string.auth_error_invalid_email)),
        )
    }
}
