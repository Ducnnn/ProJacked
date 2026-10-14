package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.MAX_WEIGHT_DECIMALS
import com.projacked.app.domain.model.MAX_WEIGHT_KG
import com.projacked.app.domain.model.SetInput
import com.projacked.app.domain.model.SetInputError
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Parses the logger's weight field, in kg (P7-6). Spaces at both ends are trimmed and empty text is 0.0.
 *
 * Both "." and "," are accepted as the decimal separator on purpose, whatever the device's locale, so nobody is
 * refused for typing their own. The text needs at least one digit and at most one separator; "22." (22.0) and
 * ".5" (0.5) are valid, so typing never shows an error halfway through a number. At most
 * [MAX_WEIGHT_DECIMALS] decimals and at most [MAX_WEIGHT_KG]. The value is parsed exactly with [BigDecimal],
 * so no locale is involved and "62.5" is exactly 62.5.
 */
class ParseWeight @Inject constructor() {
    operator fun invoke(text: String): SetInput<Double> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SetInput.Valid(0.0)

        val separators = trimmed.count { it == '.' || it == ',' }
        val digitCount = trimmed.count { it in '0'..'9' }
        if (separators > 1 || digitCount == 0 || digitCount + separators != trimmed.length) {
            return SetInput.Invalid(SetInputError.NOT_A_NUMBER)
        }

        val normalised = trimmed.replace(',', '.')
        val decimals = normalised.substringAfter('.', "")
        if (decimals.length > MAX_WEIGHT_DECIMALS) return SetInput.Invalid(SetInputError.TOO_MANY_DECIMALS)

        val value = BigDecimal(normalised.ifEmpty { "0" }.let { if (it.startsWith('.')) "0$it" else it })
        return if (value > BigDecimal.valueOf(MAX_WEIGHT_KG)) {
            SetInput.Invalid(SetInputError.TOO_LARGE)
        } else {
            SetInput.Valid(value.toDouble())
        }
    }
}
