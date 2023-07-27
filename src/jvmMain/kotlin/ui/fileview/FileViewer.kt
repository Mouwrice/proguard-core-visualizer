package ui.fileview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ui.codeview.CodeViewer
import viewmodel.DebuggerViewModel

/**
 * Panel that displays all instructions and a pointer to the current instruction.
 * Also shows the parameters of a method and any errors that occur in the Partial Evaluator.
 * @param viewModel the [DebuggerViewModel] that contains the state of the debugger.
 */
@Composable
fun FileViewer(viewModel: DebuggerViewModel, closeFile: () -> Unit) {
    Column(Modifier.fillMaxWidth(0.5f)) {
        Column(
            Modifier.fillMaxSize().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium,
            ).clip(MaterialTheme.shapes.medium),
        ) {
            if (viewModel.path != null) {
                FileTab(viewModel, closeFile)
                CodeViewer(viewModel)
            }
        }
    }
}
