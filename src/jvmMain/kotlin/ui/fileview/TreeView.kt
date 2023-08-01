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
import viewmodel.FilesViewModel
import java.nio.file.Path
import java.util.*

@Composable
fun TreeView(viewModel: FilesViewModel, modifier: Modifier = Modifier) {
    // Map of Path to <path open; Map of <Clazz; clazz open>>
    var expandedState by remember { mutableStateOf(emptyMap<Path, Pair<Boolean, SortedMap<String, Boolean>>>().toSortedMap()) }

    // Recompute expandedState if pathMap gets changed
    LaunchedEffect(viewModel.files) {
        val map: MutableMap<Path, Pair<Boolean, MutableMap<String, Boolean>>> = HashMap()
        viewModel.files.forEach { (path, clazzMap) ->
            clazzMap.forEach { (clazz, _) ->
                map.getOrPut(path) { Pair(expandedState[path]?.first ?: false, HashMap()) }.second[clazz] =
                    expandedState[path]?.second?.get(clazz) ?: false
            }
        }
        expandedState = map.mapValues { Pair(it.value.first, it.value.second.toSortedMap()) }.toSortedMap()
    }

    val horizontalState = rememberScrollState(0)
    val verticalState = rememberLazyListState()

    Box(modifier = modifier) {
        Box(modifier = Modifier.horizontalScroll(horizontalState)) {
            LazyColumn(state = verticalState) {
                viewModel.files.forEach { (path, clazzMap) ->
                    val pathIsOpen = expandedState[path]?.first

                    item {
                        node(
                            path.toString(),
                            4.dp,
                            if (pathIsOpen == true) IconMode.Open else IconMode.Closed,
                            closeCallback = {
                                viewModel.closeFile(path)
                            },
                        ) {
                            expandedState[path]?.let { clazzInfo ->
                                expandedState = expandedState.plus(
                                    Pair(
                                        path,
                                        Pair(!clazzInfo.first, clazzInfo.second.mapValues { false }.toSortedMap()),
                                    ),
                                ).toSortedMap()
                            }
                        }
                    }
                    if (pathIsOpen == true) {
                        clazzMap.forEach { (clazz, methodList) ->
                            val clazzIsOpen = expandedState[path]?.second?.get(clazz)

                            item {
                                node(
                                    clazz,
                                    12.dp,
                                    if (clazzIsOpen == true) IconMode.Open else IconMode.Closed,
                                ) {
                                    expandedState[path]?.let { (openPath, map) ->
                                        map[clazz]?.let { openClazz ->
                                            expandedState = expandedState.plus(
                                                Pair(
                                                    path,
                                                    Pair(
                                                        openPath,
                                                        map.plus(
                                                            Pair(
                                                                clazz, !openClazz,
                                                            ),
                                                        ).toSortedMap(),
                                                    ),
                                                ),
                                            ).toSortedMap()
                                        }
                                    }
                                }
                            }
                            if (clazzIsOpen == true) {
                                methodList.forEach { (method, _) ->
                                    item {
                                        node(
                                            method,
                                            24.dp,
                                            if (viewModel.curPath == path && viewModel.curClazz == clazz && viewModel.curMethod == method) {
                                                IconMode.Selected
                                            } else {
                                                IconMode.Unselected
                                            },
                                        ) {
                                            viewModel.curPath = path
                                            viewModel.curClazz = clazz
                                            viewModel.curMethod = method
                                        }
                                    }
                                }
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
