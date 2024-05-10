package no.nav.melosys.soknadmottak.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

private val logger = KotlinLogging.logger { }

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class IkkeFunnetException : Exception {
    constructor(melding: String, throwable: Throwable) : super(melding, throwable)

    constructor(melding: String) : super(melding) {
        logger.warn { melding }
    }

    constructor(throwable: Throwable) : super(throwable)
}

class IntegrasjonException : Exception {
    constructor(melding: String) : super(melding) {
        logger.error { message }
    }

    constructor(melding: String, throwable: Throwable) : super(melding, throwable) {
        logger.error(throwable) { melding }
    }
}

class PubliserSoknadException : Exception {
    constructor(melding: String, throwable: Throwable) : super(melding, throwable) {
        logger.error(throwable) { melding }
    }

    constructor(melding: String) : super(melding) {
        logger.error { melding }
    }

    constructor(throwable: Throwable) : super(throwable) {
        logger.error(throwable) { }
    }
}
