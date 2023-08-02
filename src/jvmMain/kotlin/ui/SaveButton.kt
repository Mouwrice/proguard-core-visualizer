package ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import data.LoadUtil
import viewmodel.FilesViewModel
import java.awt.FileDialog
import java.io.File

@Composable
fun SaveButton(viewModel: FilesViewModel) {
    OutlinedButton(
        enabled = viewModel.files[viewModel.curPath]?.first != null,
        onClick = {
            viewModel.files[viewModel.curPath]?.first?.let { classPool ->
                viewModel.curClazz?.let { clazz ->
                    viewModel.curMethod?.let { method ->
                        val dialog = FileDialog(ComposeWindow(), "Export evaluation", FileDialog.SAVE)
                        dialog.file = "$clazz.$method.${viewModel.valueFactoryOption}:.json"
                            .replace("[^a-zA-Z0-9-_.]".toRegex(), "_")
                            .replace("_+".toRegex(), "_")
                        dialog.isVisible = true

                        var file = dialog.file
                        if (file != null) {
                            if (!file.endsWith(".json")) {
                                file += ".json"
                            }
                            val jsonString = LoadUtil.evaluateMethod(classPool, clazz, method, viewModel.valueFactoryOption)
                            File(dialog.directory, file).writeText(jsonString)
                        }
                    }
                }
            }
        },
    ) {
        Icon(Icons.Rounded.Save, contentDescription = "Save configuration")
    }
}
