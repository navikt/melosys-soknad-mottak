package no.nav.melosys.soknadmottak.kafka

import no.nav.melosys.soknadmottak.Soknad

data class MottattSoknadMelding(
    val archiveReference: String
) {
    companion object {
        operator fun invoke(soknad: Soknad) =
            MottattSoknadMelding(
                archiveReference = soknad.archiveReference
            )
    }
}
