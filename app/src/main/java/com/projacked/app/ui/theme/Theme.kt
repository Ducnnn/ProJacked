package com.projacked.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

// The old palette mapped onto Material 3 colour roles. Components read roles (primary, primaryContainer, …),
// so modernising the look later means changing this mapping, not the screens.
private val BrandLightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = White,
    secondary = Purple,
    onSecondary = White,
    tertiary = Lilac,
    onTertiary = White,
    primaryContainer = Periwinkle,   // cards
    onPrimaryContainer = White,
    secondaryContainer = Lilac,      // accent tiles and headers
    onSecondaryContainer = White,
    background = SkyBlue,
    onBackground = Black,
    surface = White,                 // dialogs, sheets, text fields keep a readable light surface
    onSurface = Black,
    // outline stays Material's default so text-field borders remain visible; the white card border is
    // ProJackedTheme.extendedColors.cardBorder.
    error = ErrorRed,
    onError = White,
)

/**
 * ProJacked's theme. Keeps the GymMaximum palette for now (decision D5).
 *
 * Ready for a later Material 3 redesign:
 * - [darkTheme] is wired through, but there's no dark palette yet, so it currently falls back to the light one.
 * - [dynamicColor] (wallpaper colours, Android 12+) is off by default because it would replace the brand palette.
 */
@Composable
fun ProJackedTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // TODO(D5 redesign): add a dark brand palette and use it when darkTheme is true.
        else -> BrandLightColorScheme
    }

    CompositionLocalProvider(LocalExtendedColors provides LightExtendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Access to theme values that Material 3 has no slot for, e.g. `ProJackedTheme.extendedColors.protein`. */
object ProJackedTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
