package no.nav.melosys.soknadmottak.kvittering

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import no.nav.melosys.soknadmottak.kvittering.altinn.KorrespondanseService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class KvitteringServiceTest {
    @RelaxedMockK
    lateinit var korrespondanseService: KorrespondanseService

    @Test
    fun lagKvittering() {
        val kvitteringService = KvitteringService(korrespondanseService)
        kvitteringService.sendKvittering(
            "mottakerID",
            "arkivRef",
            ByteArray(0)
        )
        verify { korrespondanseService.sendMelding(any()) }
    }
}