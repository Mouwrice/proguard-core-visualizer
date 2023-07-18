import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FileView(fileName: String, lines: List<String>, currentLine: Int, onFilePickerClick: () -> Unit) {
    Column(Modifier.fillMaxHeight().fillMaxWidth(0.5f)) {
        Column(Modifier.fillMaxSize()) {
            Button(onClick = onFilePickerClick) {
                Text("Open java bytecode file")
            }

            Text(fileName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            LazyColumn(
                Modifier.fillMaxSize().border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium,
                ),
                contentPadding = PaddingValues(8.dp),
            ) {
                itemsIndexed(lines) { index: Int, line: String ->
                    if (index == currentLine) {
                        Text(line, Modifier.background(MaterialTheme.colorScheme.primaryContainer))
                    } else {
                        Text(line, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
