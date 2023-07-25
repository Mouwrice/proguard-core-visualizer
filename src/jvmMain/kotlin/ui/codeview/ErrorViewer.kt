package ui.codeview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import data.ErrorRecord
import ui.Colors

/**
 * Display the exception that occurred during the evaluation of the current instruction.
 */
@Composable
fun ErrorViewer(error: ErrorRecord) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(Colors.Red.value.copy(alpha = 0.2F)),
    ) {
        Divider(
            color = Colors.Red.value,
            modifier = Modifier.shadow(4.dp),
        )
        Text(
            "error: ${error.message}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}
