package ui.jbcEditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Editor() {
    var text by remember {
        mutableStateOf(
            "public class HelloWorld {\n" +
                "    public static void main(final String[] args) {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}\n",
        )
    }

    TextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxSize(),
    )
}
