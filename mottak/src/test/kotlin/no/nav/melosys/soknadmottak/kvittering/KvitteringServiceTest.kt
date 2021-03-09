package no.nav.melosys.soknadmottak.kvittering

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import no.nav.melosys.soknadmottak.kvittering.altinn.KorrespondanseService
import no.nav.melosys.soknadmottak.kvittering.altinn.Melding
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class KvitteringServiceTest {
    @RelaxedMockK
    lateinit var korrespondanseService: KorrespondanseService

    @Test
    fun lagKvittering() {
        val arkivRef = "arkivRef"
        val kvitteringService = KvitteringService(korrespondanseService)

        kvitteringService.sendKvittering(
            "mottakerID",
            arkivRef,
            ByteArray(0)
        )

        val meldingSlot = slot<Melding>()
        verify {
            korrespondanseService.lagMelding(
                mottakerID = "mottakerID",
                arkivRef = arkivRef,
                any(),
                capture(meldingSlot),
                any()
            )
        }
        assertThat(meldingSlot.captured.tekst).contains(arkivRef)
        verify { korrespondanseService.sendMelding(any()) }
    }
}
