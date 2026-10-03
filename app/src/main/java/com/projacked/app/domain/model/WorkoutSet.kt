package com.projacked.app.domain.model

/** One set of an exercise. Weight is in kilograms and may be fractional (e.g. 22.5). */
data class WorkoutSet(
    val reps: Int = 0,
    val weightKg: Double = 0.0,
) {
    /** True when nothing has been entered yet (both values zero). */
    val isEmpty: Boolean get() = reps == 0 && weightKg == 0.0
}
