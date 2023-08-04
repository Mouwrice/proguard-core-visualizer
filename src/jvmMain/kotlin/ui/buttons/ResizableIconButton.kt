package ui.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * An [androidx.compose.material.IconButton] where you can specify the size.
 * The compose implementation is currently fixed to 48.dp, so we need to make our own.
 * https://github.com/androidx/androidx/blob/androidx-main/compose/material/material/src/commonMain/kotlin/androidx/compose/material/IconButton.kt
 */
@Composable
fun ResizableIconButton(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String,
    modifier: Modifier = Modifier,
    callBack: () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(
                role = Role.Button,
                interactionSource = MutableInteractionSource(),
                indication = rememberRipple(bounded = false, radius = 12.dp),
                onClick = callBack,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            tint = tint,
            contentDescription = contentDescription,
        )
    }
}
