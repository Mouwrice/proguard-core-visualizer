package ui.codeview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.InstructionRecord
import ui.Colors

/**
 * Display the current instruction. Highlight it if it is the current one.
 */
@Composable
fun InstructionViewer(instruction: InstructionRecord, maxOffsetLength: Int, color: Color, inCatch: Boolean) {
    val dividerColor = if (inCatch) Colors.Red.value else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        Modifier.fillMaxWidth().background(color).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pad with whitespaces to align the offsets
        // Compose seems to trim the whitespaces, so we use a special whitespace character
        val offset = instruction.offset.toString().padStart(maxOffsetLength, ' ')
        Text(
            offset,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
            textAlign = TextAlign.End,
        )
        Divider(
            color = dividerColor,
            modifier = Modifier
                .height(20.dp)
                .width(1.dp),
        )

        Text(
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 16.dp),
            text = buildAnnotatedString {
                append(instruction.instruction)
                if (instruction.instruction.contains(' ')) {
                    addStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold), instruction.instruction.indexOf(' '), instruction.instruction.length)
                }
            }
        )
    }
}
