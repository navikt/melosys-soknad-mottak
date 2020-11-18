package no.nav.melosys.soknadmottak.mottak

import io.mockk.mockk
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.SoknadMottakTestConfiguration
import no.nav.melosys.soknadmottak.config.MottakConfig
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.mottak.DownloadQueueService
import no.nav.melosys.soknadmottak.mottak.altinn.AltinnProperties
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [SoknadMottakTestConfiguration::class])
@ActiveProfiles("test")
internal class DownloadQueueServiceIT(
    @Autowired val soknadService: SoknadService,
    @Autowired val dokumentService: DokumentService,
    @Autowired val mottakConfig: MottakConfig,
    @Autowired val altinnProperties: AltinnProperties,
    @Autowired val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private var downloadQueueService = DownloadQueueService(
        soknadService, dokumentService, mockk(), mottakConfig, altinnProperties, iDownloadQueueExternalBasic
    )

    @Test
    fun getDownloadQueueItems() {
        downloadQueueService.getDownloadQueueItems(altinnProperties.service.code)
    }
}