package com.projacked.app.ui.screens.dayconstructor

import androidx.annotation.StringRes
import com.projacked.app.R

/** One choice in the colour picker: the stored hex and the spoken name. */
data class PaletteColor(val hex: String, @param:StringRes val nameRes: Int)

/**
 * The 16 colours a new workout can have: the 600 shades of the old picker's Material list, without white (an
 * invisible ring on the plan) and without the rest salmon. In grid order, row by row of four.
 *
 * These are stored values, not UI colours: they are drawn through `parseTemplateColor`, so they live here and not
 * in `Color.kt` (P6-8). Hex is lowercase `#rrggbb`, the format the old picker stored.
 */
val TemplatePalette: List<PaletteColor> = listOf(
    PaletteColor("#e53935", R.string.color_red),
    PaletteColor("#d81b60", R.string.color_pink),
    PaletteColor("#8e24aa", R.string.color_purple),
    PaletteColor("#5e35b1", R.string.color_deep_purple),
    PaletteColor("#3949ab", R.string.color_indigo),
    PaletteColor("#1e88e5", R.string.color_blue),
    PaletteColor("#039be5", R.string.color_light_blue),
    PaletteColor("#00acc1", R.string.color_cyan),
    PaletteColor("#00897b", R.string.color_teal),
    PaletteColor("#43a047", R.string.color_green),
    PaletteColor("#7cb342", R.string.color_light_green),
    PaletteColor("#c0ca33", R.string.color_lime),
    PaletteColor("#ffb300", R.string.color_amber),
    PaletteColor("#fb8c00", R.string.color_orange),
    PaletteColor("#6d4c41", R.string.color_brown),
    PaletteColor("#546e7a", R.string.color_blue_grey),
)

/**
 * The first palette colour no other workout uses, comparing ignoring case and surrounding spaces. Colours outside
 * the palette (white, old free-picked colours) are ignored. When all 16 are used, it's the first one.
 */
fun defaultTemplateColor(usedColors: Collection<String>): String {
    val used = usedColors.map { it.trim().lowercase() }.toSet()
    return (TemplatePalette.firstOrNull { it.hex !in used } ?: TemplatePalette.first()).hex
}
