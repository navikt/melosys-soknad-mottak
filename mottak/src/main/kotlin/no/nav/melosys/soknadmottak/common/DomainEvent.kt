package no.nav.melosys.soknadmottak.common

import java.time.ZonedDateTime

interface DomainEvent {
    val occuredOn: ZonedDateTime
}