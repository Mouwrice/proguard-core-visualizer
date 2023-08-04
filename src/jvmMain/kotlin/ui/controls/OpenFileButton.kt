package ui.controls

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import data.FileTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import viewmodel.FilesViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path

@Composable
fun OpenFileButton(viewModel: FilesViewModel) {
    var isOpen by remember { mutableStateOf(false) }
    var loadCount by remember { mutableStateOf(0) }

    if (isOpen) {
        FileDialog(
            onCloseRequest = { directory, file ->
                isOpen = false
                if (directory != null && file != null) {
                    CoroutineScope(Dispatchers.Default).launch {
                        loadCount++
                        try {
                            viewModel.loadFile(Path.of(directory, file))
                        } finally {
                            loadCount--
                        }
                    }
                }
            },
        )
    }

    Button(onClick = { isOpen = true }) {
        if (loadCount > 0) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.fillMaxHeight().aspectRatio(1f).scale(0.9f),
            )
            Spacer(Modifier.width(16.dp))
        }
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
