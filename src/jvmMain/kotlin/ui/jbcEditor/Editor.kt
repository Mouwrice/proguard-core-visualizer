package ui.jbcEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import data.FileTypes
import viewmodel.FilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Editor(viewModel: FilesViewModel) {
    var text by remember {
        mutableStateOf(
            "import java.lang.String;\n" +
                "import java.lang.System;\n" +
                "import java.io.PrintStream;\n" +
                "version 12;\n" +
                "public class MyClass {\n" +
                "    public static void main(final String[] args) {\n" +
                "        getstatic System#PrintStream out\n" +
                "        ldc \"Hello World!\"\n" +
                "        invokevirtual PrintStream#void println(String)\n" +
                "        return\n" +
                "    }\n" +
                "}",
        )
    }

    Column {
        Button(
            onClick = {
                viewModel.loadScratch(text, FileTypes.JBC)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("load it")
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxSize(),
        )
    }
}