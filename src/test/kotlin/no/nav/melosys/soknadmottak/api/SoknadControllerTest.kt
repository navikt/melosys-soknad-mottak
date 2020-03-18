package no.nav.melosys.soknadmottak.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.melosys.soknadmottak.Soknad
import no.nav.melosys.soknadmottak.database.SoknadRepository
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
    private val soknadRepository: SoknadRepository
) {
    @TestConfiguration
    class SoknadControllerTestConfig {
        @Bean
        fun soknadRepository() = mockk<SoknadRepository>()
    }

    @Test
    fun `hent søknad som finnes, forvent søknad med innhold`() {
        every { soknadRepository.findById(123) } returns Optional.of(
            Soknad("ref", true, "<innhold>xml</innhold>", 123)
        )

        val result = mockMvc.get("/api/soknader/123") {
            accept(MediaType.APPLICATION_XML)
        }.andExpect {
            status { isOk }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_XML)
            }
        }.andReturn()

        verify { soknadRepository.findById(123) }
        result.response.contentAsString.let {
            assertThat(it).isEqualTo("<innhold>xml</innhold>")
        }
    }

    @Test
    fun `hent søknad som ikke finnes, forvent not found`() {
        every { soknadRepository.findById(123) } returns Optional.empty()

        mockMvc.get("/api/soknader/123") {
            accept(MediaType.APPLICATION_XML)
        }.andExpect {
            status { isNotFound }
        }

        verify { soknadRepository.findById(123) }
    }
}