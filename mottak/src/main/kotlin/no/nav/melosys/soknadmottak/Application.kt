package no.nav.melosys.soknadmottak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args).registerShutdownHook()
}
