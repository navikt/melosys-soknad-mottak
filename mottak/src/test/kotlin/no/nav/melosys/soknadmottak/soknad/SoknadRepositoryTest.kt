package no.nav.melosys.soknadmottak.soknad

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SoknadRepositoryTest @Autowired constructor(
    val soknadRepository: SoknadRepository
) {

    @Test
    fun givenNySoknad_whenLagret_thenFunnet() {
        val soknad = soknadRepository.save(Soknad("ref_altinn", false, "blech", Instant.now()))

        val found = soknadRepository.findByIdOrNull(soknad.id!!)

        found.run {
            this shouldBe soknad
        }

    }
}
