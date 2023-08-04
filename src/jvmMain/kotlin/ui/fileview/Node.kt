package ui.fileview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.buttons.ResizableIconButton

/**
 * The state of a node in the tree view needed to render it.
 */
data class NodeState(
    val content: String,
    val indentation: Dp,
    val iconMode: IconMode,
    val modifier: Modifier = Modifier,
    val closeCallback: (() -> Unit)? = null,
    val onClick: () -> Unit,
    val query: Regex,
)

/**
 * A node in the tree view.
 * This is the basic building block of the tree view, and the UI for a single node.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Node(
    state: NodeState,
) {
    val content = buildAnnotatedString {
        append(state.content)
        state.query.findAll(state.content).forEach {
            if (it.range.first < it.range.last) {
                addStyle(
                    SpanStyle(
                        background = MaterialTheme.colorScheme.tertiaryContainer,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    it.range.first,
                    it.range.last + 1,
                )
            }
        }
    }
    val backgroundColor = if (state.iconMode == IconMode.Selected) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        state.modifier.padding(start = state.indentation).background(backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 8.dp).onClick(onClick = state.onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state.iconMode) {
            IconMode.Open -> Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Drawer handle is open")
            IconMode.Closed -> Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Drawer handle is closed",
            )

            IconMode.Selected -> Icon(
                Icons.Rounded.RadioButtonChecked,
                contentDescription = "Icon checked",
                Modifier.scale(0.5F),
                tint = MaterialTheme.colorScheme.primary,
            )

            IconMode.Unselected -> Icon(
                Icons.Rounded.RadioButtonUnchecked,
                contentDescription = "Icon unchecked",
                Modifier.scale(0.5F),
            )
        }

        TooltipArea(tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    content,
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }) {
            Text(
                content,
                style = TextStyle(textIndent = TextIndent(0.sp, 12.sp)),
            )
        }

        if (state.closeCallback != null) {
            println(state.closeCallback)
            ResizableIconButton(
                icon = Icons.Default.Close,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = "Close file",
                modifier = Modifier.padding(horizontal = 8.dp).size(16.dp),
            ) {
                state.closeCallback
            }
        }
    }
}
