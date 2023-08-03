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
import data.LoadedClass
import data.LoadedPath
import viewmodel.FilesViewModel
import java.nio.file.Path
import java.util.SortedMap

// Can be either a package or a class
data class PackageState(
    val name: String,
    val expanded: Boolean,
    val path: LoadedPath,
    val isFileEntry: Boolean,

    // Need to make distinction because of for example:
    // data.BranchTargetRecord.ErrorRecord
    // data.BranchTargetRecord
    val subPackages: SortedMap<String, PackageState>,
    val containingClasses: SortedMap<String, PackageState>,

    val clazz: LoadedClass?,
) {
    /**
     * Collapse this Package and all children.
     */
    private fun collapseRecursive(): PackageState {
        return PackageState(
            name,
            false,
            path,
            isFileEntry,
            subPackages.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            containingClasses.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            clazz,
        )
    }

    /**
     * Toggle expanded state, always makes sure children are collapsed.
     */
    fun toggleExpanded(): PackageState {
        return PackageState(
            name,
            !expanded,
            path,
            isFileEntry,
            subPackages.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            containingClasses.mapValues { it.value.collapseRecursive() }.toSortedMap(),
            clazz,
        )
    }
}

private fun sortByPackage(
    path: LoadedPath,
    classes: Map<String, LoadedClass>,
    classState: Map<String, PackageState>?,
): SortedMap<String, PackageState> {
    /**
     * Inner helper method for recursion.
     * Groups together all classes by their packages.
     */
    fun inner(
        classes: List<Pair<List<String>, LoadedClass>>,
        classState: Map<String, PackageState>?,
    ): SortedMap<String, PackageState> {
        // Classes is a list of to be handled classes, a pair of what packages remain and the class itself
        return classes.groupBy { it.first[0] }.map { (packageName, entries) ->
            // All entries share the first element of their name list
            Pair(
                packageName,
                PackageState(
                    packageName,
                    classState?.get(packageName)?.expanded ?: false,
                    path,
                    false,
                    inner(
                        entries
                            // If the child is a package, it has the current package, at least one next package and in the end, a class.
                            .filter { it.first.size > 2 }
                            .map {
                                Pair(
                                    it.first.slice(IntRange(1, it.first.size - 1)),
                                    it.second,
                                )
                            },
                        classState?.get(packageName)?.subPackages,
                    ),
                    inner(
                        entries
                            // If the child is a class, it has the current package, followed by the class name.
                            .filter { it.first.size == 2 }
                            .map {
                                Pair(
                                    it.first.slice(IntRange(1, it.first.size - 1)),
                                    it.second,
                                )
                            },
                        classState?.get(packageName)?.containingClasses,
                    ),

                    // The current "package" is not followed, this is not a package, but a class, put it in.
                    entries
                        .filter { it.first.size == 1 }.getOrNull(0)?.second,
                ),
            )
        }.toMap().toSortedMap()
    }
    return inner(classes.map { Pair(it.key.split("/"), it.value) }, classState)
}

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
                sortByPackage(
                    loadedPath,
                    loadedPath.classMap.filterKeys { it.contains("/") },
                    treeState[loadedPath.path]?.subPackages,
                ),
                sortByPackage(
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
                treeState.forEach { (path, packageInfo) ->
                    val nodes = buildTreeBranch(viewModel, packageInfo, 4.dp) {
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
