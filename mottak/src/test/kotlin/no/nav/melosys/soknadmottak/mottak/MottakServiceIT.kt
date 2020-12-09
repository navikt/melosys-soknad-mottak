package no.nav.melosys.soknadmottak.mottak

import io.mockk.mockk
import no.altinn.services.archive.downloadqueue._2012._08.IDownloadQueueExternalBasic
import no.nav.melosys.soknadmottak.SoknadMottakTestConfiguration
import no.nav.melosys.soknadmottak.config.AltinnConfig
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.kvittering.KvitteringService
import no.nav.melosys.soknadmottak.soknad.SoknadService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [SoknadMottakTestConfiguration::class])
@ActiveProfiles("test")
internal class MottakServiceIT(
    @Autowired val soknadService: SoknadService,
    @Autowired val dokumentService: DokumentService,
    @Autowired val kvitteringService: KvitteringService,
    @Autowired val altinnConfig: AltinnConfig,
    @Autowired val iDownloadQueueExternalBasic: IDownloadQueueExternalBasic
) {
    private var mottakService = MottakService(
        soknadService,
        dokumentService,
        mockk(),
        kvitteringService,
        altinnConfig,
        iDownloadQueueExternalBasic
    )

    @Test
    fun getDownloadQueueItems() {
        mottakService.getDownloadQueueItems(altinnConfig.downloadQueue.code)
    }
}
