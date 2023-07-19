package ui

import androidx.compose.ui.graphics.Color

/**
 * Custom colors that can be used as highlight colors.
 * These do not adapt to the current theme (light / dark) so only to be used
 * for highlighting.
 */
enum class Colors(val value: Color) {
    LightGreen(Color.hsv(137F, 0.49F, 0.97F)),
    Green(Color.hsv(137F, 0.59F, 0.87F)),
    DarkGreen(Color.hsv(137F, 0.69F, 0.57F)),
    Red(Color.hsv(0F, 0.62F, 0.99F)),
}
