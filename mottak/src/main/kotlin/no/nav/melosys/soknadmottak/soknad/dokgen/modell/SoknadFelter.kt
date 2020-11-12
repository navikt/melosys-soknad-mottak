package no.nav.melosys.soknadmottak.soknad.dokgen.modell

data class SoknadFelter(
    val arbeidsgiver: Arbeidsgiver,
    val arbeidssted: Arbeidssted,
    val arbeidstaker: Arbeidstaker,
    val bruttoLoennPerMnd: String,
    val bruttoLoennUtlandPerMnd: String,
    val erArbeidsgiveravgiftHelePerioden: Boolean,
    val erForetakSammeKonsern: Boolean,
    val erLoennHelePerioden: Boolean,
    val erUfakturertLoennUtland: Boolean,
    val kontakperson: Kontakperson?,
    val tidspunktMottatt: String,
    val utenlandsoppdrag: Utenlandsoppdrag,
    val virksomhetNorge: VirksomhetNorge
)