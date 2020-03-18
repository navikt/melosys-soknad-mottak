package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.Soknad

data class MottattSoknadMelding(
    val soknadID: Long
) {
    companion object {
        operator fun invoke(soknad: Soknad) =
            MottattSoknadMelding(
                soknadID = soknad.id
                    ?: throw UnsupportedOperationException("Kan ikke opprette MottattSoknadMelding uten id")
            )
    }
}
