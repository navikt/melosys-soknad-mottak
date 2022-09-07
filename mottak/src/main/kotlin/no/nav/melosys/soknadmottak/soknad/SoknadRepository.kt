package no.nav.melosys.soknadmottak.soknad

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SoknadRepository : JpaRepository<Soknad, Long> {

    fun existsByArkivReferanse(ref: String): Boolean

    fun findByArkivReferanse(ref: String): Soknad?

    fun findBySoknadID(soknadID: UUID): Soknad?

    fun findByLevertFalse(): Iterable<Soknad>
}
