package no.nav.melosys.soknadmottak.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import mu.KotlinLogging
import no.nav.melosys.soknadmottak.database.SoknadRepository
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger { }

@Protected
@RestController
@RequestMapping("/soknader")
@Api(tags = ["soknader"])
class SoknadController @Autowired constructor(
    private val soknadRepository: SoknadRepository
) {
    @ApiOperation("Henter xml-innhold til en søknad med gitt ID")
    @GetMapping("{soknadID}", produces = [MediaType.APPLICATION_XML_VALUE])
    fun hentSøknad(@PathVariable soknadID: Long): ResponseEntity<String> {
        logger.info { "Henter søknad med ID $soknadID" }
        return soknadRepository.findById(soknadID)
            .map { ResponseEntity.ok(it.content) }
            .orElseGet { ResponseEntity.notFound().build() }
    }
}