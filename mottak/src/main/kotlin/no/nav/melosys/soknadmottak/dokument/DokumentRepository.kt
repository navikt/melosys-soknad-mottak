package no.nav.melosys.soknadmottak.dokument

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface DokumentRepository : JpaRepository<Dokument, Long> {
    fun findByDokumentID(dokID: String): Dokument?
    fun findBySoknadSoknadID(soknadID: UUID): Iterable<Dokument>
}
