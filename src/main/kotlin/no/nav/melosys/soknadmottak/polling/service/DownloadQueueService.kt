package no.nav.melosys.soknadmottak.polling.service

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.Soknad
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.database.SoknadRepository
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.kafka.MottattSoknadMelding
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
    private val username = properties.username
    private val password = properties.password

    @Scheduled(fixedRate = ETT_SEKUND_MILLI, initialDelay = ETT_SEKUND_MILLI)
    fun pollDocuments() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val items = getDownloadQueueItems(properties.service.code).downloadQueueItemBE
                logger.debug { "DownloadQueue: processing '${items.size}' items" }
                items.forEachIndexed { index, item ->
                    val archiveReference = item.archiveReference
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(archiveReference)
                    val attachments = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE

                    logger.info { "DownloadQueue: processing '${attachments.size}' attachments for archive: '${archiveReference}'" }
                    attachments.forEachIndexed { attachmentIndex, attachment ->
                        logger.info { "Vedlegg støttes ikke."}
                    }

                    val søknad = Soknad(archiveReference, false, archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData)
                    if (soknadRepository.findByArchiveReference(archiveReference).count() == 0) {
                        soknadRepository.save(søknad)
                        kafkaProducer.publiserMelding(
                            MottattSoknadMelding(
                                søknad
                            )
                        )
                    }
                    //TODO: Callback purgeItemFromDownloadQueue(item.archiveReference)
                    //logger.info { "DownloadQueue: processing of item '${index + 1} of ${items.size}' complete (AR: '${archiveReference}')" }
                    //Metrics.altinnSkjemaReceivedCounter.inc()
                }
                logger.debug { "DownloadQueue: completed processing '${items.size}' items" }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    fun getDownloadQueueItems(serviceCode: String) =
        iDownloadQueueExternalBasic.getDownloadQueueItems(username, password, serviceCode)


    private fun purgeItem(archiveReference: String) =
        iDownloadQueueExternalBasic.purgeItem(username, password, archiveReference)

    private fun getArchivedFormTaskBasicDQ(archiveReference: String) =
        iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(username, password, archiveReference, null, false)

    fun purgeItemFromDownloadQueue(archiveReference: String) {
        try {
            purgeItem(archiveReference)
            logger.info { "DownloadQueue: successfully purged archive reference '${archiveReference}'" }
        } catch (e: Throwable) {
            logger.error { "DownloadQueue: failed to purge archive reference '${archiveReference}'" }
        }
    }
}