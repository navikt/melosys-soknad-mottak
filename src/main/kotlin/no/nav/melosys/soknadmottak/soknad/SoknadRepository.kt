package no.nav.melosys.soknadmottak.soknad

import org.springframework.data.jpa.repository.JpaRepository

interface SoknadRepository : JpaRepository<Soknad, Long> {
    fun findByArkivReferanse(ref: String): Iterable<Soknad>

    fun findBySoknadID(soknadID: String): Soknad?
}

