package com.projacked.app.ui.screens.home.components

import java.text.NumberFormat
import java.util.Locale

/**
 * A weight for display: no decimals when whole (60), otherwise up to two (22.5), with the locale's decimal
 * separator (22,5 in German). No thousands grouping.
 */
fun formatWeight(kg: Double, locale: Locale = Locale.getDefault()): String =
    NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
        isGroupingUsed = false
    }.format(kg)
