package no.nav.melosys.soknadmottak.dokument

import de.huxhorn.sulky.ulid.ULID
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadFactory.lagSoknad
import java.time.Instant

object DokumentFactory {
    private val ulid: ULID = ULID()

    fun lagDokument(soknad: Soknad = lagSoknad(), innhold: ByteArray = ByteArray(10)): Dokument {
        return Dokument(soknad, "fil_navn", DokumentType.VEDLEGG, innhold, Instant.MIN, ulid.nextULID())
    }
}
