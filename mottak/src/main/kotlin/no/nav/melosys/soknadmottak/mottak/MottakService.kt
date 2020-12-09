package no.nav.melosys.soknadmottak.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.dokument.DokumentType
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.kvittering.KvitteringService
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger { }
private const val VENTETID_MELLOM_JOBBER_MILLIS = 10 * 1000L
private const val OPPSTART_FØRSTE_JOBB_MILLIS = 30 * 1000L
private const val OPPSTART_ANDRE_JOBB_MILLIS = 45 * 1000L

@Service
class MottakService(
    private val soknadService: SoknadService,
    private val dokumentService: DokumentService,
    private val kafkaProducer: KafkaProducer,
    private val kvitteringService: KvitteringService,
    private val altinnConfig: AltinnConfig,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private val brukernavn = altinnConfig.username
    private val passord = altinnConfig.password

    @Scheduled(fixedDelay = VENTETID_MELLOM_JOBBER_MILLIS, initialDelay = OPPSTART_FØRSTE_JOBB_MILLIS)
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
                        val søknadPDF = soknadService.lagPdf(søknad)
                        lagreDokumentOgVedlegg(søknad, arkivRef, vedlegg, søknadPDF)
                        kvitteringService.sendKvittering(søknad.hentKvitteringMottakerID(), arkivRef, søknadPDF)
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

    @Scheduled(fixedDelay = VENTETID_MELLOM_JOBBER_MILLIS, initialDelay = OPPSTART_ANDRE_JOBB_MILLIS)
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

    @Transactional
    fun lagreDokumentOgVedlegg(
        søknad: Soknad,
        arkivRef: String,
        vedlegg: MutableList<ArchivedAttachmentDQBE>,
        søknadPDF: ByteArray
    ) {
        logger.info { "Lagrer søknad med soknadID ${søknad.soknadID}" }
        soknadService.lagre(søknad)
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
            purgeItem(arkivRef)
            logger.info { "Fjernet arkiv '$arkivRef'" }
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
