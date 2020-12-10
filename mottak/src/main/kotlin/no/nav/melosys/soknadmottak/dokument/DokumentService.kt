package no.nav.melosys.soknadmottak.dokument

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }
private val ulidGenerator = ULID()

@Service
class DokumentService @Autowired constructor(
    private val dokumentRepository: DokumentRepository
) {
    fun hentDokument(dokumentID: String): Dokument {
        logger.debug { "Henter dokument med ID $dokumentID" }
        return dokumentRepository.findByDokumentID(dokumentID)
            ?: throw IkkeFunnetException("Finner ikke dokument med ID $dokumentID")
    }

    fun hentDokumenterForSoknad(soknadID: String): Iterable<Dokument> {
        return dokumentRepository.findBySoknadSoknadID(UUID.fromString(soknadID))
    }

    fun lagreDokument(dokument: Dokument): String {
        val dokID = ulidGenerator.nextULID()
        dokumentRepository.save(dokument.apply { dokumentID = dokID })
        return dokID
    }

    fun lagrePDF(
        dokID: String,
        søknadPDF: ByteArray
    ) {
        val dokument = hentDokument(dokID)
        dokument.innhold = søknadPDF
        lagreDokument(dokument)
    }

    fun erDokumentInnholdLagret(soknadID: String): Boolean {
        return hentDokumenterForSoknad(soknadID)
            .all { it.innhold != null }
    }
}
