package no.nav.melosys.soknadmottak.mottak

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.schemas.services.archive.reporteearchive._2012._08.*
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentFactory
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.kafka.KafkaAivenProducer
import no.nav.melosys.soknadmottak.kafka.SoknadMottatt
import no.nav.melosys.soknadmottak.kopi.KopiService
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.xml.datatype.DatatypeFactory

private const val ARKIV_REF = "AR_ref"

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MottakServiceTest {
    @RelaxedMockK
    lateinit var altinnConfig: AltinnConfig

    @RelaxedMockK
    lateinit var soknadService: SoknadService

    @RelaxedMockK
    lateinit var dokumentService: DokumentService

    @RelaxedMockK
    lateinit var kopiService: KopiService

    @RelaxedMockK
    lateinit var downloadQueue: IDownloadQueueExternalBasic

    @RelaxedMockK
    lateinit var kafkaAivenProducer: KafkaAivenProducer

    private lateinit var mottakService: MottakService

    @BeforeAll
    fun setUp() {
        mottakService = MottakService(
            soknadService,
            dokumentService,
            kopiService,
            altinnConfig,
            downloadQueue,
            kafkaAivenProducer
        )
    }

    @Test
    fun `pollDokumentKø lagrer (melding + vedlegg + søknadPDF) og sender kopi og fjerner fra kø`() {
        val itemList = DownloadQueueItemBEList()
        val item = DownloadQueueItemBE().apply {
            archiveReference = ARKIV_REF
        }
        itemList.downloadQueueItemBE.add(item)
        every { downloadQueue.getDownloadQueueItems(any(), any(), any()) } returns itemList

        val vedlegg1 = ArchivedAttachmentDQBE().apply {
            attachmentData = ByteArray(8)
            fileName = "vedlegg_1"
            attachmentTypeName = "Fullmakt"
        }
        val vedleggListe = ArchivedAttachmentExternalListDQBE().withArchivedAttachmentDQBE(vedlegg1)

        val nå = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS)
        val søknadXML = SoknadFactory.lagSoknadFraXmlFil().innhold
        val søknad = Soknad.fraArkivFormData(ARKIV_REF, søknadXML, nå.toInstant())

        val archivedForms = ArchivedFormTaskDQBE().apply {
            forms = ArchivedFormListDQBE()
                .withArchivedFormDQBE(ArchivedFormDQBE().withFormData(søknadXML))
            attachments = vedleggListe
            archiveTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(nå.format(DateTimeFormatter.ISO_INSTANT))
            reportee = "reportee"
        }
        every { downloadQueue.getArchivedFormTaskBasicDQ(any(), any(), ARKIV_REF, null, false) } returns archivedForms
        every { soknadService.erSøknadArkivLagret(any()) } returns false
        every { soknadService.hentSøknadMedArkivRef(ARKIV_REF) } returns søknad
        val dokument = DokumentFactory.lagDokument(soknad = søknad, innhold = null)
        every { dokumentService.hentSøknadDokument(any()) } returns dokument


        mottakService.pollDokumentKø()


        val soknadSlot = slot<Soknad>()
        verify { soknadService.lagreSøknadMeldingOgVedlegg(capture(soknadSlot), eq(ARKIV_REF), any()) }
        assertThat(soknadSlot.captured.innsendtTidspunkt).isEqualTo(nå.toInstant())
        val dokumentSlot = slot<Dokument>()
        val pdfSlot = slot<ByteArray>()
        verify { dokumentService.lagrePDF(capture(dokumentSlot), capture(pdfSlot)) }
        assertThat(dokumentSlot.captured.soknad.innhold).isEqualTo(soknadSlot.captured.innhold)
        verify { kopiService.sendKopi("fullmektigVirksomhetsnummer", ARKIV_REF, pdfSlot.captured) }
        verify { downloadQueue.purgeItem(any(), any(), ARKIV_REF) }
    }

    @Test
    fun `pollDokumentKø ikke lagre søknad-PDF hvis det ble gjort`() {
        val itemList = DownloadQueueItemBEList()
        val item = DownloadQueueItemBE().apply {
            archiveReference = ARKIV_REF
        }
        itemList.downloadQueueItemBE.add(item)
        every { downloadQueue.getDownloadQueueItems(any(), any(), any()) } returns itemList

        val søknadXML = SoknadFactory.lagSoknadFraXmlFil().innhold
        val nå = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS)

        val archivedForm = ArchivedFormTaskDQBE().apply {
            forms = ArchivedFormListDQBE()
                .withArchivedFormDQBE(ArchivedFormDQBE().withFormData(søknadXML))
            attachments = ArchivedAttachmentExternalListDQBE()
            archiveTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(nå.format(DateTimeFormatter.ISO_INSTANT))
            reportee = "reportee"
        }
        every { downloadQueue.getArchivedFormTaskBasicDQ(any(), any(), ARKIV_REF, null, false) } returns archivedForm
        every { soknadService.erSøknadArkivLagret(any()) } returns false
        val søknad = Soknad.fraArkivFormData(ARKIV_REF, søknadXML, nå.toInstant())
        every { soknadService.hentSøknadMedArkivRef(ARKIV_REF) } returns søknad
        val dokument = DokumentFactory.lagDokument()
        every { dokumentService.hentSøknadDokument(any()) } returns dokument


        mottakService.pollDokumentKø()


        verify(exactly = 0) { dokumentService.lagrePDF(any(), any()) }
    }

    @Test
    fun `publiserIkkeLeverteSøknader dokumentInnholdErLagret søknadBlirPublisert`() {
        val søknad = Soknad("", false, "", Instant.now(), soknadID = UUID.randomUUID())
        every { soknadService.hentIkkeLeverteSøknader() } returns listOf(søknad)
        every { dokumentService.erDokumentInnholdLagret(søknad.soknadID.toString()) } returns true
        mottakService.publiserIkkeLeverteSøknader()

        val slot = slot<SoknadMottatt>()
        verify { kafkaAivenProducer.publiserMelding(capture(slot)) }
        assertThat(slot.captured.soknadID).isEqualTo(søknad.soknadID.toString())
    }

    @Test
    fun `publiserIkkeLeverteSøknader dokumentInnholdIkkeLagret søknadBlirIkkePublisert`() {
        val søknad = Soknad("", false, "", Instant.now(), soknadID = UUID.randomUUID())
        every { soknadService.hentIkkeLeverteSøknader() } returns listOf(søknad)
        every { dokumentService.erDokumentInnholdLagret(søknad.soknadID.toString()) } returns false
        mottakService.publiserIkkeLeverteSøknader()

        verify(exactly = 0) { kafkaAivenProducer.publiserMelding(any()) }
    }
}
