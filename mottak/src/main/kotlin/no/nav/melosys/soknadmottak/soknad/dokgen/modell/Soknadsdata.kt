package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Soknadsdata(
    val arbeidsgiver: Arbeidsgiver,
    val arbeidssted: Arbeidssted,
    val arbeidstaker: Arbeidstaker,
    val loennOgGodtgjoerelse: LoennOgGodtgjoerelse,
    val kontakperson: Kontakperson?,
    val tidspunktMottatt: String,
    val utenlandsoppdrag: Utenlandsoppdrag,
    val virksomhetNorge: VirksomhetNorge,
    val arbeidssituasjon: Arbeidssituasjon
)