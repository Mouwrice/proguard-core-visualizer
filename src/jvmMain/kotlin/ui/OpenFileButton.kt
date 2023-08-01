package ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import viewmodel.FileTypes
import viewmodel.FilesViewModel
import java.nio.file.Path

@Composable
fun OpenFileButton(viewModel: FilesViewModel) {
    var showFilePicker by remember { mutableStateOf(false) }

    Button(onClick = { showFilePicker = true }) {
        Text("Open file")
    }

    // Accept json files
    FilePicker(showFilePicker, fileExtensions = FileTypes.entries.map { it.extension }.toList()) { path ->
        showFilePicker = false
        if (path != null) {
            viewModel.loadFile(Path.of(path.path))
        }
    }
}
