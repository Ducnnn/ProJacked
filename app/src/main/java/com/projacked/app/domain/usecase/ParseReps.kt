package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.MAX_REPS
import com.projacked.app.domain.model.SetInput
import com.projacked.app.domain.model.SetInputError
import javax.inject.Inject

/**
 * Parses the logger's reps field (P7-6). Spaces at both ends are trimmed and empty text is 0. Only ASCII digits
 * are allowed ("-1", "1.5", "abc" and "1 0" are refused); leading zeros are fine. More than [MAX_REPS] is refused.
 */
class ParseReps @Inject constructor() {
    operator fun invoke(text: String): SetInput<Int> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SetInput.Valid(0)
        if (!trimmed.all { it in '0'..'9' }) return SetInput.Invalid(SetInputError.NOT_WHOLE_NUMBER)
        // Dropping leading zeros keeps a long run of digits from overflowing: anything over 3 digits is too large.
        val digits = trimmed.trimStart('0')
        if (digits.length > MAX_REPS.toString().length) return SetInput.Invalid(SetInputError.TOO_LARGE)
        val value = if (digits.isEmpty()) 0 else digits.toInt()
        return if (value > MAX_REPS) SetInput.Invalid(SetInputError.TOO_LARGE) else SetInput.Valid(value)
    }
}
