package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidsgiver(
    val orgnr: String,
    val erOffenlig: Boolean,
    val navn: String,
    val adresse: Adresse?
)

data class Adresse(
    val gate: String?,
    val postkode: String?,
    val poststed: String?,
    val land: String?
)
