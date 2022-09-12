package no.nav.melosys.soknadmottak.kopi

import mu.KotlinLogging
import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.melosys.soknadmottak.common.IntegrasjonException
import no.nav.melosys.soknadmottak.kopi.altinn.KorrespondanseService
import no.nav.melosys.soknadmottak.kopi.altinn.Melding
import no.nav.melosys.soknadmottak.kopi.altinn.Vedlegg
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class KopiService(private val korrespondanseService: KorrespondanseService) {
    companion object {
        private const val MELDING_SENDER = "NAV"
        private const val MELDING_EMNE = "Bekreftelse på innsendt søknad om A1"
        private const val MELDING_TEKST_KOPI =
            "Du har sendt inn «Søknad om A1 for utsendte arbeidstakere innen EØS/Sveits». Kopi av søknaden er vedlagt."
        private const val MELDING_TEKST_ALTINN_REF = "Altinn-referanse:"
        private const val FIL_NAVN = "Søknad om A1 for utsendte arbeidstakere innen EØS/Sveits"
        private const val LANGT_FREM_I_TID_ANTALL_ÅR = 20L
    }

    fun sendKopi(
        mottakerID: String,
        arkivRef: String,
        vedlegg: ByteArray
    ) {
        try {
            korrespondanseService.sendMelding(
                lagKopi(
                    mottakerID,
                    arkivRef,
                    vedlegg
                )
            )
        } catch (t: Throwable) {
            throw IntegrasjonException("Kunne ikke kvittere for arkiv '$arkivRef'", t)
        }
        logger.info { "Sendt kopi for arkiv '$arkivRef'" }
    }

    fun lagKopi(mottakerID: String, arkivRef: String, vedlegg: ByteArray): InsertCorrespondenceBasicV2 {
        return korrespondanseService.lagMelding(
            mottakerID,
            arkivRef,
            MELDING_SENDER,
            Melding(MELDING_EMNE, lagMeldingTekst(arkivRef), Vedlegg(FIL_NAVN, vedlegg)),
            LANGT_FREM_I_TID_ANTALL_ÅR
        )
    }

    private fun lagMeldingTekst(arkivRef: String): String = "$MELDING_TEKST_KOPI $MELDING_TEKST_ALTINN_REF $arkivRef"
}
