package no.nav.melosys.soknadmottak.polling

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.dokument.DokumentType.SOKNAD
import no.nav.melosys.soknadmottak.dokument.DokumentType.VEDLEGG
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.polling.altinn.AltinnProperties
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadRepository
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }
private const val ETT_SEKUND_MILLI = 30 * 1000L

@Service
class DownloadQueueService(
    val soknadRepository: SoknadRepository,
    val dokumentService: DokumentService,
    val kafkaProducer: KafkaProducer,
    private val properties: AltinnProperties,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val brukernavn = properties.username
    private val passord = properties.password

    @Scheduled(fixedRate = ETT_SEKUND_MILLI, initialDelay = ETT_SEKUND_MILLI)
    fun pollDokumentKø() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val elementer = getDownloadQueueItems(properties.service.code).downloadQueueItemBE
                logger.debug { "DownloadQueue: behandler '${elementer.size}' elementer" }
                elementer.forEachIndexed { index, item ->
                    val arkivRef = item.archiveReference
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(arkivRef)
                    val vedlegg = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE

                    val søknad = Soknad(
                        arkivRef,
                        false,
                        archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData
                    )
                    if (soknadRepository.findByArkivReferanse(arkivRef).count() == 0) {
                        soknadRepository.save(søknad)
                        dokumentService.lagreDokument(Dokument(søknad, "TODO", SOKNAD, hentSkjemaPdf(arkivRef)))
                        kafkaProducer.publiserMelding(SoknadMottatt(søknad))
                        behandleVedleggListe(søknad, vedlegg, arkivRef)
                        fjernElementFraKø(arkivRef)
                        logger.info {
                            "DownloadQueue: behandlet AR: '${arkivRef}' ('${index + 1} av ${elementer.size}') "
                        }
                    }
                }
                logger.debug { "DownloadQueue: ferdig med behandling av '${elementer.size}' elementer." }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    private fun behandleVedleggListe(
        søknad: Soknad,
        attachments: MutableList<ArchivedAttachmentDQBE>,
        arkivRef: String
    ) {
        logger.info { "DownloadQueue: behandler '${attachments.size}' vedlegg for arkiv: '${arkivRef}'" }
        attachments.forEach { attachment ->
            behandleVedlegg(søknad, attachment)
        }
    }

    private fun behandleVedlegg(søknad: Soknad, attachment: ArchivedAttachmentDQBE) {
        dokumentService.lagreDokument(Dokument(søknad, attachment.fileName, VEDLEGG, attachment.attachmentData))
    }

    private fun hentSkjemaPdf(arkivRef: String) =
        iDownloadQueueExternalBasic.getFormSetPdfBasic(
            brukernavn,
            passord,
            arkivRef,
            1044,
            "6065",
            43900
        )!!

    private fun fjernElementFraKø(arkivRef: String) {
        try {
            purgeItem(arkivRef)
            logger.info { "DownloadQueue: fjernet arkiv '${arkivRef}'" }
        } catch (e: Throwable) {
            logger.error { "DownloadQueue: kunne ikke fjerne arkiv '${arkivRef}'" }
        }
    }

    fun getDownloadQueueItems(serviceCode: String) =
        iDownloadQueueExternalBasic.getDownloadQueueItems(brukernavn, passord, serviceCode)

    private fun purgeItem(arkivRef: String) =
        iDownloadQueueExternalBasic.purgeItem(brukernavn, passord, arkivRef)

    private fun getArchivedFormTaskBasicDQ(arkivRef: String) =
        iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(brukernavn, passord, arkivRef, null, false)
}