package ui.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.Display
import viewmodel.FilesViewModel

@Composable
fun Controls(viewModel: FilesViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.wrapContentSize(align = Alignment.Companion.CenterStart, unbounded = true),
    ) {
        OpenFileButton(viewModel)
        SaveButton(viewModel)
        EvalPicker(viewModel)

        val currentCodeViewModel = viewModel.currentCodeAttributeViewModel
        OutlinedButton(
            enabled = currentCodeViewModel != null && currentCodeViewModel.hasPrevious,
            onClick = { currentCodeViewModel?.previous() },
        ) {
            Text("Previous")
        }

        OutlinedButton(
            enabled = currentCodeViewModel != null && currentCodeViewModel.hasNext,
            onClick = { currentCodeViewModel?.next() },
        ) {
            Text("Next")
        }

        OutlinedButton(
            enabled = currentCodeViewModel != null,
            onClick = { currentCodeViewModel?.switchDisplay() },
        ) {
            when (currentCodeViewModel?.display) {
                Display.EVALUATIONS -> Text("Show results")
                Display.RESULTS -> Text("Show evaluations")
                else -> { Text("Show results") }
            }
        }
    }
}
