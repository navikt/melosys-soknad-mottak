package no.nav.melosys.soknadmottak.soknad

import java.time.Instant

object SoknadFactory {
    fun lagSoknad(id: Long? = null, innhold: String = "innhold"): Soknad {
        return Soknad("referanse", false, innhold, Instant.now(), Instant.MIN, id)
    }

    fun lagSoknadFraXmlFil(): Soknad {
        return lagSoknadFraXmlFil("NAV_MedlemskapArbeidEOS.xml")
    }

    fun lagSoknadFraXmlFil(filNavn: String): Soknad {
        val søknadXML = javaClass.getResource("/$filNavn").readText()
        return lagSoknad(1, søknadXML)
    }
}
