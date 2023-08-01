package ui.fileview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ui.codeview.CodeViewer
import ui.codeview.MethodHeader
import viewmodel.FilesViewModel

/**
 * Panel that displays all opened files.
 * And their code attributes grouped by class name.
 */
@Composable
fun FileViewer(viewModel: FilesViewModel) {
    Row(
        Modifier.fillMaxWidth(0.6f).fillMaxHeight().border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium,
        ).clip(MaterialTheme.shapes.medium),
    ) {
        TreeView(viewModel, Modifier.fillMaxWidth(0.4f).fillMaxHeight())

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxHeight().width(1.dp),
        )

        Column {
            viewModel.currentCodeAttributeViewModel?.let {
                MethodHeader(it.codeAttribute)
                CodeViewer(it)
            }
        }
    }
}
