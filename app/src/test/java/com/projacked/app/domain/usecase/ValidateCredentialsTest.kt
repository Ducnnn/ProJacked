package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.CredentialsError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCredentialsTest {

    private val validate = ValidateCredentials()

    @Test
    fun `valid email and password pass`() {
        assertTrue(validate("gym.user+1@example.co.uk", "Str0ng!pass").isEmpty())
    }

    @Test
    fun `every allowed symbol is accepted`() {
        assertTrue(validate("a@b.com", """~`!@#$%^&*()_-+={[}]|\:;"'<,>.?/""").isEmpty())
    }

    @Test
    fun `malformed emails are rejected`() {
        listOf("", "plainaddress", "no-at.example.com", "user@", "user@domain", "user@.com", "a b@c.com").forEach {
            assertEquals("email: $it", setOf(CredentialsError.INVALID_EMAIL), validate(it, "secret1"))
        }
    }

    @Test
    fun `password shorter than 6 characters is rejected`() {
        assertEquals(setOf(CredentialsError.PASSWORD_TOO_SHORT), validate("a@b.com", "12345"))
    }

    @Test
    fun `exactly 6 characters is enough`() {
        assertTrue(validate("a@b.com", "123456").isEmpty())
    }

    @Test
    fun `spaces and non-English letters are rejected`() {
        assertEquals(setOf(CredentialsError.PASSWORD_INVALID_CHARACTERS), validate("a@b.com", "pass word"))
        assertEquals(setOf(CredentialsError.PASSWORD_INVALID_CHARACTERS), validate("a@b.com", "пароль123"))
    }

    @Test
    fun `all problems are reported together`() {
        assertEquals(
            setOf(
                CredentialsError.INVALID_EMAIL,
                CredentialsError.PASSWORD_TOO_SHORT,
                CredentialsError.PASSWORD_INVALID_CHARACTERS,
            ),
            validate("nope", "a b"),
        )
    }
}
