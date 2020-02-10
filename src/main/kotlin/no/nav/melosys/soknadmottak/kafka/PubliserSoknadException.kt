package no.nav.melosys.soknadmottak.kafka

class PubliserSoknadException : Exception {
    constructor(melding: String, throwable: Throwable) : super(melding, throwable)

    constructor(melding: String) : super(melding)

    constructor(throwable: Throwable) : super(throwable)
}
