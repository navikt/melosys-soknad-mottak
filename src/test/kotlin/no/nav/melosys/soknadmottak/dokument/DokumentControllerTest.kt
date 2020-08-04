package no.nav.melosys.soknadmottak.dokument

import com.fasterxml.jackson.databind.ObjectMapper
import de.huxhorn.sulky.ulid.ULID
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.melosys.soknadmottak.common.IkkeFunnetException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(DokumentController::class)
class DokumentControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val dokumentService: DokumentService
) {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun dokumentService() = mockk<DokumentService>()
    }

    @Test
    fun `hent dokument som finnes, forvent dokument med innhold`() {
        val fakePdf = "Dette er faktisk en PDF".toByteArray()
        every { dokumentService.hentDokument(DOKUMENT_ID) } returns DokumentFactory.lagDokument(
            innhold = fakePdf
        )

        val result = mockMvc.get("/api/dokumenter/$DOKUMENT_ID") {
            accept(MediaType.APPLICATION_PDF)
        }.andExpect {
            status { isOk }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PDF)
            }
        }.andReturn()

        verify { dokumentService.hentDokument(DOKUMENT_ID) }
        result.response.contentAsByteArray.let {
            assertThat(it).isEqualTo(fakePdf)
        }
    }

    @Test
    fun `hent dokument som ikke finnes, forvent not found`() {
        every { dokumentService.hentDokument(DOKUMENT_ID) } throws IkkeFunnetException("Ikke funnet")

        mockMvc.get("/api/dokumenter/$DOKUMENT_ID") {
            accept(MediaType.APPLICATION_PDF)
        }.andExpect {
            status { isNotFound }
        }

        verify { dokumentService.hentDokument(DOKUMENT_ID) }
    }

    @Test
    fun `hent vedlegg for en søknad, forvent vedlegg med pdf som base64-string`() {

        every { dokumentService.hentDokumenterForSoknad(SOKNAD_ID.toString()) } returns listOf(DokumentFactory.lagDokument())

        val res = mockMvc.get("/api/dokumenter/$SOKNAD_ID") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            }
        }.andReturn()

        res.response.contentAsString.let {
            assertThat(ObjectMapper().readTree(it).path(0).path("innhold").textValue()).isBase64()
        }

        verify { dokumentService.hentDokumenterForSoknad(SOKNAD_ID.toString()) }
    }

    companion object {
        private val DOKUMENT_ID = ULID().nextULID()
        private val SOKNAD_ID = UUID.randomUUID()
    }
}
