package no.nav.melosys.soknadmottak.polling.service

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.schemas.services.archive.reporteearchive._2012._08.*
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.config.MottakConfig
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.polling.DownloadQueueService
import no.nav.melosys.soknadmottak.polling.altinn.AltinnProperties
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import no.nav.melosys.soknadmottak.soknad.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DownloadQueueServiceTest {
    @RelaxedMockK
    lateinit var søknadRepository: SoknadRepository
    @RelaxedMockK
    lateinit var dokumentService: DokumentService
    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer
    @RelaxedMockK
    lateinit var downloadQueue: IDownloadQueueExternalBasic

    private val mottakConfig = MottakConfig(
        true
    )

    private val altinnProperties = AltinnProperties(
        AltinnProperties.Informasjon("url"),
        "user",
        "pass",
        AltinnProperties.Service("code")
    )

    @Test
    fun pollDokumentKø() {
        val itemList = DownloadQueueItemBEList()
        val item = DownloadQueueItemBE().apply {
            archiveReference = "ref"
        }
        itemList.downloadQueueItemBE.add(item)
        val downloadQueueService =
            DownloadQueueService(
                søknadRepository,
                dokumentService,
                kafkaProducer,
                mottakConfig,
                altinnProperties,
                downloadQueue
            )
        every { downloadQueue.getDownloadQueueItems("user", "pass", "code") } returns itemList

        val vedlegg1 = ArchivedAttachmentDQBE().apply {
            attachmentData = ByteArray(8)
            fileName = "vedlegg_1"
        }
        val vedleggListe = ArchivedAttachmentExternalListDQBE().withArchivedAttachmentDQBE(vedlegg1)

        val søknadXML = "<note>\n" +
                "<to>Tove</to>\n" +
                "<from>Jani</from>\n" +
                "<heading>Reminder</heading>\n" +
                "<body>Don't forget me this weekend!</body>\n" +
                "</note>"
        val archivedForms = ArchivedFormTaskDQBE().apply {
            forms = ArchivedFormListDQBE()
                .withArchivedFormDQBE(ArchivedFormDQBE().withFormData(søknadXML))
            attachments = vedleggListe
        }
        every { downloadQueue.getArchivedFormTaskBasicDQ("user", "pass", "ref", null, false) } returns archivedForms
        every { søknadRepository.save<Soknad>(any()) } returns SoknadFactory.lagSoknad(1)
        every { dokumentService.lagreDokument(any()) } returns "lagret"

        downloadQueueService.pollDokumentKø()

        verify { søknadRepository.save(any<Soknad>()) }
        val slot = slot<Dokument>()
        verify { dokumentService.lagreDokument(capture(slot)) }
        assertThat(slot.captured.filnavn).isEqualTo("vedlegg_1")
        verify { kafkaProducer.publiserMelding(any()) }
        verify { downloadQueue.purgeItem(any(), any(), "ref") }
    }
}