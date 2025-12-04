package no.nav.melosys.soknadmottak.soknad

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import no.nav.melosys.soknadmottak.RepositoryBaseTest


class SoknadRepositoryTest: RepositoryBaseTest() {

    @Test
    fun givenNySoknad_whenLagret_thenFunnet() {
        val soknad = soknadRepository.save(Soknad("ref_altinn", false, "blech", Instant.now()))

        val found = soknadRepository.findByIdOrNull(soknad.id!!)

        found.run {
            this shouldBe soknad
        }

    }
}
