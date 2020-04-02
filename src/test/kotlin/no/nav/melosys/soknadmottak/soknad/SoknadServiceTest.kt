package no.nav.melosys.soknadmottak.soknad

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SoknadServiceTest {
    @RelaxedMockK()
    lateinit var soknadRepository: SoknadRepository

    lateinit var soknadService: SoknadService

    @BeforeEach
    fun setUp() {
        soknadService = SoknadService(soknadRepository)
    }

    @Test
    fun hentSøknad() {
        val soknadID = "soknadID"
        soknadService.hentSøknad(soknadID)
        verify { soknadRepository.findBySoknadID(soknadID) }
    }

    @Test
    fun updateDeliveryStatus() {
        val soknadID = "soknadID"
        val soknad =
            Soknad("ref", false, "content", 123, soknadID)
        every { soknadRepository.findBySoknadID(soknadID) } returns soknad
        every { soknadRepository.save(any<Soknad>()) } returns mockk()

        soknadService.updateDeliveryStatus(soknadID)

        val slot = slot<Soknad>()
        verify { soknadRepository.save(capture(slot)) }
        assertThat(slot.captured.levert).isTrue()
    }
}