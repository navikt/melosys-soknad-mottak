package no.nav.melosys.soknadmottak.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Profile("!test")
@Configuration
@EnableJpaRepositories(basePackages = ["no.nav.melosys.soknadmottak"])
class DatabaseConfig(private val environment: Environment) {
    companion object {
        private const val PROD_MOUNT_PATH = "postgresql/prod-fss"
        private const val PREPROD_MOUNT_PATH = "postgresql/preprod-fss"
    }

    private val isProduction: Boolean =
        environment.getProperty("NAIS_CLUSTER_NAME")?.equals("prod-fss", ignoreCase = true) ?: false

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            dataSource = userDataSource()
            setPackagesToScan("no.nav.melosys.soknadmottak")

            jpaVendorAdapter = HibernateJpaVendorAdapter().apply {
                setDatabase(Database.POSTGRESQL)
            }
        }

    @Bean(name = ["transactionManager"])
    fun jpaTransactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory)

    @Bean
    fun adminDataSource(): DataSource {
        return dataSource("admin")
    }

    @Primary
    @Bean
    fun userDataSource(): DataSource {
        return dataSource("user")
    }

    @Bean
    fun flywayConfig(@Qualifier("adminDataSource") adminDataSource: DataSource): FlywayConfigurationCustomizer =
        FlywayConfigurationCustomizer {
            it.initSql("SET ROLE \"${environment.getRequiredProperty("DATABASE_NAME")}-admin\"")
                .dataSource(adminDataSource)
        }

    private fun dataSource(user: String): HikariDataSource =
        HikariConfig().apply {
            jdbcUrl = environment.getProperty("spring.datasource.url")
            maximumPoolSize = 3
            minimumIdle = 1
        }.let { config ->
            HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, getMountPath(), dbRole(user))
        }

    private fun getMountPath() = when {
        isProduction -> PROD_MOUNT_PATH
        else -> PREPROD_MOUNT_PATH
    }

    private fun dbRole(role: String) = arrayOf(environment.getProperty("DATABASE_NAME"), role).joinToString("-")
}
