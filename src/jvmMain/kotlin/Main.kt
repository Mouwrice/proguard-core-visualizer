import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import data.DebuggerViewModel
import ui.Debugger
import ui.StateTrackerViewer

@Composable
fun App(viewModel: DebuggerViewModel) {
    var showFilePicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(all = 16.dp)) {
            StateTrackerViewer(viewModel) {
                showFilePicker = true
            }

            Spacer(Modifier.padding(horizontal = 8.dp))
            Debugger(viewModel)
        }

        // Accept json files
        FilePicker(showFilePicker, fileExtensions = listOf("json")) { path ->
            showFilePicker = false
            if (path != null) {
                viewModel.loadJson(path.path)
            }
        }
    }
}

fun main() = application {
    val viewModel = DebuggerViewModel()
    Window(title = "Proguard CORE Visualizer", onCloseRequest = ::exitApplication) {
        App(viewModel)
    }
}
