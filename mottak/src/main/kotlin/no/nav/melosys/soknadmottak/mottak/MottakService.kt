package no.nav.melosys.soknadmottak.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.config.MottakConfig
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.dokument.DokumentType
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger { }
private const val ETT_SEKUND_MILLI = 30 * 1000L
private const val TO_SEKUNDER_MILLI = 45 * 1000L

@Service
class MottakService(
    private val soknadService: SoknadService,
    private val dokumentService: DokumentService,
    private val kafkaProducer: KafkaProducer,
    private val mottakConfig: MottakConfig,
    private val altinnConfig: AltinnConfig,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val brukernavn = altinnConfig.username
    private val passord = altinnConfig.password

    @Scheduled(fixedRate = ETT_SEKUND_MILLI, initialDelay = ETT_SEKUND_MILLI)
    fun pollDokumentKø() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val elementer = getDownloadQueueItems(altinnConfig.downloadQueue.code).downloadQueueItemBE
                logger.debug { "Behandler '${elementer.size}' elementer" }
                elementer.forEachIndexed { index, item ->
                    val arkivRef = item.archiveReference
                    val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(arkivRef)
                    val vedlegg = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE
                    val innsendtTidspunkt = archivedFormTaskBasicDQ.archiveTimeStamp.toGregorianCalendar().toInstant()

                    val søknad = Soknad(
                        arkivRef,
                        false,
                        archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData,
                        innsendtTidspunkt
                    )
                    if (soknadService.erSøknadArkivIkkeLagret(arkivRef)) {
                        lagreDokumentOgVedlegg(søknad, arkivRef, vedlegg)
                        fjernElementFraKø(arkivRef)
                        logger.info {
                            "Behandlet AR: '$arkivRef' ('${index + 1} av ${elementer.size}') "
                        }
                    }
                }
                logger.debug { "Ferdig med behandling av '${elementer.size}' elementer." }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    @Scheduled(fixedRate = ETT_SEKUND_MILLI, initialDelay = TO_SEKUNDER_MILLI)
    fun publiserIkkeLeverteSøknader() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                soknadService.hentIkkeLeverteSøknader().forEach { søknad ->
                    logger.info { "Publiser søknad med soknadID ${søknad.soknadID}" }
                    kafkaProducer.publiserMelding(SoknadMottatt(søknad))
                }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    fun sendIkkeLeverteSøknader(soknadIDer: List<UUID>) {
        logger.info { "Forsøker å sende søknader på nytt $soknadIDer" }
        soknadService.hentIkkeLeverteSøknader()
            .filter { soknadIDer.contains(it.soknadID) }
            .forEach { kafkaProducer.publiserMelding(SoknadMottatt(it)) }
    }

    @Transactional
    fun lagreDokumentOgVedlegg(
        søknad: Soknad,
        arkivRef: String,
        vedlegg: MutableList<ArchivedAttachmentDQBE>
    ) {
        soknadService.lagre(søknad)
        val søknadPDF = soknadService.lagPdf(søknad)
        dokumentService.lagreDokument(
            Dokument(
                søknad,
                "ref_$arkivRef.pdf", DokumentType.SOKNAD, søknadPDF
            )
        )
        behandleVedleggListe(søknad, vedlegg, arkivRef)
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

    private fun fjernElementFraKø(arkivRef: String) {
        try {
            if (mottakConfig.fjernFraDq) {
                purgeItem(arkivRef)
                logger.info { "Fjernet arkiv '$arkivRef'" }
            }
        } catch (e: Throwable) {
            logger.error { "Kunne ikke fjerne arkiv '$arkivRef'" }
        }
    }

    fun getDownloadQueueItems(serviceCode: String): DownloadQueueItemBEList =
        iDownloadQueueExternalBasic.getDownloadQueueItems(brukernavn, passord, serviceCode)

    private fun purgeItem(arkivRef: String) =
        iDownloadQueueExternalBasic.purgeItem(brukernavn, passord, arkivRef)

    private fun getArchivedFormTaskBasicDQ(arkivRef: String) =
        iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(brukernavn, passord, arkivRef, null, false)
}
