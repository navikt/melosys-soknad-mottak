package no.nav.melosys.soknadmottak.soknad

import mu.KotlinLogging
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.dokument.DokumentType
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger { }

@Service
class SoknadService @Autowired constructor(
    private val soknadRepository: SoknadRepository,
    private val dokgenService: DokgenService,
    private val dokumentService: DokumentService
) {
    fun erSøknadArkivIkkeLagret(arkivRef: String): Boolean {
        return soknadRepository.findByArkivReferanse(arkivRef).count() == 0
    }

    fun hentSøknad(soknadID: String): Soknad {
        logger.debug { "Henter søknad med ID $soknadID" }
        return soknadRepository.findBySoknadID(UUID.fromString(soknadID))
            ?: throw IkkeFunnetException("Finner ikke søknad med ID $soknadID")
    }

    fun lagPdf(søknad: Soknad): ByteArray {
        return dokgenService.lagSøknadPDF(SoknadSkjemaOversetter.tilSøknadsdata(søknad))
    }

    @Transactional
    fun lagreSøknadOgDokumenter(
        søknad: Soknad,
        arkivRef: String,
        vedlegg: MutableList<ArchivedAttachmentDQBE>
    ): String {
        logger.info { "Lagrer søknad med soknadID ${søknad.soknadID}, arkivRef '$arkivRef'" }
        lagre(søknad)
        behandleVedleggListe(søknad, vedlegg, arkivRef)
        return dokumentService.lagreDokument(
            Dokument(
                søknad,
                "ref_$arkivRef.pdf", DokumentType.SOKNAD
            )
        )
    }

    fun lagre(soknad: Soknad): Soknad {
        return soknadRepository.save(soknad)
    }

    fun oppdaterLeveringsstatus(soknadID: String) {
        soknadRepository.save(
            hentSøknad(soknadID).apply {
                levert = true
            }
        )
    }

    fun hentIkkeLeverteSøknader(): Iterable<Soknad> {
        return soknadRepository.findByLevertFalse()
    }

    private fun behandleVedleggListe(
        søknad: Soknad,
        attachments: MutableList<ArchivedAttachmentDQBE>,
        arkivRef: String
    ) {
        logger.info { "Behandler '${attachments.size}' vedlegg for arkiv: '$arkivRef'" }
        attachments.forEach { attachment ->
            behandleVedlegg(søknad, attachment)
        }
    }

    private fun behandleVedlegg(søknad: Soknad, attachment: ArchivedAttachmentDQBE) {
        dokumentService.lagreDokument(Dokument(søknad, attachment.fileName, attachment.attachmentTypeName, attachment.attachmentData))
    }
}
