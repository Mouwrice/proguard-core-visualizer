import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import data.DebuggerViewModel
import ui.CodeViewer
import ui.Controls
import ui.StateViewer

@Composable
fun App(viewModel: DebuggerViewModel) {
    var showFilePicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(all = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Controls(viewModel) {
                showFilePicker = it
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CodeViewer(viewModel)
                StateViewer(viewModel)
            }
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
    viewModel.loadJson("examples/pe-variable-empty-ex.json")
    Window(
        title = "Proguard CORE Visualizer",
        state = WindowState(WindowPlacement.Maximized),
        onCloseRequest = ::exitApplication,
    ) {
        App(viewModel)
    }
}
