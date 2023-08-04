package ui.jbcEditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import viewmodel.FilesViewModel

@Composable
fun Editor(viewModel: FilesViewModel) {
    var text by remember { mutableStateOf("") }

    Box {
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                ),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxSize(),
            )
        }

        ExtendedFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd),
            onClick = {
                viewModel.currentScratchFileType?.let {
                    try {
                        viewModel.loadScratch(text, it)
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
