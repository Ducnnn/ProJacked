package com.projacked.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val Default = Typography()

// The old app used sans-serif-black (the heaviest Roboto weight) for titles and buttons.
// Keep Material 3's type scale (sizes, line heights) and only make headings and labels heavy.
val Typography = Typography(
    displayLarge = Default.displayLarge.copy(fontWeight = FontWeight.Black),
    displayMedium = Default.displayMedium.copy(fontWeight = FontWeight.Black),
    displaySmall = Default.displaySmall.copy(fontWeight = FontWeight.Black),
    headlineLarge = Default.headlineLarge.copy(fontWeight = FontWeight.Black),
    headlineMedium = Default.headlineMedium.copy(fontWeight = FontWeight.Black),
    headlineSmall = Default.headlineSmall.copy(fontWeight = FontWeight.Black),
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.Black),
    titleMedium = Default.titleMedium.copy(fontWeight = FontWeight.Black),
    titleSmall = Default.titleSmall.copy(fontWeight = FontWeight.Black),
    labelLarge = Default.labelLarge.copy(fontWeight = FontWeight.Black),
)
