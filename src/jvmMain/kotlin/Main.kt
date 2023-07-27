import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.jthemedetecor.OsThemeDetector
import com.materialkolor.AnimatedDynamicMaterialTheme
import ui.Controls
import ui.fileview.FileViewer
import ui.stateview.StateViewer
import viewmodel.DebuggerViewModel
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Composable
fun App() {
    var viewModel by rememberSaveable { mutableStateOf<DebuggerViewModel?>(null) }
    var showFilePicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(all = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Controls(viewModel) {
                showFilePicker = it
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Closing the file means setting the view model to null.
                FileViewer(viewModel) {
                    viewModel = null
                }
                StateViewer(viewModel)
            }
        }

        // Accept json files
        FilePicker(showFilePicker, fileExtensions = listOf("json")) { path ->
            showFilePicker = false
            if (path != null) {
                // If we already have a view model, load the json file into it.
                // Otherwise, create a new view model from the json file.
                viewModel = viewModel?.loadJson(path.path) ?: DebuggerViewModel.fromJson(path.path)
            }
        }
    }
}

fun main() = application {
    // TODO: remove when https://github.com/JetBrains/compose-multiplatform/issues/3366 is resolved
    System.setProperty("compose.scrolling.smooth.enabled", "false")

    val version = System.getProperty("app.version") ?: "DEV"

    // app.dir is set when packaged to point at our collected inputs.
    val appIcon = remember {
        System.getProperty("app.dir")
            ?.let { Paths.get(it, "icon-512.png") }
            ?.takeIf { it.exists() }
            ?.inputStream()
            ?.buffered()
            ?.use { BitmapPainter(loadImageBitmap(it)) }
    }

    Window(
        title = "Proguard CORE Visualizer $version",
        icon = appIcon,
        onCloseRequest = ::exitApplication,
    ) {
        AppTheme(
            seedColor = Color.Blue,
        ) {
            App()
        }
    }
}

@Composable
internal fun AppTheme(
    seedColor: Color,
    content: @Composable () -> Unit,
) {
    // the `isSystemInDarkTheme()` is not yet fully supported on all desktop platforms:
    // https://github.com/JetBrains/compose-multiplatform/issues/169
    // so we use this neat library that should be able to detect the system theme on most major platforms.
    // https://github.com/Dansoftowner/jSystemThemeDetector
    val detector = OsThemeDetector.getDetector()
    var isDarkTheme by remember { mutableStateOf(detector.isDark) }
    detector.registerListener { isDarkTheme = it }
    MaterialTheme {
        // Great library by Jordon de Hoog for creating dynamic Material Design 3 color palettes from any color,
        // similar to generating a theme from https://m3.material.io/theme-builder
        // https://github.com/jordond/MaterialKolor
        AnimatedDynamicMaterialTheme(
            seedColor = seedColor,
            useDarkTheme = isDarkTheme,
        ) {
            Surface { content() }
        }
    }
}
