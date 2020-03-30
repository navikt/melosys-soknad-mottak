package no.nav.melosys.soknadmottak.polling.service

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.Soknad
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.database.SoknadRepository
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.polling.altinn.client.AltinnProperties
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }
private const val ETT_SEKUND_MILLI = 30 * 1000L

@Service
class DownloadQueueService(
    val soknadRepository: SoknadRepository,
    val kafkaProducer: KafkaProducer,
    private val properties: AltinnProperties,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val brukernavn = properties.username
    private val passord = properties.password

    @Scheduled(fixedRate = ETT_SEKUND_MILLI, initialDelay = ETT_SEKUND_MILLI)
    fun pollDocuments() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val elementer = getDownloadQueueItems(properties.service.code).downloadQueueItemBE
                logger.debug { "DownloadQueue: behandler '${elementer.size}' elementer" }
                elementer.forEachIndexed { index, item ->
                    val archiveReference = item.archiveReference
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(archiveReference)
                    val vedlegg = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE

                    val søknad = Soknad(archiveReference, false, archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData)
                    if (soknadRepository.findByArchiveReference(archiveReference).count() == 0) {
                        soknadRepository.save(søknad)
                        kafkaProducer.publiserMelding(SoknadMottatt(søknad))
                        behandleVedlegg(vedlegg, archiveReference)
                        fjernerElementFraKø(archiveReference)
                        logger.info {
                            "DownloadQueue: behandlet AR: '${archiveReference}' ('${index + 1} av ${elementer.size}') "
                        }
                    }
                }
                logger.debug { "DownloadQueue: ferdig med behandling av '${elementer.size}' elementer." }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    private fun behandleVedlegg(
        attachments: MutableList<ArchivedAttachmentDQBE>,
        archiveReference: String
    ) {
        logger.info { "DownloadQueue: behandler '${attachments.size}' vedlegg for arkiv: '${archiveReference}'" }
        attachments.forEachIndexed { attachmentIndex, attachment ->
            logger.info { "Vedlegg støttes ikke." }
        }
    }

    fun getDownloadQueueItems(serviceCode: String) =
        iDownloadQueueExternalBasic.getDownloadQueueItems(brukernavn, passord, serviceCode)

    private fun purgeItem(archiveReference: String) =
        iDownloadQueueExternalBasic.purgeItem(brukernavn, passord, archiveReference)

    private fun getArchivedFormTaskBasicDQ(archiveReference: String) =
        iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(brukernavn, passord, archiveReference, null, false)

    fun fjernerElementFraKø(archiveReference: String) {
        try {
            purgeItem(archiveReference)
            logger.info { "DownloadQueue: fjernet arkiv '${archiveReference}'" }
        } catch (e: Throwable) {
            logger.error { "DownloadQueue: kunne ikke fjerne arkiv '${archiveReference}'" }
        }
    }
}