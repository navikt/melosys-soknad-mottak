package no.nav.melosys.soknadmottak.soknad.dokgen

import no.nav.melosys.soknadmottak.soknad.dokgen.modell.*
import java.time.Instant

data class SoknadFelterBuilder(
    val arbeidsgiver: Arbeidsgiver = ArbeidsgiverBuilder().build(),
    val arbeidssted: Arbeidssted = ArbeidsstedBuilder().build(),
    val arbeidstaker: Arbeidstaker = ArbeidstakerBuilder().build(),
    val bruttoLoennPerMnd: String = "",
    val bruttoLoennUtlandPerMnd: String = "",
    val erArbeidsgiveravgiftHelePerioden: Boolean = false,
    val erForetakSammeKonsern: Boolean = false,
    val erLoennHelePerioden: Boolean = false,
    val erUfakturertLoennUtland: Boolean = false,
    val kontakperson: Kontakperson = KontakpersonBuilder().build(),
    val tidspunktMottatt: String = Instant.now().toString(),
    val utenlandsoppdrag: Utenlandsoppdrag = UtenlandsoppdragBuilder().build(),
    val virksomhetNorge: VirksomhetNorge = VirksomhetNorgeBuilder().build()
) {
    fun build(): SoknadFelter {
        return SoknadFelter(
            arbeidsgiver = arbeidsgiver,
            arbeidssted = arbeidssted,
            arbeidstaker = arbeidstaker,
            bruttoLoennPerMnd = bruttoLoennPerMnd,
            bruttoLoennUtlandPerMnd = bruttoLoennUtlandPerMnd,
            erArbeidsgiveravgiftHelePerioden = erArbeidsgiveravgiftHelePerioden,
            erForetakSammeKonsern = erForetakSammeKonsern,
            erLoennHelePerioden = erLoennHelePerioden,
            erUfakturertLoennUtland = erUfakturertLoennUtland,
            kontakperson = kontakperson,
            tidspunktMottatt = tidspunktMottatt,
            utenlandsoppdrag = utenlandsoppdrag,
            virksomhetNorge = virksomhetNorge
        )
    }
}

data class ArbeidsgiverBuilder(
    val adresse: String = "",
    val navn: String = "",
    val orgnr: String = ""
) {
    fun build(): Arbeidsgiver {
        return Arbeidsgiver(
            adresse = adresse,
            navn = navn,
            orgnr = orgnr
        )
    }
}

data class ArbeidsstedBuilder(
    val innretningNavn: String = "",
    val innretningType: String = "",
    val sokkelLand: String = "",
    val type: String = ""
) {
    fun build(): Arbeidssted {
        return Arbeidssted(
            innretningNavn = innretningNavn,
            innretningType = innretningType,
            sokkelLand = sokkelLand,
            type = type
        )
    }
}

data class ArbeidstakerBuilder(
    val barnMed: List<BarnMed> = listOf(),
    val erMedBarnUnder18: Boolean = false,
    val etternavn: String = "",
    val fnr: String = "",
    val foedeland: String = "",
    val foedsted: String = "",
    val fornavn: String = ""
) {
    fun build(): Arbeidstaker {
        return Arbeidstaker(
            barnMed = barnMed,
            erMedBarnUnder18 = erMedBarnUnder18,
            etternavn = etternavn,
            fnr = fnr,
            foedeland = foedeland,
            foedsted = foedsted,
            fornavn = fornavn
        )
    }
}

data class KontakpersonBuilder(
    val ansattHos: String = "",
    val harFullmakt: Boolean = false,
    val navn: String = "",
    val telefon: String = ""
) {
    fun build(): Kontakperson {
        return Kontakperson(
            ansattHos = ansattHos,
            harFullmakt = harFullmakt,
            navn = navn,
            telefon = telefon
        )
    }
}

data class UtenlandsoppdragBuilder(
    val arbeidsland: String = "",
    val erAnsattHelePeriode: Boolean = false,
    val erAnsettelseForOpphold: Boolean = false,
    val erErstatning: Boolean = false,
    val erSendingForOppdrag: Boolean = false,
    val periode: String = ""
) {
    fun build(): Utenlandsoppdrag {
        return Utenlandsoppdrag(
            arbeidsland = arbeidsland,
            erAnsattHelePeriode = erAnsattHelePeriode,
            erAnsettelseForOpphold = erAnsettelseForOpphold,
            erErstatning = erErstatning,
            erSendingForOppdrag = erSendingForOppdrag,
            periode = periode
        )
    }
}

data class VirksomhetNorgeBuilder(
    val administrativtAnsatte: Int = 0,
    val andelOppdrag: Int = 0,
    val andelOppdragskontrakter: Int = 0,
    val andelOpptjent: Int = 0,
    val andelRekruttert: Int = 0,
    val ansatte: Int = 0,
    val erOffenlig: Boolean = false,
    val utsendteArbeidstakere: Int = 0
) {
    fun build(): VirksomhetNorge {
        return VirksomhetNorge(
            administrativtAnsatte = administrativtAnsatte,
            andelOppdrag = andelOppdrag,
            andelOppdragskontrakter = andelOppdragskontrakter,
            andelOpptjent = andelOpptjent,
            andelRekruttert = andelRekruttert,
            ansatte = ansatte,
            erOffenlig = erOffenlig,
            utsendteArbeidstakere = utsendteArbeidstakere
        )
    }
}