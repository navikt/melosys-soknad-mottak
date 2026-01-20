package no.nav.melosys.soknadmottak.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
        FlywayConfigurationCustomizer { it.dataSource(adminDataSource) }

    private fun dataSource(user: String): HikariDataSource =
        HikariConfig().apply {
            jdbcUrl = environment.getRequiredProperty("spring.datasource.url")
            maximumPoolSize = 3
            minimumIdle = 1
            username = environment.getRequiredProperty("spring.datasource.username")
            password = environment.getRequiredProperty("spring.datasource.password")
        }.let { config -> HikariDataSource(config) }
}
