package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.common.DomainEvent
import no.nav.melosys.soknadmottak.soknad.Soknad
import java.time.ZoneId
import java.time.ZonedDateTime

data class SoknadMottatt(
    val soknadID: String,
    override val occuredOn: ZonedDateTime = ZonedDateTime.now()
) : DomainEvent {
    companion object {
        operator fun invoke(soknad: Soknad) =
            SoknadMottatt(
                soknadID = soknad.soknadID.toString(),
                occuredOn = ZonedDateTime.ofInstant(soknad.innsendtTidspunkt, ZoneId.systemDefault())
            )
    }
}
