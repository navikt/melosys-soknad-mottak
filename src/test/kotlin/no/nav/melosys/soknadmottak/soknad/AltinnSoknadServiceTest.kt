package no.nav.melosys.soknadmottak.soknad

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.melosys.soknadmottak.SoknadMottak
import no.nav.melosys.soknadmottak.database.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class AltinnSoknadServiceTest {
    @RelaxedMockK()
    lateinit var soknadRepository: SoknadRepository

    lateinit var altinnSoknadService: AltinnSoknadService

    @BeforeEach
    fun setUp() {
        altinnSoknadService = AltinnSoknadService(soknadRepository)
    }

    @Test
    fun hentSøknad() {
        val soknadID = "soknadID"
        altinnSoknadService.hentSøknad(soknadID)
        verify { soknadRepository.findBySoknadID(soknadID) }
    }

    @Test
    fun updateDeliveryStatus() {
        val soknadID = "soknadID"
        val soknad = SoknadMottak("ref", false, "content", 123, soknadID)
        every { soknadRepository.findBySoknadID(soknadID) } returns soknad
        every { soknadRepository.save(any<SoknadMottak>()) } returns mockk()

        altinnSoknadService.updateDeliveryStatus(soknadID)

        val slot = slot<SoknadMottak>()
        verify { soknadRepository.save(capture(slot)) }
        assertThat(slot.captured.levert).isTrue()
    }
}