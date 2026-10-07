package com.projacked.app.ui.screens.plan.components

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Formatter
import java.util.Locale

/** A single date in the device locale, e.g. "Mon, 6 Oct". */
fun formatPlanDate(date: LocalDate, locale: Locale): String = date.format(formatterFor("EEEdMMM", locale))

/**
 * The week strip's title: "Oct 2026", "Sep – Oct 2026" when the week spans two months, and
 * "Dec 2026 – Jan 2027" across years. The week is the 7 days from [weekStart].
 */
fun formatWeekTitle(weekStart: LocalDate, locale: Locale): String {
    val weekEnd = weekStart.plusDays(6)
    val monthYear = formatterFor("MMMyyyy", locale)
    return when {
        weekStart.year != weekEnd.year -> "${weekStart.format(monthYear)} – ${weekEnd.format(monthYear)}"
        weekStart.month != weekEnd.month ->
            "${weekStart.format(formatterFor("MMM", locale))} – ${weekEnd.format(monthYear)}"
        else -> weekStart.format(monthYear)
    }
}

/** The 7 days from [weekStart] as a short localized range without the year, e.g. "6–12 Oct". */
fun formatWeekRange(context: Context, weekStart: LocalDate, locale: Locale): String {
    val start = weekStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    // Midday, because DateUtils treats a range that ends at midnight as ending the day before.
    val end = weekStart.plusDays(6).atStartOfDay(ZoneOffset.UTC).plusHours(12).toInstant().toEpochMilli()
    return DateUtils.formatDateRange(
        context,
        Formatter(StringBuilder(), locale),
        start,
        end,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_ABBREV_MONTH,
        "UTC",
    ).toString()
}

private fun formatterFor(skeleton: String, locale: Locale): DateTimeFormatter =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
