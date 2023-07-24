package ui.codeview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel

/**
 * Displays the name of the current file and a button to close it.
 */
@Composable
fun FileTab(viewModel: DebuggerViewModel, closeFile: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            viewModel.file.name,
            style = MaterialTheme.typography.titleSmall,
        )
        // An IconButton is currently fixed to 48.dp, so we need to make our own.
        // https://github.com/androidx/androidx/blob/androidx-main/compose/material/material/src/commonMain/kotlin/androidx/compose/material/IconButton.kt
        Box(
            modifier = Modifier.size(16.dp)
                .clickable(
                    onClick = closeFile,
                    role = Role.Button,
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(bounded = false, radius = 12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close file")
        }
    }
}

/**
 * Panel that displays all instructions and a pointer to the current instruction.
 * Also shows the parameters of a method and any errors that occur in the Partial Evaluator.
 * @param viewModel the [DebuggerViewModel] that contains the state of the debugger.
 */
@Composable
fun FileViewer(viewModel: DebuggerViewModel?, closeFile: () -> Unit) {
    Column(Modifier.fillMaxWidth(0.5f)) {
        Column(
            Modifier.fillMaxSize().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium,
            ).clip(MaterialTheme.shapes.medium),
        ) {
            viewModel?.let { viewModel ->
                FileTab(viewModel, closeFile)

                CodeViewer(viewModel)
            }
        }
    }
}
