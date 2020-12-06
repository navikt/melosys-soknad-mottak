package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidsgiver(
    val orgnr: String,
    val erOffenlig: Boolean,
    val navn: String,
    val adresse: String
)
