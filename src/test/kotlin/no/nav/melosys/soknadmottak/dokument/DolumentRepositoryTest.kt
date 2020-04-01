package no.nav.melosys.soknadmottak.dokument

import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class DolumentRepositoryTest @Autowired constructor(
        val entityManager: TestEntityManager,
        val dokumentRepository: DokumentRepository) {

    @Test
    fun givenNyttDokument_whenLagret_thenFunnet() {
        val soknad = SoknadFactory.lagSoknad()
        entityManager.persist(soknad)
        val dokument = DokumentFactory.lagDokument(soknad)
        entityManager.persist(dokument)
        entityManager.flush()
        val funnet = dokumentRepository.findByIdOrNull(dokument.id)
        assertThat(funnet).isEqualTo(dokument)
    }
}