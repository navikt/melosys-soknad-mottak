package no.nav.melosys.soknadmottak.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
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
import javax.sql.DataSource

@Profile("prod")
@Configuration
@EnableJpaRepositories(basePackages = ["no.nav.melosys.soknadmottak"])
class DatabaseConfig(private val environment: Environment) {
    private val logger = KotlinLogging.logger {}

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
    fun adminDataSource(): DataSource = dataSource()

    @Primary
    @Bean
    fun userDataSource(): DataSource = dataSource()

    @Bean
    fun flywayConfig(@Qualifier("adminDataSource") adminDataSource: DataSource): FlywayConfigurationCustomizer =
        FlywayConfigurationCustomizer { it.dataSource(adminDataSource) }

    private fun dataSource(): HikariDataSource {
        val jdbcUrl = environment.getRequiredProperty("spring.datasource.url")
        val username = environment.getRequiredProperty("spring.datasource.username")
        val password = environment.getRequiredProperty("spring.datasource.password")

        logger.warn { // temp visibility in tests; remove before prod
            "DB_CONFIG url=$jdbcUrl user=$username passwordSet=${password.isNotBlank()} " +
                "cluster=${environment.getProperty("NAIS_CLUSTER_NAME")}"
        }

        return HikariDataSource(
            HikariConfig().apply {
                this.jdbcUrl = jdbcUrl
                maximumPoolSize = 3
                minimumIdle = 1
                this.username = username
                this.password = password
            }
        )
    }
}
