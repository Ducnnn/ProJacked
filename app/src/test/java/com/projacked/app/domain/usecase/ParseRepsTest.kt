package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.SetInput
import com.projacked.app.domain.model.SetInputError
import org.junit.Assert.assertEquals
import org.junit.Test

class ParseRepsTest {
    private val parse = ParseReps()

    @Test
    fun `empty and blank text is zero`() {
        assertEquals(SetInput.Valid(0), parse(""))
        assertEquals(SetInput.Valid(0), parse("  "))
    }

    @Test
    fun `whole numbers are valid, with spaces trimmed and leading zeros allowed`() {
        assertEquals(SetInput.Valid(12), parse("12"))
        assertEquals(SetInput.Valid(8), parse(" 8 "))
        assertEquals(SetInput.Valid(7), parse("007"))
        assertEquals(SetInput.Valid(999), parse("999"))
    }

    @Test
    fun `more than 999 is too large, without crashing on long input`() {
        assertEquals(SetInput.Invalid(SetInputError.TOO_LARGE), parse("1000"))
        assertEquals(SetInput.Invalid(SetInputError.TOO_LARGE), parse("99999999999999999999"))
    }

    @Test
    fun `anything but digits is not a whole number`() {
        listOf("-1", "1.5", "1,5", "abc", "1 0").forEach {
            assertEquals(it, SetInput.Invalid(SetInputError.NOT_WHOLE_NUMBER), parse(it))
        }
    }
}
