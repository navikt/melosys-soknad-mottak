package no.nav.melosys.soknadmottak.dokument

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
@RequestMapping("/dokumenter")
@Api(tags = ["dokumenter"])
class DokumentController @Autowired constructor(
    private val dokumentService: DokumentService
) {
    @ApiOperation("Henter pdf for et dokument med gitt ID")
    @GetMapping("{dokumentID}", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun hentPdf(@PathVariable dokumentID: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok(
            dokumentService.hentDokument(dokumentID).innhold
        )

    @ApiOperation("Henter dokumenter for en søknad med gitt ID")
    @GetMapping("{soknadID}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentVedlegg(@PathVariable soknadID: String): ResponseEntity<List<DokumentDto>> =
        ResponseEntity.ok(
            dokumentService.hentDokumenterForSoknad(soknadID).map { dok -> DokumentDto(dok) }
        )
}
