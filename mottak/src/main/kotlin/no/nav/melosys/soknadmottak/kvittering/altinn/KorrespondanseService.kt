package no.nav.melosys.soknadmottak.kvittering.altinn

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.melosys.soknadmottak.config.AltinnConfig
import org.springframework.stereotype.Service
import java.util.*

private const val SYSTEM_USERCODE = "NAV_MELO"

@Service
class KorrespondanseService(
    private val altinnConfig: AltinnConfig,
    private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic
) {
    fun sendMelding(insertCorrespondence: InsertCorrespondenceBasicV2) {
        iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
            insertCorrespondence.systemUserName,
            insertCorrespondence.systemPassword,
            insertCorrespondence.systemUserCode,
            insertCorrespondence.externalShipmentReference,
            insertCorrespondence.correspondence
        )
    }

    fun lagMelding(
        mottakerID: String,
        arkivRef: String,
        avsender: String,
        melding: Melding,
        varighetÅr: Long
    ): InsertCorrespondenceBasicV2 {
        return InsertCorrespondenceBasicV2()
            .withSystemUserName(altinnConfig.username)
            .withSystemPassword(altinnConfig.password)
            .withSystemUserCode(SYSTEM_USERCODE)
            .withExternalShipmentReference(UUID.randomUUID().toString())
            .withCorrespondence(
                CorrespondenceFactory.insertCorrespondence(
                    altinnConfig.correspondence.code,
                    altinnConfig.correspondence.editionCode,
                    mottakerID,
                    arkivRef,
                    avsender,
                    melding,
                    varighetÅr
                )
            )
    }
}

data class Melding(
    val tittel: String,
    val tekst: String,
    val vedlegg: Vedlegg
)

data class Vedlegg(
    val tittel: String,
    val fil: ByteArray
)
