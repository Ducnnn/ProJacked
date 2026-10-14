package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.SetInput
import com.projacked.app.domain.model.SetInputError
import org.junit.Assert.assertEquals
import org.junit.Test

class ParseWeightTest {
    private val parse = ParseWeight()

    @Test
    fun `empty text is zero`() {
        assertEquals(SetInput.Valid(0.0), parse(""))
    }

    @Test
    fun `valid weights with either separator`() {
        assertEquals(SetInput.Valid(60.0), parse("60"))
        assertEquals(SetInput.Valid(62.5), parse("62.5"))
        assertEquals(SetInput.Valid(62.5), parse("62,5"))
        assertEquals(SetInput.Valid(22.0), parse("22."))
        assertEquals(SetInput.Valid(0.5), parse(".5"))
        assertEquals(SetInput.Valid(0.25), parse("0.25"))
        assertEquals(SetInput.Valid(999.99), parse("999.99"))
    }

    @Test
    fun `more than 999_99 is too large, without crashing on long input`() {
        assertEquals(SetInput.Invalid(SetInputError.TOO_LARGE), parse("1000"))
        assertEquals(SetInput.Invalid(SetInputError.TOO_LARGE), parse("9".repeat(40)))
    }

    @Test
    fun `more than two decimals`() {
        assertEquals(SetInput.Invalid(SetInputError.TOO_MANY_DECIMALS), parse("22.555"))
        assertEquals(SetInput.Invalid(SetInputError.TOO_MANY_DECIMALS), parse("999.995"))
    }

    @Test
    fun `not a number`() {
        listOf(".", ",", "1.2.3", "1,2.3", "abc", "-5", "1 000").forEach {
            assertEquals(it, SetInput.Invalid(SetInputError.NOT_A_NUMBER), parse(it))
        }
    }
}
