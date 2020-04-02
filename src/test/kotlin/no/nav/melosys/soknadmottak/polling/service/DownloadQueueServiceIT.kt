package no.nav.melosys.soknadmottak.polling.service

import io.mockk.mockk
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.SoknadMottakTestConfiguration
import no.nav.melosys.soknadmottak.soknad.SoknadRepository
import no.nav.melosys.soknadmottak.polling.altinn.client.AltinnProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [SoknadMottakTestConfiguration::class])
@ActiveProfiles("test")
internal class DownloadQueueServiceIT(
    @Autowired val soknadRepository: SoknadRepository,
    @Autowired val altinnProperties: AltinnProperties,
    @Autowired val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private var downloadQueueService = DownloadQueueService(soknadRepository,
        mockk(), altinnProperties, iDownloadQueueExternalBasic)

    @Test
    fun getDownloadQueueItems() {
        downloadQueueService.getDownloadQueueItems(altinnProperties.service.code)
    }
}