package no.nav.melosys.soknadmottak.soknad

import java.time.Instant

object SoknadFactory {
    fun lagSoknad(id: Long? = null): Soknad {
        return Soknad("referanse", false, "innhold", Instant.MIN, id)
    }
}
