package no.nav.melosys.soknadmottak.soknad

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import no.altinn.schemas.services.archive.reporteearchive._2012._08.ArchivedAttachmentDQBE
import no.nav.melosys.soknadmottak.dokument.Dokument
import no.nav.melosys.soknadmottak.dokument.DokumentService
import no.nav.melosys.soknadmottak.soknad.dokgen.DokgenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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
    @RelaxedMockK
    lateinit var dokumentService: DokumentService

    private lateinit var soknadService: SoknadService

    @BeforeAll
    fun setUp() {
        soknadService = SoknadService(soknadRepository, dokgenService, dokumentService)
    }

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun hentPdf() {
        val søknad = SoknadFactory.lagSoknadFraXmlFil()
        every { dokgenService.lagSøknadPDF(any()) } returns ByteArray(8)

        val pdf = soknadService.lagPDF(søknad)
        assertThat(pdf).hasSize(8)
    }

    @Test
    fun hentSøknad() {
        val soknadID = UUID.randomUUID()
        soknadService.hentSøknad(soknadID.toString())
        verify { soknadRepository.findBySoknadID(soknadID) }
    }

    @Test
    fun lagreSøknadMeldingOgVedlegg() {
        val søknad = SoknadFactory.lagSoknad(1)
        val vedlegg = ArchivedAttachmentDQBE().apply {
            attachmentData = ByteArray(8)
            fileName = "vedlegg_1"
            attachmentTypeName = "Fullmakt"
        }
        val soknadSlot = slot<Soknad>()
        every { soknadService.lagre(capture(soknadSlot)) } returns søknad
        every { dokumentService.lagreDokument(any()) } returns "lagret"

        soknadService.lagreSøknadMeldingOgVedlegg(søknad, "ref", mutableListOf(vedlegg))

        verify { soknadService.lagre(any()) }
        val dokumentSlots = mutableListOf<Dokument>()
        verify(exactly = 2) { dokumentService.lagreDokument(capture(dokumentSlots)) }
        assertThat(dokumentSlots[0].filnavn).isEqualTo("ref_ref.pdf")
        assertThat(dokumentSlots[1].filnavn).isEqualTo("vedlegg_1")
    }

    @Test
    fun `Ikke lagre XML + vedlegg hvis det ble gjort`() {
        val søknad = SoknadFactory.lagSoknad(1)
        every { soknadRepository.existsByArkivReferanse("ref") } returns true

        soknadService.lagreSøknadMeldingOgVedlegg(søknad, "ref", emptyList<ArchivedAttachmentDQBE>().toMutableList())

        verify(exactly = 0) { soknadService.lagre(any()) }
        verify { dokumentService wasNot called }
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
        assertThat(slot.captured.levert).isTrue
    }
}
