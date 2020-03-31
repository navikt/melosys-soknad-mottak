package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.SoknadMottak
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import no.nav.melosys.soknadmottak.database.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class AltinnSoknadService @Autowired constructor(
    private val soknadRepository: SoknadRepository
) {
    fun hentSøknad(soknadID: String): SoknadMottak {
        logger.debug{ "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(soknadID)
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }

    fun updateDeliveryStatus(soknadID: String) {
        soknadRepository.save(hentSøknad(soknadID).apply {
            delivered = true
        })
    }
}