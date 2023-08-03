package ui.fileview

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.ExperimentalMaterial3Api
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
import viewmodel.FilesViewModel
import java.nio.file.Path

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
                // Display all file branches
                treeState.forEach { (path, packageState) ->
                    val nodes = packageState.buildTreeBranch(viewModel, searchQuery, 4.dp) {
                        treeState = treeState.plus(Pair(path, it)).toSortedMap()
                    }
                    items(nodes) {
                        Node(it)
                    }
                }
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
