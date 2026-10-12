package com.projacked.app.domain.usecase

import com.projacked.app.domain.model.MAX_TEMPLATE_NAME_LENGTH
import com.projacked.app.domain.model.isAllowedTemplateDocumentId
import javax.inject.Inject

/** Why a new template's name was refused. */
enum class TemplateNameError {
    BLANK,
    CONTAINS_SLASH,

    /** `.`, `..` and `__x__`: Firestore reserves them as document ids. */
    RESERVED,

    /** A safety net: the Day constructor's field already stops at [MAX_TEMPLATE_NAME_LENGTH]. */
    TOO_LONG,
}

/** The outcome of [ValidateTemplateName]: the trimmed name, or the reason it was refused. */
sealed interface TemplateNameResult {
    data class Valid(val name: String) : TemplateNameResult
    data class Invalid(val error: TemplateNameError) : TemplateNameResult
}

/**
 * Rules for the name of a new template (P6-6, D7). Spaces at both ends are trimmed. Checks run in this order:
 * blank, contains `/`, reserved, too long. "Rest" is a valid name.
 */
class ValidateTemplateName @Inject constructor() {
    operator fun invoke(raw: String): TemplateNameResult {
        val name = raw.trim()
        val error = when {
            name.isEmpty() -> TemplateNameError.BLANK
            '/' in name -> TemplateNameError.CONTAINS_SLASH
            !isAllowedTemplateDocumentId(name) -> TemplateNameError.RESERVED
            name.codePointCount(0, name.length) > MAX_TEMPLATE_NAME_LENGTH -> TemplateNameError.TOO_LONG
            else -> return TemplateNameResult.Valid(name)
        }
        return TemplateNameResult.Invalid(error)
    }
}
