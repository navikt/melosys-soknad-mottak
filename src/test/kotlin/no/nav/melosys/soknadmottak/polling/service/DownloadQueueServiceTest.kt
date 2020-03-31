package no.nav.melosys.soknadmottak.polling.service

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBE
import no.altinn.schemas.services.archive.downloadqueue._2012._08.DownloadQueueItemBEList
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentExternalListDQBE
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedFormDQBE
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedFormListDQBE
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedFormTaskDQBE
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.SoknadMottak
import no.nav.melosys.soknadmottak.database.SoknadRepository
import no.nav.melosys.soknadmottak.kafka.KafkaProducer
import no.nav.melosys.soknadmottak.polling.altinn.client.AltinnProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DownloadQueueServiceTest {
    @RelaxedMockK
    lateinit var søknadRepository: SoknadRepository

    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer

    @RelaxedMockK
    lateinit var downloadQueue: IDownloadQueueExternalBasic

    val altinnProperties = AltinnProperties(
        AltinnProperties.Informasjon("url"),
        "user",
        "pass",
        AltinnProperties.Service("code")
    )

    @Test
    fun pollDocuments() {
        val itemList = DownloadQueueItemBEList()
        val item = DownloadQueueItemBE().apply {
            archiveReference = "ref"
        }
        itemList.downloadQueueItemBE.add(item)
        val downloadQueueService =
            DownloadQueueService(søknadRepository, kafkaProducer, altinnProperties, downloadQueue)
        every { downloadQueue.getDownloadQueueItems("user", "pass", "code") } returns itemList

        val søknadXML = "<note>\n" +
                "<to>Tove</to>\n" +
                "<from>Jani</from>\n" +
                "<heading>Reminder</heading>\n" +
                "<body>Don't forget me this weekend!</body>\n" +
                "</note>"
        val archivedForms = ArchivedFormTaskDQBE().apply {
            forms = ArchivedFormListDQBE()
                .withArchivedFormDQBE(ArchivedFormDQBE().withFormData(søknadXML))
            attachments = ArchivedAttachmentExternalListDQBE()
        }
        every { downloadQueue.getArchivedFormTaskBasicDQ("user", "pass", "ref", null, false) } returns archivedForms
        every { søknadRepository.save<SoknadMottak>(any()) } returns SoknadMottak(
            arkivReferanse = "ref",
            levert = false,
            innhold = "content",
            id = 1
        )

        downloadQueueService.pollDocuments()

        verify { søknadRepository.save(any<SoknadMottak>()) }
        verify { kafkaProducer.publiserMelding(any()) }
        verify { downloadQueue.purgeItem(any(), any(), "ref") }
    }
}