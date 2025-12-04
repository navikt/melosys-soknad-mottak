package no.nav.melosys.soknadmottak

import io.kotest.core.spec.style.AnnotationSpec.AfterEach
import no.nav.melosys.soknadmottak.dokument.DokumentRepository
import no.nav.melosys.soknadmottak.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
abstract class RepositoryBaseTest {

    @Autowired
    lateinit var soknadRepository: SoknadRepository

    @Autowired
    lateinit var dokumentRepository: DokumentRepository

    @AfterEach
    fun clean() {
        dokumentRepository.deleteAll()
        soknadRepository.deleteAll()
    }
}
