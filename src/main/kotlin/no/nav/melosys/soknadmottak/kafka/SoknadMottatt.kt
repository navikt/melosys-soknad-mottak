package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.SoknadMottak
import no.nav.melosys.soknadmottak.common.DomainEvent
import java.time.ZonedDateTime

data class SoknadMottatt(
    val soknadID: String,
    override val occuredOn: ZonedDateTime = ZonedDateTime.now()
) : DomainEvent {
    companion object {
        operator fun invoke(soknadMottak: SoknadMottak) =
            SoknadMottatt(
                soknadID = soknadMottak.soknadID
            )
    }
}
