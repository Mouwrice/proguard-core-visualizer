package ui.fileview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.core.BonsaiStyle
import cafe.adriel.bonsai.core.node.Branch
import cafe.adriel.bonsai.core.node.Leaf
import cafe.adriel.bonsai.core.tree.Tree
import ui.codeview.CodeViewer
import ui.codeview.MethodHeader
import viewmodel.DebuggerViewModel

/**
 * Panel that displays all instructions and a pointer to the current instruction.
 * Also shows the parameters of a method and any errors that occur in the Partial Evaluator.
 * @param viewModel the [DebuggerViewModel] that contains the state of the debugger.
 */
@Composable
fun FileViewer(viewModel: DebuggerViewModel?, closeFile: () -> Unit) {
    val tree = Tree<Pair<String, String?>?> {
        if (viewModel != null) {
            Branch(null, {
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
            }, name = viewModel?.file?.name ?: "") {
                viewModel?.codeAttributes?.groupBy { it.clazz }?.forEach { entry ->
                    val clazz = entry.key
                    val methods = entry.value

                    Branch(Pair(clazz, null), name = clazz) {
                        methods.forEach {
                            Leaf(Pair(clazz, it.method), name = it.method)
                        }
                    }
                }
            }
        }
    }
    Row(
        Modifier.fillMaxWidth(0.5f).fillMaxHeight().border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium,
        ).clip(MaterialTheme.shapes.medium),
    ) {
        Bonsai(tree, Modifier.fillMaxWidth(0.3f).border(1.dp, Color.Black).fillMaxHeight(), { node ->
            tree.toggleExpansion(node)
            if (node.content?.second != null) {
                val index = viewModel?.codeAttributes?.withIndex()
                    ?.find { it.value.clazz == node.content?.first && it.value.method == node.content?.second }

                if (index != null) viewModel.selectCodeAttribute(index.index)
            }
        })

        if (viewModel != null) {
            Column {
                MethodHeader(viewModel.codeAttributes[viewModel.currentCodeAttribute])

                CodeViewer(viewModel)
            }
        }
    }
}
