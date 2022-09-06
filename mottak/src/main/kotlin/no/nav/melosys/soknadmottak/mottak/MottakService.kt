package no.nav.melosys.soknadmottak.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.kafka.KafkaAivenProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.kopi.KopiService
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger { }
private const val VENTETID_MELLOM_JOBBER_MILLIS = 5 * 60 * 1000L
private const val OPPSTART_FØRSTE_JOBB_MILLIS = 30 * 1000L
private const val OPPSTART_ANDRE_JOBB_MILLIS = 45 * 1000L

@Service
class MottakService(
    private val soknadService: SoknadService,
    private val dokumentService: DokumentService,
    private val kopiService: KopiService,
    private val altinnConfig: AltinnConfig,
    private val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic,
    private val kafkaAivenProducer: KafkaAivenProducer
) {
    private val brukernavn = altinnConfig.username
    private val passord = altinnConfig.password

    @Scheduled(fixedDelay = VENTETID_MELLOM_JOBBER_MILLIS, initialDelay = OPPSTART_FØRSTE_JOBB_MILLIS)
    fun pollDokumentKø() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                val elementer = getDownloadQueueItems(altinnConfig.downloadQueue.code).downloadQueueItemBE
                logger.info {
                    "Hentet '${elementer.size}' DownloadQueueItems: '${elementer.map { it.archiveReference }}'"
                }
                elementer.forEachIndexed { index, item ->
                    logger.info { "Behandler arkiv '${formatDownloadQueueItemData(item)}'" }
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
                        logger.info {
                            "Behandler straks arkivRef: '$arkivRef' ('${index + 1} av ${elementer.size}') "
                        }
                        val søknadDokumentID = soknadService.lagreSøknadMeldingOgVedlegg(søknad, arkivRef, vedlegg)
                        val søknadPDF = soknadService.lagPDF(søknad)
                        kopiService.sendKopi(søknad.hentKvitteringMottakerID(), arkivRef, søknadPDF)
                        dokumentService.lagrePDF(søknadDokumentID, søknadPDF)
                        fjernElementFraKø(arkivRef)
                        logger.info {
                            "Behandlet arkivRef: '$arkivRef'"
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
                val partition = soknadService.hentIkkeLeverteSøknader()
                    .partition { dokumentService.erDokumentInnholdLagret(it.soknadID.toString()) }
                partition.first.forEach { søknad ->
                    logger.info { "Publiser søknad med soknadID ${søknad.soknadID}" }
                    kafkaAivenProducer.publiserMelding(SoknadMottatt(søknad))
                }
                if (partition.second.isNotEmpty()) {
                    logger.info {
                        "Følgende '${partition.second.size}' søknad(er) mangler dokument og kan derfor ikke publiseres: '${partition.second.map { it.soknadID }}''"
                    }
                }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    private fun formatDownloadQueueItemData(item: DownloadQueueItemBE): String {
        return "archiveReference '${item.archiveReference}', archivedDate '${item.archivedDate}', reporteeType '${item
            .reporteeType}', serviceCode '${item.serviceCode}', serviceEditionCode '${item.serviceEditionCode}'"
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
