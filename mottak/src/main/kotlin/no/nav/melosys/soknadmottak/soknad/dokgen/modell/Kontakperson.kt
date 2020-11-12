package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Kontakperson(
    val navn: String,
    val telefon: String,
    val ansattHos: String,
    val harFullmakt: Boolean,
    val fullmektigVirksomhetsnummer: String,
    val fullmektigVirksomhetsnavn: String
)