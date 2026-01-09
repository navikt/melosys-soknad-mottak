package no.nav.melosys.soknadmottak.mottak

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasicGetDownloadQueueItemsAltinnFaultFaultFaultMessage
import no.nav.melosys.soknadmottak.common.MDC_CALL_ID
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.config.MetrikkConfig
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.dokument.DokumentType
import no.nav.melosys.soknadmottak.kafka.KafkaAivenProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.kopi.KopiService
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
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
                val items = getDownloadQueueItems(altinnConfig.downloadQueue.code).downloadQueueItemBE
                if (items.size > 0) {
                    logger.info {
                        "Hentet ${items.size} DownloadQueueItems: ${items.map { it.archiveReference }}"
                    }
                }
                items.forEachIndexed { index, item ->
                    try {
                        behandleArkivItem(index, item)
                    } catch (t: Throwable) {
                        logger.error(t) {
                            "Behandling av arkivRef ${item.archiveReference} feilet"
                        }
                    }
                }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    private fun behandleArkivItem(
        index: Int,
        item: DownloadQueueItemBE
    ) {
        logger.info { "Behandler arkiv ${formatDownloadQueueItemData(item)}" }
        val arkivRef = item.archiveReference

        if (!soknadService.erSøknadArkivLagret(arkivRef)) {
            logger.info {
                "Lagrer melding og vedlegg for arkivRef $arkivRef (index: ${index + 1}) "
            }
            lagreMeldingOgVedleggForArkiv(arkivRef)
            MetrikkConfig.Metrikker.søknadMottatt.increment()
        }

        val søknad = soknadService.hentSøknadMedArkivRef(arkivRef)
        val søknadPDF = soknadService.lagPDF(søknad)
        lagreNySøknadPDF(søknad, søknadPDF)
        sendSøknadKopiHvisØnskelig(søknad, arkivRef, søknadPDF)
        fjernElementFraKø(arkivRef)
        logger.info {
            "Behandlet arkivRef $arkivRef (index: ${index + 1})"
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
                        "Følgende ${partition.second.size} søknad(er) mangler dokument og kan derfor ikke publiseres: ${partition.second.map { it.soknadID }}"
                    }
                }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    @Scheduled(fixedDelay = VENTETID_MELLOM_JOBBER_MILLIS * 12, initialDelay = 60 * 1000L)
    fun lagSøknadPdfHvisManglerEtterFeil() {
        try {
            withLoggingContext(MDC_CALL_ID to UUID.randomUUID().toString()) {
                soknadService.hentIkkeLeverteSøknader()
                    .filter { !finnesPåDownloadQueue(it.arkivReferanse) }
                    .filter { !harSøknadPdf(it) }
                    .forEach { søknad ->
                        logger.info {
                            "Søknad med arkiv referanse ${søknad.arkivReferanse} mangler dokument og er ikke på kø."
                        }
                        val søknadPDF = soknadService.lagPDF(søknad)
                        lagreNySøknadPDF(søknad, søknadPDF)
                        logger.info {
                            "Opprettet PDF for søknad ${søknad.soknadID} med arkiv referanse ${søknad.arkivReferanse}"
                        }
                    }
            }
        } finally {
            MDC.remove(MDC_CALL_ID)
        }
    }

    private fun formatDownloadQueueItemData(item: DownloadQueueItemBE): String {
        return "archiveReference ${item.archiveReference}, archivedDate ${item.archivedDate}, " +
                "reporteeType ${item.reporteeType}, serviceCode ${item.serviceCode}, serviceEditionCode ${item.serviceEditionCode}"
    }

    private fun lagreMeldingOgVedleggForArkiv(arkivRef: String) {
        val archivedFormTaskBasicDQ = getArchivedFormTaskBasicDQ(arkivRef)
        val formData = archivedFormTaskBasicDQ.forms.archivedFormDQBE[0].formData
        val vedlegg = archivedFormTaskBasicDQ.attachments.archivedAttachmentDQBE
        val innsendtTidspunkt = archivedFormTaskBasicDQ.archiveTimeStamp.toGregorianCalendar().toInstant()
        val nySøknad = Soknad.fraArkivFormData(arkivRef, formData, innsendtTidspunkt)
        soknadService.lagreSøknadMeldingOgVedlegg(nySøknad, arkivRef, vedlegg)
    }

    private fun lagreNySøknadPDF(søknad: Soknad, søknadPDF: ByteArray) {
        val søknadDokument = dokumentService.hentSøknadDokument(søknad.soknadID.toString())
        if (søknadDokument.innhold == null) {
            dokumentService.lagrePDF(søknadDokument, søknadPDF)
        }
    }

    private fun sendSøknadKopiHvisØnskelig(
        søknad: Soknad,
        arkivRef: String,
        søknadPDF: ByteArray
    ) {
        if (søknad.innsendtTidspunkt.isAfter(Instant.now().minus(7, ChronoUnit.DAYS))) {
            kopiService.sendKopi(søknad.hentKvitteringMottakerID(), arkivRef, søknadPDF)
        } else {
            logger.info {
                "Sender ikke søknad kopi for AR $arkivRef fordi for mange dager har passert."
            }
        }
    }

    private fun fjernElementFraKø(arkivRef: String) {
        try {
            purgeItem(arkivRef)
            logger.info { "Fjernet arkiv $arkivRef" }
        } catch (t: Throwable) {
            logger.error(t, { "Kunne ikke fjerne arkiv $arkivRef" })
        }
    }

    private fun finnesPåDownloadQueue(arkivReferanse: String): Boolean =
        getDownloadQueueItems(altinnConfig.downloadQueue.code).downloadQueueItemBE.map {
            it.archiveReference
        }.contains(arkivReferanse)

    private fun harSøknadPdf(it: Soknad) =
        dokumentService.hentDokumenterForSoknad(it.soknadID.toString()).filter {
            it.type == DokumentType.SOKNAD
        }.none {
            it.innhold == null
        }

    fun getDownloadQueueItems(serviceCode: String): DownloadQueueItemBEList =
        try {
            iDownloadQueueExternalBasic.getDownloadQueueItems(brukernavn, passord, serviceCode)
        } catch (e: IDownloadQueueExternalBasicGetDownloadQueueItemsAltinnFaultFaultFaultMessage) {
            logger.error { "Altinn fault in getDownloadQueueItems: ${e.faultInfo}" }
            throw e
        }

    private fun purgeItem(arkivRef: String) =
        iDownloadQueueExternalBasic.purgeItem(brukernavn, passord, arkivRef)

    private fun getArchivedFormTaskBasicDQ(arkivRef: String) =
        iDownloadQueueExternalBasic.getArchivedFormTaskBasicDQ(brukernavn, passord, arkivRef, null, false)
}
