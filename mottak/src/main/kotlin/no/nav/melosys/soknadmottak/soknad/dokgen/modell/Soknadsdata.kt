package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Soknadsdata(
    val arbeidsgiver: Arbeidsgiver,
    val arbeidssituasjon: Arbeidssituasjon,
    val arbeidssted: Arbeidssted,
    val arbeidstaker: Arbeidstaker,
    val kontakperson: Kontakperson?,
    val loennOgGodtgjoerelse: LoennOgGodtgjoerelse,
    val tidspunktMottatt: String,
    val utenlandskVirksomhet: UtenlandskVirksomhet?,
    val utenlandsoppdrag: Utenlandsoppdrag,
    val virksomhetNorge: VirksomhetNorge?,
)
