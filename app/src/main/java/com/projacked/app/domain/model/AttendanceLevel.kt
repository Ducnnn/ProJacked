package com.projacked.app.domain.model

/** How a day shows on the attendance heatmap (GitHub contribution-graph style). */
enum class AttendanceLevel {
    /** No exercises on that date (rest day or nothing assigned). */
    NOTHING_PLANNED,

    /** Exercises were planned for today or a past date, and none were finished. */
    MISSED,

    /** 1–25% of the exercises finished. */
    LOW,

    /** 26–50% finished. */
    MEDIUM,

    /** 51–75% finished. */
    HIGH,

    /** 76–100% finished. */
    FULL,

    /** A future date with exercises planned. */
    PLANNED,
}
