package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import viewmodel.FilesViewModel

@Composable
fun ExceptionPopup(viewModel: FilesViewModel, modifier: Modifier = Modifier) {
    val scrollState = rememberLazyListState()

    Box(
        modifier = modifier.fillMaxSize(0.5f)
            .border(2.dp, MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("An exception occurred.")
            Text(viewModel.exception?.message ?: "")
            Box {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxHeight(0.7f)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                ) {
                    viewModel.exception?.stackTrace?.forEach {
                        item {
                            Text(
                                it.toString(),
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(0.7f).padding(all = 4.dp).align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(
                        scrollState = scrollState,
                    ),
                    style = defaultScrollbarStyle().copy(
                        unhoverColor = MaterialTheme.colorScheme.outline,
                        hoverColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = {
                        viewModel.exception = null
                    },
                ) {
                    Text("Close")
                }
            }
        }
    }
}
