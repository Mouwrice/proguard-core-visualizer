package ui.fileview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MethodDropdown(viewModel: DebuggerViewModel, modifier: Modifier = Modifier) {
    Box(modifier) {
        var expanded by remember { mutableStateOf(false) }
        val currentAttribute = viewModel.codeAttributes[viewModel.currentCodeAttribute]
        TooltipArea(tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "${currentAttribute.clazz}::${currentAttribute.method}",
                    modifier = Modifier.padding(10.dp),
                )
            }
        }) {
            OutlinedButton(
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
                shape = MaterialTheme.shapes.small,
                onClick = { expanded = true },
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${currentAttribute.clazz}::${currentAttribute.method}",
                        fontFamily = FontFamily.Monospace,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                    )
                    Icon(Icons.Default.ExpandMore, contentDescription = "Expand method dropdown")
                }
            }
        }

        DropdownMenu(expanded, { expanded = false }) {
            viewModel.codeAttributes.forEachIndexed { index, attribute ->
                TooltipArea(tooltip = {
                    Surface(
                        modifier = Modifier.shadow(4.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "${attribute.clazz}:${attribute.method}",
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }) {
                    DropdownMenuItem(
                        onClick = {
                            viewModel.selectCodeAttribute(index)
                            expanded = false
                        },
                    ) {
                        Text(
                            "${attribute.clazz}:${attribute.method}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
