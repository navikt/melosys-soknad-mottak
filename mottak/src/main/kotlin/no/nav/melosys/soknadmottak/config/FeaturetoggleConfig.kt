package no.nav.melosys.soknadmottak.config

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.FakeUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.strategy.Strategy
import no.finn.unleash.util.UnleashConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class FeaturetoggleConfig {

    @Bean
    fun unleash(environment: Environment): Unleash? {
        if (environment.activeProfiles.contains("test")) {
            return FakeUnleash()
        }

        val unleashConfig = UnleashConfig.builder()
            .appName("melosys-soknad-mottak")
            .unleashAPI("https://unleash.nais.io/api/")
            .build()
        return DefaultUnleash(
            unleashConfig,
            IsTestStrategy(environment.getProperty("APP_ENVIRONMENT"))
        )
    }

    internal class IsTestStrategy(private val env: String?) : Strategy {
        override fun getName(): String {
            return "isTest"
        }

        override fun isEnabled(map: Map<String, String>): Boolean {
            return "dev".equals(env, ignoreCase = true)
        }
    }
}
