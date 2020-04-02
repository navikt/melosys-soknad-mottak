package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.common.DomainEvent
import java.time.ZonedDateTime

data class SoknadMottatt(
    val soknadID: String,
    override val occuredOn: ZonedDateTime = ZonedDateTime.now()
) : DomainEvent {
    companion object {
        operator fun invoke(soknad: Soknad) =
            SoknadMottatt(
                soknadID = soknad.soknadID
            )
    }
}
