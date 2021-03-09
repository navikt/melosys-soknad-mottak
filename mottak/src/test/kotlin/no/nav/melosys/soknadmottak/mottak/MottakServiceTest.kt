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
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
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
    lateinit var kafkaProducer: KafkaProducer
    @RelaxedMockK
    lateinit var kopiService: KopiService
    @RelaxedMockK
    lateinit var downloadQueue: IDownloadQueueExternalBasic

    private lateinit var mottakService: MottakService

    @BeforeAll
    fun setUp() {
        mottakService = MottakService(
            soknadService,
            dokumentService,
            kafkaProducer,
            kopiService,
            altinnConfig,
            downloadQueue
        )
    }

    @Test
    fun pollDokumentKø() {
        val itemList = DownloadQueueItemBEList()
        val item = DownloadQueueItemBE().apply {
            archiveReference = "ref"
        }
        itemList.downloadQueueItemBE.add(item)
        every { downloadQueue.getDownloadQueueItems(any(), any(), any()) } returns itemList

        val vedlegg1 = ArchivedAttachmentDQBE().apply {
            attachmentData = ByteArray(8)
            fileName = "vedlegg_1"
            attachmentTypeName = "Fullmakt"
        }
        val vedleggListe = ArchivedAttachmentExternalListDQBE().withArchivedAttachmentDQBE(vedlegg1)

        val søknadXML = SoknadFactory.lagSoknadFraXmlFil().innhold
        val nå = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT)

        val archivedForms = ArchivedFormTaskDQBE().apply {
            forms = ArchivedFormListDQBE()
                .withArchivedFormDQBE(ArchivedFormDQBE().withFormData(søknadXML))
            attachments = vedleggListe
            archiveTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(nå)
            reportee = "reportee"
        }
        every { downloadQueue.getArchivedFormTaskBasicDQ(any(), any(), "ref", null, false) } returns archivedForms
        every { soknadService.erSøknadArkivIkkeLagret(any()) } returns true

        mottakService.pollDokumentKø()

        val soknadSlot = slot<Soknad>()
        verify { soknadService.lagreSøknadOgDokumenter(capture(soknadSlot), eq("ref"), any()) }
        assertThat(soknadSlot.captured.innsendtTidspunkt).isEqualTo(nå)
        verify { soknadService.lagPdf(soknadSlot.captured) }
        val pdfSlot = slot<ByteArray>()
        verify { kopiService.sendKopi(eq("fullmektigVirksomhetsnummer"), eq("ref"), capture(pdfSlot)) }
        verify { dokumentService.lagrePDF(any(), pdfSlot.captured) }
        verify { downloadQueue.purgeItem(any(), any(), "ref") }
    }

    @Test
    fun `publiserIkkeLeverteSøknader dokumentInnholdErLagret søknadBlirPublisert`() {
        val søknad = Soknad("", false, "", Instant.now(), soknadID = UUID.randomUUID())
        every { soknadService.hentIkkeLeverteSøknader() } returns listOf(søknad)
        every { dokumentService.erDokumentInnholdLagret(søknad.soknadID.toString()) } returns true
        mottakService.publiserIkkeLeverteSøknader()

        val slot = slot<SoknadMottatt>()
        verify { kafkaProducer.publiserMelding(capture(slot)) }
        assertThat(slot.captured.soknadID).isEqualTo(søknad.soknadID.toString())
    }

    @Test
    fun `publiserIkkeLeverteSøknader dokumentInnholdIkkeLagret søknadBlirIkkePublisert`() {
        val søknad = Soknad("", false, "", Instant.now(), soknadID = UUID.randomUUID())
        every { soknadService.hentIkkeLeverteSøknader() } returns listOf(søknad)
        every { dokumentService.erDokumentInnholdLagret(søknad.soknadID.toString()) } returns false
        mottakService.publiserIkkeLeverteSøknader()

        verify(exactly = 0) { kafkaProducer.publiserMelding(any()) }
    }
}
