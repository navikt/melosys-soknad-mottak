package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class Arbeidssituasjon(
    val andreArbeidsgivereIUtsendingsperioden: Boolean,
    val beskrivArbeidSisteMnd: String?,
    val beskrivelseAnnetArbeid: String?,
    val erSkattepliktig: Boolean,
    val loennetArbeidMinstEnMnd: Boolean,
    val mottaYtelserNorge: Boolean,
    val mottaYtelserUtlandet: Boolean
)
