package ui.fileview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.bonsai.core.node.Branch
import cafe.adriel.bonsai.core.node.Leaf
import cafe.adriel.bonsai.core.tree.Tree
import viewmodel.File
import kotlin.io.path.name

/**
 * Returns a bonsai tree where the content is the Pair<FileIndex, CodeAttributeIndex>
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTree(files: List<File>, closeFile: (Int) -> Unit): Tree<Pair<Int, Int>?> {
    return Tree {
        files.forEachIndexed { fileIndex, file ->
            Branch(null, customName = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // State change done here to avoid invalid state issues in the lifecycles.
                    var remove by remember { mutableStateOf(false) }
                    if (remove) {
                        remove = false
                        closeFile(fileIndex)
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
                        Icon(
                            Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = "Close file",
                        )
                    }

                    TooltipArea(tooltip = {
                        Surface(
                            modifier = Modifier.shadow(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                text = file.path.name,
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }) {
                        Text(
                            file.path.name,
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                        )
                    }
                }
            }) {
                // Clazz name to <attributeIndex, method>
                val branches = HashMap<String, MutableList<Pair<Int, String>>>()
                file.codeAttributeViewModels.forEachIndexed { attributesIndex, codeAttributeViewModels ->
                    branches.getOrPut(codeAttributeViewModels.codeAttribute.clazz) { mutableListOf() }
                        .add(Pair(attributesIndex, codeAttributeViewModels.codeAttribute.method))
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
