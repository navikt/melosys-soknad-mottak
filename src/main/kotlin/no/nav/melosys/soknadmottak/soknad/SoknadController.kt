package no.nav.melosys.soknadmottak.soknad

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
@RequestMapping("/soknader")
@Api(tags = ["soknader"])
class SoknadController @Autowired constructor(
    private val soknadService: SoknadService
) {
    @ApiOperation("Henter XML-innhold til en søknad med gitt ID")
    @GetMapping("{soknadID}", produces = [MediaType.APPLICATION_XML_VALUE])
    fun hentSøknad(@PathVariable soknadID: String): ResponseEntity<String> =
        ResponseEntity.ok(
            soknadService.hentSøknad(soknadID).innhold
        )
}
