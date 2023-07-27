package ui.fileview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.core.BonsaiStyle
import cafe.adriel.bonsai.core.node.Branch
import cafe.adriel.bonsai.core.node.Leaf
import cafe.adriel.bonsai.core.tree.Tree
import ui.codeview.CodeViewer
import ui.codeview.MethodHeader
import viewmodel.DebuggerViewModel
import kotlin.io.path.name

/**
 * Returns a bonsai tree where the content is the index to the code attribute.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BonsaiTree(viewModel: DebuggerViewModel): Tree<Pair<Int, Int>?> {
    return Tree {
        viewModel.openedFiles.forEachIndexed { fileIndex, (path, tracker) ->
            Branch(null, customName = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // State change done here to avoid invalid state issues in the lifecycles.
                    var remove by remember { mutableStateOf(false) }
                    if (remove) {
                        remove = false
                        viewModel.closeFile(fileIndex)
                    }

                    // An IconButton is currently fixed to 48.dp, so we need to make our own.
                    // https://github.com/androidx/androidx/blob/androidx-main/compose/material/material/src/commonMain/kotlin/androidx/compose/material/IconButton.kt
                    Box(
                        modifier = Modifier.size(16.dp)
                            .clickable(
                                role = Role.Button,
                                interactionSource = MutableInteractionSource(),
                                indication = rememberRipple(bounded = false, radius = 12.dp),
                            ) {
                                remove = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, tint = MaterialTheme.colorScheme.error, contentDescription = "Close file")
                    }

                    TooltipArea(tooltip = {
                        Surface(
                            modifier = Modifier.shadow(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                text = path.name,
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }) {
                        Text(
                            path.name,
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                        )
                    }
                }
            }) {
                // Clazz name to <attributeIndex, method>
                val branches = HashMap<String, MutableList<Pair<Int, String>>>()
                tracker.codeAttributes.forEachIndexed { attributesIndex, codeAttribute ->
                    branches.getOrPut(codeAttribute.clazz) { mutableListOf() }.add(Pair(attributesIndex, codeAttribute.method))
                }
                branches.forEach { (clazz, methods) ->
                    Branch(null, customName = {
                        TooltipArea(tooltip = {
                            Surface(
                                modifier = Modifier.shadow(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.extraSmall,
                            ) {
                                Text(
                                    text = clazz,
                                    modifier = Modifier.padding(4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }) { Text(clazz, style = MaterialTheme.typography.labelMedium) }
                    }) {
                        methods.forEach { method ->
                            Leaf(
                                Pair(fileIndex, method.first),
                                customName = {
                                    TooltipArea(tooltip = {
                                        Surface(
                                            modifier = Modifier.shadow(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = MaterialTheme.shapes.extraSmall,
                                        ) {
                                            Text(
                                                text = method.second,
                                                modifier = Modifier.padding(4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                    }) { Text(method.second, style = MaterialTheme.typography.labelSmall) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Panel that displays all instructions and a pointer to the current instruction.
 * Also shows the parameters of a method and any errors that occur in the Partial Evaluator.
 * @param viewModel the [DebuggerViewModel] that contains the state of the debugger.
 */
@Composable
fun FileViewer(viewModel: DebuggerViewModel) {
    Row(
        Modifier.fillMaxWidth(0.6f).fillMaxHeight().border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium,
        ).clip(MaterialTheme.shapes.medium),
    ) {
        val tree = BonsaiTree(viewModel)
        var viewUpdate by remember { mutableStateOf<Pair<Int, Int>?>(null) }
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
                node.content?.let { viewUpdate = it }
            },
        )

        // State change done here to avoid invalid state issues in the lifecycles.
        viewUpdate?.let {
            viewModel.updateFileIndex(it.first)
            viewModel.updateAttributeIndex(it.second)
        }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxHeight().width(1.dp),
        )

        Column {
            viewModel.codeAttribute?.let { MethodHeader(it) }
            CodeViewer(viewModel)
        }
    }
}
