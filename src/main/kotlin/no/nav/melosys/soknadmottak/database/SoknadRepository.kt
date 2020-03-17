package no.nav.melosys.soknadmottak.database

import no.nav.melosys.soknadmottak.Soknad
import org.springframework.data.jpa.repository.JpaRepository

interface SoknadRepository : JpaRepository<Soknad, Long> {
    fun findByArchiveReference(ref: String): Iterable<Soknad>
}

