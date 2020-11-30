package no.nav.melosys.soknadmottak.common

import kotlinx.coroutines.delay
import mu.KotlinLogging
import no.nav.melosys.soknadmottak.metrics.Metrics
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger { }

internal suspend fun <T> retry(
    callName: String,
    attempts: Int = DEFAULT_RETRY_ATTEMPTS,
    initialDelay: Long = 100L,
    maxDelay: Long = 1000L,
    vararg illegalExceptions: KClass<out Throwable> = arrayOf(),
    block: suspend () -> T
): T {
    Metrics.networkCallFailuresCounter.labels(callName)
    var currentDelay = initialDelay
    repeat(attempts - 1) { attempt ->
        try {
            return timed(callName) { block() }
        } catch (e: Throwable) {
            Metrics.networkCallFailuresCounter.labels(callName).inc()
            // Any exception deemed to be client/user error should be propagated immediately rather than retried
            // Equivalently for HTTP client calls resulting in status codes between 400 and 499.
            if (illegalExceptions.any { it.isInstance(e) }) {
                countAndRethrowError(e, callName) {
                    logger.error(e) { "$callName: Propagating illegal exception - ${e.message}" }
                }
            }
            logger.warn(e) { "$callName: Attempt ${attempt + 1} of $attempts failed - retrying in $currentDelay ms - ${e.message}" }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * 2.0).toLong().coerceAtMost(maxDelay)
    }
    return try {
        timed(callName) { block() }
    } catch (e: Throwable) {
        countAndRethrowError(e, callName) {
            logger.error(e) { "$callName: Final retry attempt #$attempts failed - ${e.message}" }
        }
    }
}

private fun countAndRethrowError(e: Throwable, callName: String, block: () -> Any?): Nothing {
    Metrics.networkCallFailuresCounter.labels(callName).inc()
    block()
    throw e
}

private suspend inline fun <T> timed(callName: String, crossinline block: suspend () -> T) =
    Metrics.networkCallSummary.labels(callName).startTimer().use {
        block()
    }
