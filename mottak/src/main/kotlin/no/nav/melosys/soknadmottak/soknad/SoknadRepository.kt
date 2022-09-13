package no.nav.melosys.soknadmottak.soknad

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface SoknadRepository : JpaRepository<Soknad, Long> {

    fun existsByArkivReferanse(ref: String): Boolean

    fun findByArkivReferanse(ref: String): Soknad?

    fun findBySoknadID(soknadID: UUID): Soknad?

    fun findByLevertFalse(): Iterable<Soknad>

    @Query("SELECT count(s) from Soknad s where s.levert = true")
    fun hentAntallSoknaderLevert() : Long

    @Query("SELECT count(s) from Soknad s where s.levert = false")
    fun hentAntallSoknaderIkkeLevert() : Long
}
