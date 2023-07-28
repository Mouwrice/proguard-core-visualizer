package ui.fileview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.core.BonsaiStyle
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
        val tree = FileTree(viewModel.files) { index -> viewModel.closeFile(index) }
        Bonsai(
            tree = tree,
            style = BonsaiStyle(
                nodeCollapsedIconColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                nodeExpandedIconColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                toggleIconColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                nodeSelectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                nodePadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp),
            ),
            modifier = Modifier.fillMaxWidth(0.4f).fillMaxHeight(),
            onClick = { node ->
                tree.clearSelection()
                tree.toggleExpansion(node)
                tree.selectNode(node)
                node.content?.let { viewModel.selectCodeAttribute(it.first, it.second) }
            },
        )

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxHeight().width(1.dp),
        )

        Column {
            viewModel.currentCodeAttribute?.let {
                MethodHeader(it.codeAttribute)
                CodeViewer(it)
            }
        }
    }
}
