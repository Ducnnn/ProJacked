package com.projacked.app.domain.model

/** The longest name the Day constructor accepts for a new template, counted in Unicode code points. */
const val MAX_TEMPLATE_NAME_LENGTH = 40

private val ReservedIdPattern = Regex("__.*__")

/**
 * Whether [name] can be a template's Firestore document id (a template's id is its name, decision D7): not blank,
 * no `/`, not `.` or `..`, and not of the form `__x__` (Firestore reserves those ids).
 * It does not trim or limit the length, so old templates with long names stay usable and deletable.
 */
fun isAllowedTemplateDocumentId(name: String): Boolean =
    name.isNotBlank() && '/' !in name && name != "." && name != ".." && !ReservedIdPattern.matches(name)
