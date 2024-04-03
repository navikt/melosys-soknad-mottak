package no.nav.melosys.soknadmottak.dokument

import de.huxhorn.sulky.ulid.ULID
import io.github.oshai.kotlinlogging.KotlinLogging
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

    fun hentSøknadDokument(soknadID: String): Dokument {
        return dokumentRepository.findBySoknadSoknadIDAndType(UUID.fromString(soknadID), DokumentType.SOKNAD)
    }

    fun lagreDokument(dokument: Dokument): String {
        val dokID = ulidGenerator.nextULID()
        dokumentRepository.save(dokument.apply { dokumentID = dokID })
        return dokID
    }

    fun lagrePDF(
        dokument: Dokument,
        pdf: ByteArray
    ) {
        dokument.innhold = pdf
        lagreDokument(dokument)
    }

    fun erDokumentInnholdLagret(soknadID: String): Boolean {
        return hentDokumenterForSoknad(soknadID)
            .all { it.innhold != null }
    }
}
