package ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import viewmodel.FilesViewModel

/**
 * A very simple text input field that can be used to edit all [data.FileTypes]
 * that have the `canWrite` property set to true.
 * This allows to have scratch files and edit existing files that are supported.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Editor(viewModel: FilesViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = viewModel.currentScratchFileContent,
            onValueChange = { viewModel.currentScratchFileContent = it },
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
            ),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxHeight(0.9f).fillMaxWidth(),
        )

        Row {
            OutlinedButton(
                content = {
                    Text("Cancel")
                },
                modifier = Modifier.padding(8.dp),
                onClick = {
                    viewModel.currentScratchFileType?.let {
                        viewModel.currentScratchFileType = null
                        viewModel.currentScratchFileContent = ""
                    }
                },
            )

            Button(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    viewModel.currentScratchFileType?.let {
                        try {
                            viewModel.parseScratch(it)
                        } catch (exception: Exception) {
                            viewModel.exception = exception
                        }
                    }
                },
            ) {
                Text("Submit")
            }
        }
    }
}
