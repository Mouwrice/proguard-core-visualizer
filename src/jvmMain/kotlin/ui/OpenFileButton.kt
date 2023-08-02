package ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import viewmodel.FileTypes
import viewmodel.FilesViewModel
import java.awt.FileDialog
import java.io.FilenameFilter
import java.nio.file.Path

@Composable
fun OpenFileButton(viewModel: FilesViewModel) {
    Button(onClick = {
        val dialog = FileDialog(ComposeWindow(), "Load file", FileDialog.LOAD)
        dialog.filenameFilter = FilenameFilter { _, name ->
            FileTypes.entries.toList().any { name.endsWith(it.extension) }
        }
        dialog.isVisible = true

        dialog.file?.let { viewModel.loadFile(Path.of(dialog.directory, it)) }
    }) {
        Text("Open file")
    }
}
