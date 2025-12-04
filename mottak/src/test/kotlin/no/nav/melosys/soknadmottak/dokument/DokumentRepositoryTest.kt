package no.nav.melosys.soknadmottak.dokument

import no.nav.melosys.soknadmottak.soknad.SoknadFactory
import no.nav.melosys.soknadmottak.soknad.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DokumentRepositoryTest @Autowired constructor(
    val soknadRepository: SoknadRepository,
    val dokumentRepository: DokumentRepository,
) {

    @Test
    fun givenNyttDokument_whenLagret_thenFunnet() {
        val soknad = soknadRepository.save(SoknadFactory.lagSoknad())

        val dokument = dokumentRepository.save(DokumentFactory.lagDokument(soknad))

        val funnet = dokumentRepository.findByIdOrNull(dokument.id!!)

        assertThat(funnet).isEqualTo(dokument)
        assertThat(funnet!!.lagretTidspunkt).isNotNull()
    }

}
