package com.projacked.app.domain.model

/** Thrown or returned when an operation needs a signed-in user and there is none. */
class NotSignedInException : IllegalStateException("No user is signed in")
