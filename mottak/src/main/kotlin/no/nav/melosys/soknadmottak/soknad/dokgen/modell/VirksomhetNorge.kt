package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class VirksomhetNorge(
    val erOffenlig: Boolean,
    val administrativtAnsatte: Int,
    val andelOppdrag: Int,
    val andelOppdragskontrakter: Int,
    val andelOpptjent: Int,
    val andelRekruttert: Int,
    val ansatte: Int,
    val utsendteArbeidstakere: Int
)