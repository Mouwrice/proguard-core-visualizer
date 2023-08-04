package ui.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import data.LoadUtil
import viewmodel.FilesViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun SaveButton(viewModel: FilesViewModel) {
    var isOpen by remember { mutableStateOf(false) }

    if (isOpen) {
        viewModel.curPath?.classPool?.let { classPool ->
            viewModel.curClazz?.let { clazz ->
                viewModel.curMethod?.let { method ->
                    var fileName = "$clazz.$method.${viewModel.valueFactoryType}:.json"
                        .replace("[^a-zA-Z0-9-_.]".toRegex(), "_")
                        .replace("_+".toRegex(), "_")

                    FileDialog(
                        fileName = fileName,
                        onCloseRequest = { directory, file ->
                            isOpen = false
                            if (directory != null && file != null) {
                                if (!file.endsWith(".json")) {
                                    fileName += ".json"
                                }
                                val jsonString =
                                    LoadUtil.evaluateMethod(classPool, clazz.name, method.name, viewModel.valueFactoryType)
                                File(directory, file).writeText(jsonString)
                            }
                        },
                    )
                }
            }
        }
    }

    OutlinedButton(
        enabled = viewModel.curPath?.classPool != null,
        onClick = { isOpen = true },
    ) {
        Icon(Icons.Rounded.Save, contentDescription = "Save configuration")
    }
}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (directory: String?, file: String?) -> Unit,
    fileName: String,
) = AwtWindow(
    create = {
        val dialog = object : FileDialog(parent, "Export evaluation", SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory, file)
                }
            }
        }
        dialog.file = fileName
        dialog
    },
    dispose = FileDialog::dispose,
)
