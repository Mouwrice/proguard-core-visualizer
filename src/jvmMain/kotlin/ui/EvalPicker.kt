package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import data.LoadUtil
import viewmodel.FilesViewModel

@Composable
fun EvalPicker(viewModel: FilesViewModel) {
    Box() {
        var menuOpened by remember { mutableStateOf(false) }
        val buttonEnabled = viewModel.curPath?.classPool != null
        OutlinedButton(
            enabled = buttonEnabled,
            onClick = { menuOpened = true },
        ) {
            Row() {
                if (menuOpened) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Drawer handle is open")
                }
                if (!menuOpened) {
                    Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Drawer handle is closed")
                }
                Text(if (buttonEnabled) viewModel.valueFactoryOption.toString() else "Cannot reevaluate")
            }
        }
        DropdownMenu(menuOpened, { menuOpened = false }) {
            LoadUtil.ValueFactoryOption.entries.sorted().forEach { evalOption ->
                DropdownMenuItem(
                    {
                        Text(evalOption.toString())
                    },
                    onClick = {
                        menuOpened = false
                        viewModel.setEvalFactoryAndUpdate(evalOption)
                    },
                )
            }
        }
    }
}
