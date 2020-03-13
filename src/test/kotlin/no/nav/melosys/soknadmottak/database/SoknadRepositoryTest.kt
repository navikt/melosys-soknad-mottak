package no.nav.melosys.soknadmottak.database;

import no.nav.melosys.soknadmottak.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class SoknadRepositoryTest @Autowired constructor(
        val entityManager: TestEntityManager,
        val soknadRepository: SoknadRepository) {

    @Test
    fun givenNewSoknad_whenSaved_thenFound() {
        val soknad = Soknad("ref_altinn","blech")
        entityManager.persist(soknad)
        entityManager.flush()
        val found = soknadRepository.findByIdOrNull(soknad.id)
        assertThat(found).isEqualTo(soknad)
    }
}