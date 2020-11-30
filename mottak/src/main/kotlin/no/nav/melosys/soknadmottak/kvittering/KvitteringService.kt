package no.nav.melosys.soknadmottak.kvittering

import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.melosys.soknadmottak.kvittering.altinn.KorrespondanseService
import no.nav.melosys.soknadmottak.kvittering.altinn.Melding
import no.nav.melosys.soknadmottak.kvittering.altinn.Vedlegg
import org.springframework.stereotype.Service

@Service
class KvitteringService(private val korrespondanseService: KorrespondanseService) {
    companion object {
        private const val MELDING_SENDER = "NAV"
        private const val MELDING_EMNE = "Kvittering for søknad om A1"
        private const val MELDING_TEKST =
            "Du har sendt inn «Søknad om A1 for utsendte arbeidstakere innen EØS/Sveits». Kopi av søknaden er vedlagt."
        private const val FIL_NAVN = "Søknad om A1 for utsendte arbeidstakere innen EØS/Sveits"
        private const val LANGT_FREM_I_TID_ANTALL_ÅR = 20L
    }

    fun sendKvittering(
        mottakerID: String,
        arkivRef: String,
        vedlegg: ByteArray
    ) {
        korrespondanseService.sendMelding(
            lagKvittering(
                mottakerID,
                arkivRef,
                vedlegg
            )
        )
    }

    private fun lagKvittering(mottakerID: String, arkivRef: String, vedlegg: ByteArray): InsertCorrespondenceBasicV2 {
        return korrespondanseService.lagMelding(
            mottakerID,
            arkivRef,
            MELDING_SENDER,
            Melding(MELDING_EMNE, MELDING_TEKST, Vedlegg(FIL_NAVN, vedlegg)),
            LANGT_FREM_I_TID_ANTALL_ÅR
        )
    }
}
