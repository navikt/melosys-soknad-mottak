package no.nav.melosys.soknadmottak.database

import no.nav.melosys.soknadmottak.SoknadMottak
import org.springframework.data.jpa.repository.JpaRepository

interface SoknadRepository : JpaRepository<SoknadMottak, Long> {
    fun findByArchiveReference(ref: String): Iterable<SoknadMottak>

    fun findBySoknadID(soknadID: String): SoknadMottak?
}

