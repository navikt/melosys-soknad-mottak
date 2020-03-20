package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.nav.melosys.soknadmottak.Soknad
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import no.nav.melosys.soknadmottak.database.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }

@Service
class AltinnSoknadService @Autowired constructor(
    private val soknadRepository: SoknadRepository
) {
    fun hentSøknad(soknadID: UUID): Soknad {
        logger.info { "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(soknadID)
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }
}