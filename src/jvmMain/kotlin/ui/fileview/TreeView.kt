package ui.fileview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    fun collapseRecursive(): PackageState {
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

private fun sortByPackage(path: LoadedPath, classes: Map<String, LoadedClass>, classState: Map<String, PackageState>?): SortedMap<String, PackageState> {
    /**
     * Inner helper method for recursion.
     * Groups together all classes by their packages.
     */
    fun inner(classes: List<Pair<List<String>, LoadedClass>>, classState: Map<String, PackageState>?): SortedMap<String, PackageState> {
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
                sortByPackage(loadedPath, loadedPath.classMap.filterKeys { it.contains("/") }, treeState[loadedPath.path]?.subPackages),
                sortByPackage(loadedPath, loadedPath.classMap.filterKeys { !it.contains("/") }, treeState[loadedPath.path]?.containingClasses),
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
                /**
                 * Tree branch composable, displays a single packed and its children.
                 */
                fun TreeBranch(packageState: PackageState, indentation: Dp, registerChange: (PackageState) -> Unit) {
                    // Display package name
                    item {
                        node(
                            packageState.name,
                            indentation,
                            if (packageState.expanded) IconMode.Open else IconMode.Closed,
                            // If the entry is a file, you can close it
                            closeCallback = if (packageState.isFileEntry) {
                                {
                                    viewModel.closeFile(packageState.path.path)
                                }
                            } else {
                                null
                            },
                        ) {
                            registerChange(packageState.toggleExpanded())
                        }
                    }
                    // If expanded, show children
                    if (packageState.expanded) {
                        // Show all subpackages first
                        packageState.subPackages.forEach { (subPackageName, subPackage) ->
                            TreeBranch(subPackage, indentation + 12.dp) {
                                registerChange(
                                    PackageState(
                                        packageState.name,
                                        packageState.expanded,
                                        packageState.path,
                                        packageState.isFileEntry,
                                        packageState.subPackages.plus(Pair(subPackageName, it)).toSortedMap(),
                                        packageState.containingClasses,
                                        packageState.clazz,
                                    ),
                                )
                            }
                        }
                        // Then all classes within this package
                        packageState.containingClasses.forEach { (subPackageName, subPackage) ->
                            TreeBranch(subPackage, indentation + 12.dp) {
                                registerChange(
                                    PackageState(
                                        packageState.name,
                                        packageState.expanded,
                                        packageState.path,
                                        packageState.isFileEntry,
                                        packageState.subPackages,
                                        packageState.containingClasses.plus(Pair(subPackageName, it)).toSortedMap(),
                                        packageState.clazz,
                                    ),
                                )
                            }
                        }

                        // Lastly, show the methods of the current class (the above 2 maps should be empty in this case)
                        packageState.clazz?.let { ownClazz ->
                            ownClazz.methodMap.forEach { (_, method) ->
                                item {
                                    node(
                                        method.name,
                                        indentation + 12.dp,
                                        // Go through name since selection of a method that need to be evaluated will change the method instance
                                        if (viewModel.curPath?.path == packageState.path.path &&
                                            viewModel.curClazz?.name == ownClazz.name &&
                                            viewModel.curMethod?.name == method.name
                                        ) {
                                            IconMode.Selected
                                        } else {
                                            IconMode.Unselected
                                        },
                                    ) {
                                        viewModel.curPath = packageState.path
                                        viewModel.curClazz = ownClazz
                                        viewModel.curMethod = method
                                    }
                                }
                            }
                        }
                    }
                }
                // Display all file branches
                treeState.forEach { (path, packageInfo) ->
                    TreeBranch(packageInfo, 4.dp) {
                        treeState = treeState.plus(Pair(path, it)).toSortedMap()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun node(
    content: String,
    indentation: Dp,
    iconMode: IconMode,
    modifier: Modifier = Modifier,
    closeCallback: (() -> Unit)? = null,
    onCLick: () -> Unit,
) {
    val backgroundColor = if (iconMode == IconMode.Selected) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    Row(
        modifier.padding(start = indentation).background(backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 8.dp).onClick { onCLick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (iconMode) {
            IconMode.Open -> Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Drawer handle is open")
            IconMode.Closed -> Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Drawer handle is closed")
            IconMode.Selected -> Icon(
                Icons.Rounded.RadioButtonChecked,
                contentDescription = "Icon checked",
                Modifier.scale(0.5F),
                tint = MaterialTheme.colorScheme.primary,
            )

            IconMode.Unselected -> Icon(
                Icons.Rounded.RadioButtonUnchecked,
                contentDescription = "Icon unchecked",
                Modifier.scale(0.5F),
            )
        }

        TooltipArea(tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    text = content,
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }) {
            Text(
                content,
                style = TextStyle(textIndent = TextIndent(0.sp, 12.sp)),
            )
        }

        if (closeCallback != null) {
            // An IconButton is currently fixed to 48.dp, so we need to make our own.
            // https://github.com/androidx/androidx/blob/androidx-main/compose/material/material/src/commonMain/kotlin/androidx/compose/material/IconButton.kt
            Box(
                modifier = Modifier.size(16.dp).padding(start = 4.dp)
                    .clickable(
                        role = androidx.compose.ui.semantics.Role.Button,
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(bounded = false, radius = 12.dp),
                    ) {
                        closeCallback()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = "Close file",
                )
            }
        }
    }
}

enum class IconMode {
    Open,
    Closed,
    Selected,
    Unselected,
}
