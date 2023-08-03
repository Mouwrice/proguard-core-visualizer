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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import data.LoadedPath
import data.OwnClazz
import data.OwnMethod
import viewmodel.FilesViewModel
import java.nio.file.Path

private data class ClassInfo(val path: LoadedPath, val expanded: Boolean, val clazz: OwnClazz, val methods: Map<String, OwnMethod>)

private data class ClazzBranchState(
    val name: String,
    val expanded: Boolean,
    val subPackage: Map<String, ClazzBranchState>,
    val containingClasses: Map<String, ClassInfo>,
)
private data class PathExpandedInfo(val path: LoadedPath, val expanded: Boolean, val classState: Map<String, ClazzBranchState>)

private fun sortByPackage(classes: Map<String, ClassInfo>, classState: Map<String, ClazzBranchState>?): Map<String, ClazzBranchState> {
    fun inner(classes: List<Pair<List<String>, ClassInfo>>, classState: Map<String, ClazzBranchState>?): Map<String, ClazzBranchState> {
        return classes.groupBy { it.first[0] }.map { (packageName, entries) ->
            Pair(
                packageName,
                ClazzBranchState(
                    packageName,
                    classState?.get(packageName)?.expanded ?: false,
                    inner(
                        entries
                            .filter { it.first.size > 1 }
                            .map {
                                Pair(
                                    it.first.slice(IntRange(1, it.first.size - 1)),
                                    it.second,
                                )
                            },
                        classState?.get(packageName)?.subPackage,
                    ),
                    entries
                        .filter { it.first.size == 1 }.associate { (className, classInfo) ->
                            Pair(
                                className[0],
                                ClassInfo(
                                    classInfo.path,
                                    classState?.get(packageName)?.containingClasses?.get(className[0])?.expanded ?: false,
                                    classInfo.clazz,
                                    classInfo.methods,
                                ),
                            )
                        },
                ),
            )
        }.toMap()
    }
    return inner(classes.map { Pair(it.key.split("/"), it.value) }, classState)
}

@Composable
fun TreeView(viewModel: FilesViewModel, modifier: Modifier = Modifier) {
    // Map of Path to <path open; Map of <Clazz; clazz open>>
    var treeState by remember { mutableStateOf(emptyMap<Path, PathExpandedInfo>().toSortedMap()) }

    // Recompute expandedState if pathMap gets changed
    LaunchedEffect(viewModel.files) {
        // val filesMap = mutableMapOf<Path, PathExpandedInfo>()
        // viewModel.files.forEach { (path, loadedPath) ->
        //    val newPackageMap = mutableMapOf<String, ClazzBranchState>()
        //    loadedPath.classMap.forEach { clazzName, clazz ->
        //        val packages = clazzName.split("/")
        //        var oldPackageMap = treeState[path]?.classState
        //        for (pack in packages) {
        //            val curNode = oldPackageMap?.get(pack)
        //            oldPackageMap = curNode?.subPackage
        //            newPackageMap
        //                .getOrPut(pack, { ClazzBranchState("", false, emptyMap(), emptyMap()) })
        //        }
        //    }
//
        //    filesMap.put(path, PathExpandedInfo(loadedPath, treeState[loadedPath.path]?.expanded ?: false, newPackageMap))
        // }

        treeState = viewModel.files.mapValues { (_, loadedPath) ->
            PathExpandedInfo(
                loadedPath,
                treeState[loadedPath.path]?.expanded ?: false,
                sortByPackage(
                    loadedPath.classMap.map { (key, clazz) ->
                        // Expanded state is later corrected
                        Pair(key, ClassInfo(loadedPath, false, clazz, clazz.methodMap))
                    }.toMap(),
                    treeState[loadedPath.path]?.classState,
                ),
            )
        }.toSortedMap()
    }

    val horizontalState = rememberScrollState(0)
    val verticalState = rememberLazyListState()

    Box(modifier = modifier) {
        Box(modifier = Modifier.horizontalScroll(horizontalState)) {
            LazyColumn(state = verticalState) {
                treeState.forEach { (_, pathInfo) ->
                    item {
                        node(
                            pathInfo.path.path.toString(),
                            4.dp,
                            if (pathInfo.expanded) IconMode.Open else IconMode.Closed,
                            closeCallback = {
                                viewModel.closeFile(pathInfo.path.path)
                            },
                        ) {
                            treeState = treeState.plus(
                                Pair(
                                    pathInfo.path.path,
                                    PathExpandedInfo(
                                        pathInfo.path,
                                        !pathInfo.expanded,
                                        pathInfo.classState
                                            .mapValues { (_, clazz) ->
                                                ClazzBranchState(clazz.name, false, clazz.subPackage, clazz.containingClasses)
                                            }
                                            .toSortedMap(),
                                    ),
                                ),
                            ).toSortedMap()
                        }
                    }
                    if (pathInfo.expanded) {
                        fun classInfo(classInfo: ClassInfo, indentation: Dp, registerChange: (ClassInfo) -> Unit) {
                            // Class
                            item {
                                node(
                                    classInfo.clazz.name,
                                    indentation,
                                    if (classInfo.expanded) IconMode.Open else IconMode.Closed,
                                ) {
                                    registerChange(
                                        ClassInfo(
                                            classInfo.path,
                                            !classInfo.expanded,
                                            classInfo.clazz,
                                            classInfo.methods,
                                        ),
                                    )
                                }
                            }
                            classInfo.methods.forEach { (_, method) ->
                                item {
                                    node(
                                        method.name,
                                        indentation + 12.dp,
                                        if (viewModel.curPath == classInfo.path && viewModel.curClazz == classInfo.clazz && viewModel.curMethod == method) {
                                            IconMode.Selected
                                        } else {
                                            IconMode.Unselected
                                        },
                                    ) {
                                        viewModel.curPath = classInfo.path
                                        viewModel.curClazz = classInfo.clazz
                                        viewModel.curMethod = method
                                    }
                                }
                            }
                        }
                        fun subDirBranch(clazzState: ClazzBranchState, indentation: Dp, registerChange: (ClazzBranchState) -> Unit) {
                            // This branch
                            item {
                                node(
                                    clazzState.name,
                                    indentation,
                                    if (clazzState.expanded) IconMode.Open else IconMode.Closed,
                                ) {
                                    registerChange(
                                        ClazzBranchState(
                                            clazzState.name,
                                            !clazzState.expanded,
                                            clazzState.subPackage,
                                            clazzState.containingClasses,
                                        ),
                                    )
                                }
                            }
                            if (clazzState.expanded) {
                                // Subbranches
                                clazzState.subPackage.forEach { (_, subPackage) ->
                                    subDirBranch(subPackage, indentation + 12.dp) {
                                        registerChange(
                                            ClazzBranchState(
                                                clazzState.name,
                                                clazzState.expanded,
                                                clazzState.subPackage.plus(Pair(subPackage.name, it)),
                                                clazzState.containingClasses,
                                            ),
                                        )
                                    }
                                }
                                clazzState.containingClasses.forEach { (_, classInfo) ->
                                    classInfo(classInfo, indentation) {
                                        registerChange(
                                            ClazzBranchState(
                                                clazzState.name,
                                                clazzState.expanded,
                                                clazzState.subPackage,
                                                clazzState.containingClasses.plus(Pair(classInfo.clazz.name, it)),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                        pathInfo.classState.forEach { (_, clazzState) ->
                            subDirBranch(clazzState, 12.dp) {
                                treeState = treeState.plus(
                                    Pair(
                                        pathInfo.path.path,
                                        PathExpandedInfo(
                                            pathInfo.path,
                                            pathInfo.expanded,
                                            pathInfo.classState.plus(
                                                Pair(
                                                    clazzState.name,
                                                    it,
                                                ),
                                            ).toSortedMap(),
                                        ),
                                    ),
                                ).toSortedMap()
                            }
                        }
                    }
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
