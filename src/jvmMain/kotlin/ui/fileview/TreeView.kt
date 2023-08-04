package ui.fileview

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.FileTypes
import ui.buttons.ResizableIconButton
import viewmodel.FilesViewModel
import java.nio.file.Path
import kotlin.io.path.name

/**
 * The input field for the search query.
 * The query can be a regex expression.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(text: String, onValueChange: (String) -> Unit = {}) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = { Text("Search") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.labelSmall,
    )
}

/**
 * The top level tree view, contains the search bar and the tree itself.
 */
@Composable
fun TreeView(viewModel: FilesViewModel, modifier: Modifier = Modifier) {
    // The query to search in the tree, if empty, no search is performed
    var searchQuery by remember { mutableStateOf("") }

    var treeState by remember { mutableStateOf(emptyMap<Path, PackageState>().toSortedMap()) }

    var newScratchFile by remember { mutableStateOf(false) }

    // Recompute expandedState if pathMap gets changed, Not using derived state since the new state depends on the old one.
    LaunchedEffect(viewModel.files) {
        treeState = viewModel.files.mapValues { (_, loadedPath) ->
            PackageState(
                loadedPath.path.toString(),
                treeState[loadedPath.path]?.expanded ?: false,
                loadedPath,
                true,
                PackageState.sortByPackage(
                    loadedPath,
                    loadedPath.classMap.filterKeys { it.contains("/") },
                    treeState[loadedPath.path]?.subPackages,
                ),
                PackageState.sortByPackage(
                    loadedPath,
                    loadedPath.classMap.filterKeys { !it.contains("/") },
                    treeState[loadedPath.path]?.containingClasses,
                ),
                null,
            )
        }.toSortedMap()
    }

    val horizontalState = rememberScrollState()
    val verticalState = rememberLazyListState()

    Column(modifier = modifier) {
        if (viewModel.files.isNotEmpty()) {
            SearchBar(searchQuery) {
                searchQuery = it
            }
        }

        Box {
            LazyColumn(state = verticalState, modifier = Modifier.horizontalScroll(horizontalState)) {
                // Display all non-scratch file branches
                treeState.filter { !it.key.name.startsWith("scratch-file") }.forEach { (_, packageState) ->
                    val nodes = packageState.buildTreeBranch(viewModel, searchQuery, 4.dp) {
                        treeState = treeState.plus(Pair(packageState.path.path, it)).toSortedMap()
                    }
                    items(nodes) {
                        Node(it)
                    }
                }

                // region scratch files
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Rounded.EditNote, contentDescription = "Scratch files")
                        Text("Scratch files")
                        Box {
                            ResizableIconButton(
                                icon = Icons.Rounded.Add,
                                contentDescription = "Add scratch file",
                            ) {
                                newScratchFile = true
                            }

                            DropdownMenu(
                                expanded = newScratchFile,
                                onDismissRequest = { newScratchFile = false },
                            ) {
                                FileTypes.entries.filter { it.canWrite }.forEach { fileType ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = fileType.name,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        },
                                        onClick = {
                                            viewModel.currentScratchFileType = fileType
                                            newScratchFile = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // Display all non-scratch file branches
                treeState.filter { it.key.name.startsWith("scratch-file") }.forEach { (_, packageState) ->
                    val nodes = packageState.buildTreeBranch(viewModel, searchQuery, 4.dp) {
                        treeState = treeState.plus(Pair(packageState.path.path, it)).toSortedMap()
                    }
                    items(nodes) {
                        Node(it)
                    }
                }

                // endregion
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(all = 4.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = verticalState,
                ),
                style = defaultScrollbarStyle().copy(
                    unhoverColor = MaterialTheme.colorScheme.outline,
                    hoverColor = MaterialTheme.colorScheme.onSurface,
                ),
            )

            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(all = 4.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = horizontalState,
                ),
                style = defaultScrollbarStyle().copy(
                    unhoverColor = MaterialTheme.colorScheme.outline,
                    hoverColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
