package no.nav.melosys.soknadmottak.polling.service

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.Soknad
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.database.SoknadRepository
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.metrics.Metrics
import no.nav.melosys.soknadmottak.polling.altinn.client.AltinnProperties
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }

@Service
class DownloadQueueService(
    val soknadRepository: SoknadRepository,
    val kafkaProducer: KafkaProducer,
    private val properties: AltinnProperties,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val username = properties.username
    private val password = properties.password

    @Scheduled(fixedRate = 10000)
    fun pollDocuments() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val items = getDownloadQueueItems(properties.service.code).downloadQueueItemBE
                logger.debug { "DownloadQueue: processing '${items.size}' items" }
                items.forEachIndexed { index, item ->
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(item.archiveReference)
                    val attachments = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE

                    logger.info { "DownloadQueue: processing '${attachments.size}' attachments for archive: '${item.archiveReference}'" }
                    attachments.forEachIndexed { attachmentIndex, attachment ->
                        throw UnsupportedOperationException("Vedlegg støttes ikke.")
                    }

                    val søknad = Soknad(item.archiveReference, archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData)
                    soknadRepository.save(søknad)
                    kafkaProducer.publiserMelding(søknad)
                    //TODO: Callback purgeItemFromDownloadQueue(item.archiveReference)
                    logger.info { "DownloadQueue: processing of item '${index + 1} of ${items.size}' complete (AR: '${item.archiveReference}')" }
                    Metrics.altinnSkjemaReceivedCounter.inc()
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