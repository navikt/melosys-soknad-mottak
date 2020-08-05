package no.nav.melosys.soknadmottak.dokument

import io.swagger.annotations.ApiOperation
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class DokumentController @Autowired constructor(
    private val dokumentService: DokumentService
) {
    @Deprecated(message = "Pdf som BASE64-string returneres i hentDokumenter")
    @ApiOperation("Henter pdf for et dokument med gitt ID", tags = ["dokumenter"])
    @GetMapping("/dokumenter/{dokumentID}", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun hentPdf(@PathVariable dokumentID: String): ResponseEntity<ByteArray> {
        val dokument = dokumentService.hentDokument(dokumentID)
        return ResponseEntity.ok()
            .header(CONTENT_DISPOSITION, "attachment; filename=${dokument.filnavn}")
            .body(dokument.innhold)
    }

    @ApiOperation("Henter dokumenter for en søknad med gitt ID", tags = ["soknader", "dokumenter"])
    @GetMapping("/soknader/{soknadID}/dokumenter", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentDokumenter(@PathVariable soknadID: String): ResponseEntity<List<DokumentDto>> =
        ResponseEntity.ok(
            dokumentService.hentDokumenterForSoknad(soknadID).map { dok -> DokumentDto(dok) }
        )
}
