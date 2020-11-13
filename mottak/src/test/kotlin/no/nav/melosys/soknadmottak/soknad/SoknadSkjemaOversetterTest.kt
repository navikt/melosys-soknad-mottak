package no.nav.melosys.soknadmottak.soknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SoknadSkjemaOversetterTest {
    private val søknad = SoknadFactory.lagSoknadFraXmlFil()

    @Test
    fun `mapping arbeidsgiver`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.arbeidsgiver.navn).isEqualTo("virksomhetsnavn")
        assertThat(felter.arbeidsgiver.adresse).isEqualTo("gate, 1234 poststed land")
        assertThat(felter.arbeidsgiver.orgnr).isEqualTo("virksomhetsnummer")

        assertThat(felter.virksomhetNorge.ansatte).isEqualTo(99)
        assertThat(felter.virksomhetNorge.administrativtAnsatte).isEqualTo(98)
        assertThat(felter.virksomhetNorge.utsendteArbeidstakere).isEqualTo(97)
        assertThat(felter.virksomhetNorge.andelOpptjent).isEqualTo(96)
        assertThat(felter.virksomhetNorge.andelOppdragskontrakter).isEqualTo(95)
        assertThat(felter.virksomhetNorge.andelOppdrag).isEqualTo(94)
        assertThat(felter.virksomhetNorge.andelRekruttert).isEqualTo(93)
    }

    @Test
    fun `mapping arbeidstaker`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.arbeidstaker.barnMed).isNotEmpty()
        assertThat(felter.arbeidstaker.barnMed[0].fnr).isEqualTo("barnFnr")
        assertThat(felter.arbeidstaker.barnMed[0].navn).isEqualTo("barnNavn")
        assertThat(felter.arbeidstaker.erMedBarnUnder18).isTrue()
        assertThat(felter.arbeidstaker.fulltNavn).isEqualTo("Fullt Navn")
        assertThat(felter.arbeidstaker.fnr).isEqualTo("foedselsnummer")
        assertThat(felter.arbeidstaker.foedeland).isEqualTo("foedeland")
        assertThat(felter.arbeidstaker.foedested).isEqualTo("foedested")
        assertThat(felter.arbeidstaker.utenlandskIDnummer).isEqualTo("utenlandskIDnummer")
    }

    @Test
    fun `mapping kontakperson`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.kontakperson!!.navn).isEqualTo("kontaktpersonNavn")
        assertThat(felter.kontakperson!!.telefon).isEqualTo("kontaktpersonTelefon")
        assertThat(felter.kontakperson!!.ansattHos).isEqualTo("I et rådgivningsfirma")
        assertThat(felter.kontakperson!!.fullmektigVirksomhetsnummer).isEqualTo("fullmektigVirksomhetsnummer")
        assertThat(felter.kontakperson!!.fullmektigVirksomhetsnavn).isEqualTo("fullmektigVirksomhetsnavn")
        assertThat(felter.kontakperson!!.harFullmakt).isTrue()
    }

    @Test
    fun `mapping utenlandsk oppdrag`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.utenlandsoppdrag.arbeidsland).isEqualTo("arbeidsland")
        assertThat(felter.utenlandsoppdrag.periode).isEqualTo("f.o.m. 2008-11-15 t.o.m. 2017-5-15")
        assertThat(felter.utenlandsoppdrag.erErstatning).isTrue()
        assertThat(felter.utenlandsoppdrag.samletUtsendingPeriode).isEqualTo("f.o.m. 2013-12-21 t.o.m. 2016-1-1")
        assertThat(felter.utenlandsoppdrag.erSendingForOppdrag).isFalse()
        assertThat(felter.utenlandsoppdrag.erDrattPaaEgetInitiativ).isFalse()
        assertThat(felter.utenlandsoppdrag.erAnsettelseForOpphold).isTrue()
        assertThat(felter.utenlandsoppdrag.erFortsattAnsattEtterOppdrag).isFalse()
    }

    @Test
    fun `mapping lønn og godtgjørelser`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.loennOgGodtgjoerelse.norskArbgUtbetalerLoenn).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.utlArbgUtbetalerLoenn).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.bruttoLoennPerMnd).isEqualTo("1000.00")
        assertThat(felter.loennOgGodtgjoerelse.bruttoLoennUtlandPerMnd).isEqualTo("1000.00")
        assertThat(felter.loennOgGodtgjoerelse.mottarNaturalytelser).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.samletVerdiNaturalytelser).isEqualTo("9876.55")
        assertThat(felter.loennOgGodtgjoerelse.erArbeidsgiveravgiftHelePerioden).isTrue()
        assertThat(felter.loennOgGodtgjoerelse.erTrukketTrygdeavgift).isFalse()
    }

    @Test
    fun `mapping andre felter`() {
        val felter = SoknadSkjemaOversetter().tilSøknadFelter(søknad)

        assertThat(felter.arbeidssted.type).isNotBlank() // FIXME
    }
}