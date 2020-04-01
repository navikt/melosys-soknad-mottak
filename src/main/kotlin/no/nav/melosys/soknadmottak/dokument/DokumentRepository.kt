package no.nav.melosys.soknadmottak.dokument

import org.springframework.data.jpa.repository.JpaRepository

interface DokumentRepository : JpaRepository<Dokument, Long> {
    fun findByDokumentID(dokID: String): Dokument?
    fun findBySoknadSoknadID(soknadID: String): Iterable<Dokument>
}

