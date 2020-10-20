package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Kontakperson(
    val ansattHos: String,
    val harFullmakt: Boolean,
    val navn: String,
    val telefon: String
)