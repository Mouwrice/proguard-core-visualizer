package ui.fileview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    val state = rememberLazyListState()
    val pathMap = viewModel.files

    // Map of Path to <path open; Map of <Clazz; clazz open>>
    var expandedState by remember { mutableStateOf(emptyMap<Path, Pair<Boolean, SortedMap<String, Boolean>>>().toSortedMap()) }

    // Recompute expandedState if pathMap gets changed
    LaunchedEffect(pathMap) {
        val map: MutableMap<Path, Pair<Boolean, MutableMap<String, Boolean>>> = HashMap()
        pathMap.forEach { (path, clazzMap) ->
            clazzMap.forEach { (clazz, _) ->
                map.getOrPut(path) { Pair(expandedState[path]?.first ?: false, HashMap()) }.second[clazz] =
                    expandedState[path]?.second?.get(clazz) ?: false
            }
        }
        expandedState = map.mapValues { Pair(it.value.first, it.value.second.toSortedMap()) }.toSortedMap()
    }

    Box(modifier) {
        LazyColumn(state = state) {
            pathMap.forEach { (path, clazzMap) ->
                val pathIsOpen = expandedState[path]?.first

                item {
                    clickableText(
                        path.toString(),
                        5.dp,
                        if (pathIsOpen == true) IconMode.Open else IconMode.Closed,

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
                            clickableText(
                                clazz,
                                10.dp,
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
                                    clickableText(
                                        method,
                                        15.dp,
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

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(all = 4.dp),
            adapter = rememberScrollbarAdapter(
                scrollState = state,
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
fun clickableText(content: String, indentation: Dp, iconMode: IconMode, modifier: Modifier = Modifier, onCLick: () -> Unit) {
    Row(
        modifier.padding(all = 4.dp).padding(start = indentation),
        verticalAlignment = Alignment.Top,
    ) {
        when (iconMode) {
            IconMode.Open -> Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
            IconMode.Closed -> Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null)
            IconMode.Selected -> Icon(Icons.Rounded.RadioButtonChecked, contentDescription = null, Modifier.scale(0.5F))
            IconMode.Unselected -> Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null, Modifier.scale(0.5F))
        }

        Text(
            content,
            Modifier.onClick { onCLick() }.fillMaxWidth().offset(y = if (iconMode in arrayOf(IconMode.Selected, IconMode.Unselected)) 4.dp else 0.dp),
            style = TextStyle(textIndent = TextIndent(0.sp, 10.sp)),
        )
    }
}

enum class IconMode {
    Open,
    Closed,
    Selected,
    Unselected,
}
