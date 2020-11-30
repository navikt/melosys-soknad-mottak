package no.nav.melosys.soknadmottak.soknad

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SoknadServiceTest {
    @RelaxedMockK
    lateinit var soknadRepository: SoknadRepository
    @MockK
    lateinit var dokgenService: DokgenService

    private lateinit var soknadService: SoknadService

    @BeforeAll
    fun setUp() {
        soknadService = SoknadService(soknadRepository, dokgenService)
    }

    @Test
    fun hentPdf() {
        val søknad = SoknadFactory.lagSoknadFraXmlFil()
        every { dokgenService.lagSøknadPDF(any()) } returns ByteArray(8)

        val pdf = soknadService.lagPdf(søknad)
        assertThat(pdf).hasSize(8)
    }

    @Test
    fun hentSøknad() {
        val soknadID = UUID.randomUUID()
        soknadService.hentSøknad(soknadID.toString())
        verify { soknadRepository.findBySoknadID(soknadID) }
    }

    @Test
    fun oppdaterLeveringsstatus() {
        val soknadID = UUID.randomUUID()
        val soknad = SoknadFactory.lagSoknad().apply {
            this.soknadID = soknadID
        }
        every { soknadRepository.findBySoknadID(soknadID) } returns soknad
        every { soknadRepository.save(any<Soknad>()) } returns mockk()

        soknadService.oppdaterLeveringsstatus(soknadID.toString())

        val slot = slot<Soknad>()
        verify { soknadRepository.save(capture(slot)) }
        assertThat(slot.captured.levert).isTrue()
    }
}
