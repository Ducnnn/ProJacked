package com.projacked.app.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Turns a stored template colour into a [Color]. Accepts `#RRGGBB` and `#AARRGGBB`, in either case; anything else
 * gives null. This parses stored data (a user's template colour), so it isn't a UI colour and doesn't belong in
 * `Color.kt`. It avoids `android.graphics.Color.parseColor` so it runs in JVM unit tests.
 */
fun parseTemplateColor(hex: String): Color? {
    if (!hex.startsWith('#')) return null
    val digits = hex.substring(1)
    if (digits.length != 6 && digits.length != 8 || !digits.all { it.isHexDigit() }) return null
    val value = digits.toLong(16)
    return if (digits.length == 6) Color(0xFF000000L or value) else Color(value)
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
