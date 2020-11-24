package no.nav.melosys.soknadmottak.soknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SoknadSkjemaOversetterTest {
    private var søknad = SoknadFactory.lagSoknadFraXmlFil()
    private var flettedata = SoknadSkjemaOversetter.tilFlettedata(søknad)

    @Test
    fun `mapping tidspunktMottatt `() {
        assertThat(flettedata.tidspunktMottatt).isEqualTo(søknad.innsendtTidspunkt.toString())
    }

    @Test
    fun `mapping arbeidsgiver`() {
        assertThat(flettedata.arbeidsgiver.navn).isEqualTo("virksomhetsnavn")
        assertThat(flettedata.arbeidsgiver.adresse).isEqualTo("gate, 1234 poststed land")
        assertThat(flettedata.arbeidsgiver.orgnr).isEqualTo("virksomhetsnummer")

        assertThat(flettedata.virksomhetNorge.ansatte).isEqualTo(99)
        assertThat(flettedata.virksomhetNorge.administrativtAnsatte).isEqualTo(98)
        assertThat(flettedata.virksomhetNorge.utsendteArbeidstakere).isEqualTo(97)
        assertThat(flettedata.virksomhetNorge.andelOpptjent).isEqualTo(96)
        assertThat(flettedata.virksomhetNorge.andelOppdragskontrakter).isEqualTo(95)
        assertThat(flettedata.virksomhetNorge.andelOppdrag).isEqualTo(94)
        assertThat(flettedata.virksomhetNorge.andelRekruttert).isEqualTo(93)
    }

    @Test
    fun `mapping arbeidstaker`() {
        assertThat(flettedata.arbeidstaker.barnMed).isNotEmpty()
        assertThat(flettedata.arbeidstaker.barnMed[0].fnr).isEqualTo("barnFnr")
        assertThat(flettedata.arbeidstaker.barnMed[0].navn).isEqualTo("barnNavn")
        assertThat(flettedata.arbeidstaker.erMedBarnUnder18).isTrue()
        assertThat(flettedata.arbeidstaker.fulltNavn).isEqualTo("Fullt Navn")
        assertThat(flettedata.arbeidstaker.fnr).isEqualTo("foedselsnummer")
        assertThat(flettedata.arbeidstaker.foedeland).isEqualTo("foedeland")
        assertThat(flettedata.arbeidstaker.foedested).isEqualTo("foedested")
        assertThat(flettedata.arbeidstaker.utenlandskIDnummer).isEqualTo("utenlandskIDnummer")
    }

    @Test
    fun `mapping kontakperson`() {
        assertThat(flettedata.kontakperson!!.navn).isEqualTo("kontaktpersonNavn")
        assertThat(flettedata.kontakperson!!.telefon).isEqualTo("kontaktpersonTelefon")
        assertThat(flettedata.kontakperson!!.ansattHos).isEqualTo("I et rådgivningsfirma")
        assertThat(flettedata.kontakperson!!.fullmektigVirksomhetsnummer).isEqualTo("fullmektigVirksomhetsnummer")
        assertThat(flettedata.kontakperson!!.fullmektigVirksomhetsnavn).isEqualTo("fullmektigVirksomhetsnavn")
        assertThat(flettedata.kontakperson!!.harFullmakt).isTrue()
    }

    @Test
    fun `mapping arbeidssted arbeidPaaLand`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_arbeidPaaLand.xml")
        val flettedata = SoknadSkjemaOversetter.tilFlettedata(soknadFraXmlFil)

        assertThat(flettedata.arbeidssted.type).isEqualTo("arbeidPaaLand")
        val arbeidPaaLand = flettedata.arbeidssted.arbeidPaaLand
        assertThat(arbeidPaaLand).isNotNull()
        assertThat(arbeidPaaLand!!.fastArbeidssted).isFalse()
        assertThat(arbeidPaaLand.hjemmekontor).isTrue()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder).isNotEmpty()
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].firmanavn).isEqualTo("firmanavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].gatenavn).isEqualTo("gatenavn")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].by).isEqualTo("by")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].postkode).isEqualTo("postkode")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].region).isEqualTo("region")
        assertThat(arbeidPaaLand.fysiskeArbeidssteder[0].land).isEqualTo("land")
    }

    @Test
    fun `mapping arbeidssted offshoreEnheter`() {
        assertThat(flettedata.arbeidssted.type).isEqualTo("offshoreEnheter")
        val offshoreEnheter = flettedata.arbeidssted.offshoreEnheter!!.offshoreEnheter
        assertThat(offshoreEnheter).isNotEmpty()
        assertThat(offshoreEnheter[0].enhetsNavn).isEqualTo("Askepott")
        assertThat(offshoreEnheter[0].sokkelLand).isEqualTo("Polen")
        assertThat(offshoreEnheter[0].enhetsType).isEqualTo("annenStasjonaerEnhet")
    }

    @Test
    fun `mapping arbeidssted luftfart`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_luftfart.xml")
        val flettedata = SoknadSkjemaOversetter.tilFlettedata(soknadFraXmlFil)

        assertThat(flettedata.arbeidssted.type).isEqualTo("luftfart")
        val luftfartBaser = flettedata.arbeidssted.luftfart!!.luftfartBaser
        assertThat(luftfartBaser).isNotEmpty()
        assertThat(luftfartBaser[0].hjemmebaseNavn).isEqualTo("hjemmebaseNavn")
        assertThat(luftfartBaser[0].hjemmebaseLand).isEqualTo("hjemmebaseLand")
        assertThat(luftfartBaser[0].typeFlyvninger).isEqualTo("nasjonal")
    }

    @Test
    fun `mapping arbeidssted skipsliste`() {
        val soknadFraXmlFil = SoknadFactory.lagSoknadFraXmlFil("søknad_skipListe.xml")
        val flettedata = SoknadSkjemaOversetter.tilFlettedata(soknadFraXmlFil)

        assertThat(flettedata.arbeidssted.type).isEqualTo("skipListe")
        val skipListe = flettedata.arbeidssted.skipListe!!.skipListe
        assertThat(skipListe).isNotEmpty()
        assertThat(skipListe[0].skipNavn).isEqualTo("skipNavn")
        assertThat(skipListe[0].fartsomraade).isEqualTo("utenriks")
        assertThat(skipListe[0].flaggland).isEqualTo("flaggland")
        assertThat(skipListe[0].territorialEllerHavnLand).isEqualTo("havnLand")
    }

    @Test
    fun `mapping utenlandsk oppdrag`() {
        assertThat(flettedata.utenlandsoppdrag.arbeidsland).isEqualTo("arbeidsland")
        assertThat(flettedata.utenlandsoppdrag.periode).isEqualTo("f.o.m. 2008-11-15 t.o.m. 2017-5-15")
        assertThat(flettedata.utenlandsoppdrag.erErstatning).isTrue()
        assertThat(flettedata.utenlandsoppdrag.samletUtsendingPeriode).isEqualTo("f.o.m. 2013-12-21 t.o.m. 2016-1-1")
        assertThat(flettedata.utenlandsoppdrag.erSendingForOppdrag).isFalse()
        assertThat(flettedata.utenlandsoppdrag.erDrattPaaEgetInitiativ).isFalse()
        assertThat(flettedata.utenlandsoppdrag.erAnsettelseForOpphold).isTrue()
        assertThat(flettedata.utenlandsoppdrag.erFortsattAnsattEtterOppdrag).isFalse()
    }

    @Test
    fun `mapping lønn og godtgjørelser`() {
        assertThat(flettedata.loennOgGodtgjoerelse.norskArbgUtbetalerLoenn).isTrue()
        assertThat(flettedata.loennOgGodtgjoerelse.utlArbgUtbetalerLoenn).isTrue()
        assertThat(flettedata.loennOgGodtgjoerelse.bruttoLoennPerMnd).isEqualTo("1000.00")
        assertThat(flettedata.loennOgGodtgjoerelse.bruttoLoennUtlandPerMnd).isEqualTo("1000.00")
        assertThat(flettedata.loennOgGodtgjoerelse.mottarNaturalytelser).isTrue()
        assertThat(flettedata.loennOgGodtgjoerelse.samletVerdiNaturalytelser).isEqualTo("9876.55")
        assertThat(flettedata.loennOgGodtgjoerelse.erArbeidsgiveravgiftHelePerioden).isTrue()
        assertThat(flettedata.loennOgGodtgjoerelse.erTrukketTrygdeavgift).isFalse()
    }

    @Test
    fun `mapping arbeidssituasjon`() {
        assertThat(flettedata.arbeidssituasjon.loennetArbeidMinstEnMnd).isFalse()
        assertThat(flettedata.arbeidssituasjon.beskrivArbeidSisteMnd).isEqualTo("Universell konsulent")
        assertThat(flettedata.arbeidssituasjon.andreArbeidsgivereIUtsendingsperioden).isTrue()
        assertThat(flettedata.arbeidssituasjon.beskrivelseAnnetArbeid).isEqualTo("Tankeleser")
        assertThat(flettedata.arbeidssituasjon.erSkattepliktig).isFalse()
        assertThat(flettedata.arbeidssituasjon.mottaYtelserNorge).isTrue()
        assertThat(flettedata.arbeidssituasjon.mottaYtelserUtlandet).isTrue()
    }
}