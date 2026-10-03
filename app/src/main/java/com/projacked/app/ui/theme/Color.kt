package com.projacked.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand palette carried over from the XML app (GymMaximum). Raw values live only in this file:
// screens use MaterialTheme.colorScheme roles or ProJackedTheme.extendedColors, never these directly,
// so a later Material 3 redesign only has to change the mapping in Theme.kt.

internal val SkyBlue = Color(0xFF78B0F0)        // screen background
internal val RoyalBlue = Color(0xFF2B47CE)      // primary buttons
internal val Purple = Color(0xFFAB3BBC)         // main actions
internal val Periwinkle = Color(0xFF9DA5E0)     // cards (old layout_bg)
internal val Lilac = Color(0xFFC481CC)          // accent tiles and headers (old layout_bgg)
internal val CompletedGrey = Color(0xFF888789)  // finished exercise card (old layout_bgggg)
internal val RestSalmon = Color(0xFFFFA9A3)     // default rest-day colour

internal val White = Color(0xFFFFFFFF)
internal val Black = Color(0xFF000000)
internal val ErrorRed = Color(0xFFB3261E)

// Macro colours: the Home pie chart's set, now used everywhere (the old Meals screen used different ones).
internal val ProteinOrange = Color(0xFFFFA726)
internal val FatsGreen = Color(0xFF66BB6A)
internal val CarbsRed = Color(0xFFEF5350)

// Attendance heatmap (GitHub contribution-graph scale).
internal val AttendanceNoneGrey = Color(0xFFEBEDF0)
internal val AttendanceMissedRed = Color(0xFFF65A5A)
internal val AttendanceGreen1 = Color(0xFF9BE9A8)
internal val AttendanceGreen2 = Color(0xFF40C463)
internal val AttendanceGreen3 = Color(0xFF30A14E)
internal val AttendanceGreen4 = Color(0xFF216E39)
internal val AttendancePlannedBlue = Color(0xFF2B59FF)

/** App-specific colours that have no Material 3 colour role. */
@Immutable
data class ExtendedColors(
    val cardBorder: Color,
    val exerciseCompleted: Color,
    val restDay: Color,
    val protein: Color,
    val fats: Color,
    val carbs: Color,
    val attendanceNone: Color,
    val attendanceMissed: Color,
    val attendanceLow: Color,
    val attendanceMedium: Color,
    val attendanceHigh: Color,
    val attendanceFull: Color,
    val attendancePlanned: Color,
)

internal val LightExtendedColors = ExtendedColors(
    cardBorder = White,
    exerciseCompleted = CompletedGrey,
    restDay = RestSalmon,
    protein = ProteinOrange,
    fats = FatsGreen,
    carbs = CarbsRed,
    attendanceNone = AttendanceNoneGrey,
    attendanceMissed = AttendanceMissedRed,
    attendanceLow = AttendanceGreen1,
    attendanceMedium = AttendanceGreen2,
    attendanceHigh = AttendanceGreen3,
    attendanceFull = AttendanceGreen4,
    attendancePlanned = AttendancePlannedBlue,
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
