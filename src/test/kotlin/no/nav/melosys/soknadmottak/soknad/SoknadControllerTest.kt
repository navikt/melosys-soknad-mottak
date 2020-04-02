package no.nav.melosys.soknadmottak.soknad

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

@WebMvcTest(SoknadController::class)
class SoknadControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val soknadService: SoknadService
) {
    @TestConfiguration
    class SoknadControllerTestConfig {
        @Bean
        fun altinnSoknadService() = mockk<SoknadService>()
    }

    @Test
    fun `hent søknad som finnes, forvent søknad med innhold`() {
        every { soknadService.hentSøknad(SOKNAD_ID) } returns
                Soknad(
                    "ref", true, "<innhold>xml</innhold>", 123,
                    SOKNAD_ID
                )

        val result = mockMvc.get("/api/soknader/$SOKNAD_ID") {
            accept(MediaType.APPLICATION_XML)
        }.andExpect {
            status { isOk }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_XML)
            }
        }.andReturn()

        verify { soknadService.hentSøknad(SOKNAD_ID) }
        result.response.contentAsString.let {
            assertThat(it).isEqualTo("<innhold>xml</innhold>")
        }
    }

    @Test
    fun `hent søknad som ikke finnes, forvent not found`() {
        every { soknadService.hentSøknad(SOKNAD_ID) } throws IkkeFunnetException("Søknad ikke funnet")

        mockMvc.get("/api/soknader/$SOKNAD_ID") {
            accept(MediaType.APPLICATION_XML)
        }.andExpect {
            status { isNotFound }
        }

        verify { soknadService.hentSøknad(SOKNAD_ID) }
    }

    companion object {
        private val SOKNAD_ID = UUID.randomUUID().toString()
    }
}