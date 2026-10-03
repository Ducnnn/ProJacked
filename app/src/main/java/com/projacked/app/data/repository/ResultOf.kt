package com.projacked.app.data.repository

import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs [block] and wraps its outcome in a [Result]. Unlike `runCatching`, coroutine cancellation is rethrown
 * rather than reported as a failure.
 */
internal suspend inline fun <T> resultOf(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
