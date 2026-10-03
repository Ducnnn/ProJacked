package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.CredentialsError
import javax.inject.Inject

/**
 * Sign-up checks done on the device, same rules as the old app: a valid email address, and a password of at
 * least 6 characters using only English letters, digits and common symbols.
 */
class ValidateCredentials @Inject constructor() {

    /** Returns every problem found; an empty set means the credentials are valid. */
    operator fun invoke(email: String, password: String): Set<CredentialsError> = buildSet {
        if (!EMAIL_PATTERN.matches(email)) add(CredentialsError.INVALID_EMAIL)
        if (password.length < MIN_PASSWORD_LENGTH) add(CredentialsError.PASSWORD_TOO_SHORT)
        if (password.any { it !in ALLOWED_PASSWORD_CHARACTERS }) add(CredentialsError.PASSWORD_INVALID_CHARACTERS)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6

        const val ALLOWED_PASSWORD_CHARACTERS =
            """ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890~`!@#$%^&*()_-+={[}]|\:;"'<,>.?/"""

        // Same pattern as android.util.Patterns.EMAIL_ADDRESS, which the old app used; copied here so the
        // domain layer stays free of Android.
        val EMAIL_PATTERN = Regex(
            """[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""",
        )
    }
}
