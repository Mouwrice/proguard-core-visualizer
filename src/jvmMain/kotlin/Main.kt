import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.FilePicker

@Composable
@Preview
fun App() {
    var showFilePicker by remember { mutableStateOf(false) }

    TextButton(onClick = { showFilePicker = true }) {
        Text("Open file explorer")
    }

    FilePicker(showFilePicker) { path ->
        showFilePicker = false
        println(path)
    }
}

fun main() = application {
    Window(title = "Proguard CORE Visualizer", onCloseRequest = ::exitApplication) {
        App()
    }
}
