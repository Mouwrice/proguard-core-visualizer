package ui.controls

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import viewmodel.FileTypes
import viewmodel.FilesViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path

@Composable
fun OpenFileButton(viewModel: FilesViewModel) {
    var isOpen by remember { mutableStateOf(false) }

    if (isOpen) {
        FileDialog(
            onCloseRequest = { directory, file ->
                isOpen = false
                if (directory != null && file != null) {
                    viewModel.loadFile(Path.of(directory, file))
                }
            },
        )
    }

    Button(onClick = { isOpen = true }) {
        Text("Open file")
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (directory: String?, file: String?) -> Unit,
) = AwtWindow(
    create = {
        val dialog = object : FileDialog(parent, "Load file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory, file)
                }
            }
        }
        dialog.filenameFilter = FilenameFilter { _, name ->
            FileTypes.entries.toList().any { name.endsWith(it.extension) }
        }

        dialog
    },
    dispose = FileDialog::dispose,
)
