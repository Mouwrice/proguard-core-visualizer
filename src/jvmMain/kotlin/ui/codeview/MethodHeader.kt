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
import data.CodeAttributeRecord
import ui.Colors

/**
 * Display the name of the current method and its parameters.
 */
@Composable
fun MethodHeader(codeAttribute: CodeAttributeRecord) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(Colors.LightGreen.value.copy(alpha = 0.2F)),
    ) {
        Text(
            "${codeAttribute.clazz}::${codeAttribute.method}",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Text(
            "parameters: ${codeAttribute.parameters.joinToString(", ")}",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Divider(
            color = Colors.DarkGreen.value,
            modifier = Modifier.padding(top = 8.dp).shadow(4.dp),
        )
    }
}
