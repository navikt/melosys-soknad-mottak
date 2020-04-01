package no.nav.melosys.soknadmottak.soknad

object SoknadFactory {
    fun lagSoknad(id: Long? = null): Soknad {
        return Soknad("referanse", false, "innhold", id)
    }
}
