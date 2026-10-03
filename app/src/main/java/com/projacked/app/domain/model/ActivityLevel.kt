package com.projacked.app.domain.model

/** How active the user is; drives the calorie multiplier in [com.projacked.app.domain.usecase.CalculateMacros]. */
enum class ActivityLevel {
    /** Little or no exercise. */
    SEDENTARY,

    /** 1–3 days a week. */
    LIGHT,

    /** 3–5 days a week. */
    AVERAGE,

    /** 6–7 days a week. */
    ACTIVE,
}
