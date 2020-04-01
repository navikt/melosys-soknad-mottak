package no.nav.melosys.soknadmottak.dokument

import de.huxhorn.sulky.ulid.ULID
import no.nav.melosys.soknadmottak.soknad.Soknad
import no.nav.melosys.soknadmottak.soknad.SoknadFactory.lagSoknad

object DokumentFactory {
    val ulid: ULID = ULID()

    fun lagDokument(soknad: Soknad = lagSoknad(), innhold: ByteArray = ByteArray(10)): Dokument {
        return Dokument(soknad, "fil_navn", DokumentType.VEDLEGG, innhold, ulid.nextULID())
    }
}
