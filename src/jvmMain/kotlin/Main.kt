import androidx.compose.desktop.ui.tooling.preview.Preview
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
import data.MachineParser
import java.io.File
import java.io.InputStream
import kotlin.streams.toList

@Composable
fun App() {
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf("No file open") }
    var lines by remember { mutableStateOf(listOf<String>()) }
    var currentLine by remember { mutableStateOf(0) }
    var currentInstruction by remember { mutableStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(all = 16.dp)) {
            FileView(selectedFile, lines, currentLine) {
                showFilePicker = true
            }
            Spacer(Modifier.padding(horizontal = 8.dp))
            Debugger(onNext = {
                currentInstruction++
                if (currentLine < lines.size) {
                    currentLine++
                }
            }, onPrevious = {
                currentInstruction--
                if (currentLine > 0) {
                    currentLine--
                }
            })
        }

        FilePicker(showFilePicker, fileExtensions = listOf("jbc")) { path ->
            showFilePicker = false
            if (path != null) {
                val file = File(path.path)
                selectedFile = file.name
                val inputStream: InputStream = file.inputStream()
                lines = inputStream.bufferedReader().lines().toList()
            }
        }
    }
}

fun main() = application {
    MachineParser().parsejson()
    Window(title = "Proguard CORE Visualizer", onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
}
