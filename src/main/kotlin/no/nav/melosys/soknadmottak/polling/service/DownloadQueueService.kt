package no.nav.melosys.soknadmottak.polling.service

import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.common.retry
import no.nav.melosys.soknadmottak.metrics.Metrics
import no.nav.melosys.soknadmottak.polling.altinn.client.AltinnProperties
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

private const val CALL_NAME = "Altinn - DownloadQueue"
private val logger = KotlinLogging.logger { }

@Service
class DownloadQueueService(
    private val properties: AltinnProperties,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val username = properties.username
    private val password = properties.password

    suspend fun pollDocuments() {
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val forsendelseIdXpath: XPathExpression = xPath.compile("/melding/Skjema/henvendelse/forsendelsesId")

        val documentBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        logger.info { "DownloadQueue: initiated polling from DownloadQueue" }

        try {
            MDC.put(MDC_CALL_ID, UUID.randomUUID().toString())
            withContext(MDCContext()) {
                val items = getDownloadQueueItems(properties.service.code).downloadQueueItemBE
                logger.debug { "DownloadQueue: processing '${items.size}' items" }
                items.forEachIndexed { index, item ->
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(item.archiveReference)
                    val doc: Document = documentBuilder.parse(
                        InputSource(
                            archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData
                                .removePrefix("<![CDATA[")
                                .removeSuffix("]]>")
                                .reader()
                        )
                    )
                    val forsendelseId = forsendelseIdXpath.evaluate(doc)

                    val attachments = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE

                    logger.info { "DownloadQueue: processing '${attachments.size}' attachments for archive: '${item.archiveReference}'" }
                    attachments.forEachIndexed { attachmentIndex, attachment ->
                        throw UnsupportedOperationException("Vedlegg støttes ikke.")
                    }
                    // TODO Kafka oppdateres
                    purgeItemFromDownloadQueue(item)
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


    private suspend fun purgeItem(archiveReference: String) =
        retry(callName = CALL_NAME) {
            iDownloadQueueExternalBasic.purgeItem(username, password, archiveReference)
        }

    private suspend fun getArchivedFormTaskBasicDQ(archiveReference: String) =
        retry(callName = CALL_NAME) {
            iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(username, password, archiveReference, null, false)
        }

    private suspend fun purgeItemFromDownloadQueue(item: DownloadQueueItemBE) {
        try {
            purgeItem(item.archiveReference)
            logger.debug { "DownloadQueue: successfully purged archive reference '${item.archiveReference}'" }
        } catch (e: Throwable) {
            logger.error { "DownloadQueue: failed to purge archive reference '${item.archiveReference}'" }
        }
    }
}