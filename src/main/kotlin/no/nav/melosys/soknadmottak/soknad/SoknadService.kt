package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class SoknadService @Autowired constructor(
    private val soknadRepository: SoknadRepository
) {
    fun hentSøknad(soknadID: String): Soknad {
        logger.debug{ "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(soknadID)
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }

    fun oppdaterLeveringsstatus(soknadID: String) {
        soknadRepository.save(hentSøknad(soknadID).apply {
            levert = true
        })
    }
}