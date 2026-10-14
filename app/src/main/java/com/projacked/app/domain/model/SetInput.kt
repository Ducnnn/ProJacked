package com.projacked.app.domain.model

/** The most reps a set can hold (P7-6). */
const val MAX_REPS = 999

/** The heaviest weight a set can hold, in kg (P7-6). */
const val MAX_WEIGHT_KG = 999.99

/** The most digits after the separator in a weight. */
const val MAX_WEIGHT_DECIMALS = 2

/** Why the text typed into a reps or weight field was refused. */
enum class SetInputError {
    /** Reps: anything other than digits. */
    NOT_WHOLE_NUMBER,

    /** Weight: not digits with at most one separator. */
    NOT_A_NUMBER,

    /** Above [MAX_REPS] or [MAX_WEIGHT_KG]. */
    TOO_LARGE,

    /** Weight: more than [MAX_WEIGHT_DECIMALS] digits after the separator. */
    TOO_MANY_DECIMALS,
}

/** The outcome of parsing a reps or weight field: a value, or the reason it was refused. */
sealed interface SetInput<out T> {
    data class Valid<T>(val value: T) : SetInput<T>
    data class Invalid(val error: SetInputError) : SetInput<Nothing>
}
