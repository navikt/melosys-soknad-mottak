package no.nav.melosys.soknadmottak.common

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class IkkeFunnetException : Exception {
    constructor(melding: String, throwable: Throwable) : super(melding, throwable)

    constructor(melding: String) : super(melding)

    constructor(throwable: Throwable) : super(throwable)
}

class PubliserSoknadException : Exception {
    constructor(melding: String, throwable: Throwable) : super(melding, throwable)

    constructor(melding: String) : super(melding)

    constructor(throwable: Throwable) : super(throwable)
}
