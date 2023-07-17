import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun Debugger() {
    Column(Modifier.fillMaxSize()) {
        Category("Variables", maxHeight = 0.3F) {
            LazyColumn(
                Modifier.fillMaxSize().border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium
                ), contentPadding = PaddingValues(8.dp)
            ) {
                item {
                    Text("Variables go here...")
                }
            }
        }

        Category("Stack") {
            LazyColumn(
                Modifier.fillMaxSize().border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium
                ), contentPadding = PaddingValues(8.dp)
            ) {
                item {
                    Text("Items on the stack go here...")
                }
                item {
                    Text("Another item on the stack goes here...")
                }
            }
        }
    }
}
