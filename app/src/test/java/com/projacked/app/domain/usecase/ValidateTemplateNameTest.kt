package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.isAllowedTemplateDocumentId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTemplateNameTest {
    private val validate = ValidateTemplateName()

    private fun valid(raw: String) = TemplateNameResult.Valid(raw)
    private fun invalid(error: TemplateNameError) = TemplateNameResult.Invalid(error)

    @Test
    fun `a name is valid and trimmed`() {
        assertEquals(valid("Push"), validate("Push"))
        assertEquals(valid("Push"), validate("  Push  "))
        assertEquals(valid("Rest"), validate("Rest"))
    }

    @Test
    fun `blank names are refused`() {
        assertEquals(invalid(TemplateNameError.BLANK), validate(""))
        assertEquals(invalid(TemplateNameError.BLANK), validate("   "))
    }

    @Test
    fun `a slash is refused`() {
        assertEquals(invalid(TemplateNameError.CONTAINS_SLASH), validate("Push/Pull"))
    }

    @Test
    fun `reserved ids are refused`() {
        for (name in listOf(".", "..", "__x__", "____")) {
            assertEquals(name, invalid(TemplateNameError.RESERVED), validate(name))
        }
        assertEquals(valid("__"), validate("__"))
        assertEquals(valid("_x_"), validate("_x_"))
    }

    @Test
    fun `length is counted in code points`() {
        assertEquals(valid("a".repeat(40)), validate("a".repeat(40)))
        assertEquals(invalid(TemplateNameError.TOO_LONG), validate("a".repeat(41)))
        val emoji = "💪".repeat(40)
        assertEquals(valid(emoji), validate(emoji))
    }

    @Test
    fun `document id rule keeps old long and padded names usable`() {
        assertTrue(isAllowedTemplateDocumentId("a".repeat(60)))
        assertTrue(isAllowedTemplateDocumentId("Push "))
        assertFalse(isAllowedTemplateDocumentId("__x__"))
        assertFalse(isAllowedTemplateDocumentId("a/b"))
        assertFalse(isAllowedTemplateDocumentId("."))
        assertFalse(isAllowedTemplateDocumentId(" "))
    }
}
