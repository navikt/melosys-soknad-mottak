package no.nav.melosys.soknadmottak.soknad

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SoknadRepository : JpaRepository<Soknad, Long> {
    fun findByArkivReferanse(ref: String): Iterable<Soknad>

    fun findBySoknadID(soknadID: UUID): Soknad?
}

