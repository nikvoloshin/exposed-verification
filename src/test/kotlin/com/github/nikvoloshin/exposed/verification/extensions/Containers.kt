package com.github.nikvoloshin.exposed.verification.extensions

import com.github.nikvoloshin.exposed.verification.extensions.ContainerDescription.DatabaseVendor.*
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer

private val supportedDatabases = listOf(
    ContainerDescription(MYSQL, "mysql:8.0"),
    ContainerDescription(POSTGRES, "postgres:12.2"),
    ContainerDescription(MSSQL, "mcr.microsoft.com/mssql/server:2019-CU4-ubuntu-16.04")
)

internal val databases by lazy {
    supportedDatabases.map { description ->
        val name = description.vendor.name

        val container = createContainer(description).also {
            // MySQL container takes about 5 minutes to start up
            it.withStartupTimeoutSeconds(600)
            it.start()
        }

        val db = Database.connect(container.jdbcUrl, container.driverClassName, container.username, container.password)

        name to db
    }
}

private fun createContainer(description: ContainerDescription): JdbcDatabaseContainer<Nothing> =
    when (description.vendor) {
        MYSQL -> MySQLContainer(description.dockerImageName)
        POSTGRES -> PostgreSQLContainer(description.dockerImageName)
        MSSQL -> MSSQLServerContainer(description.dockerImageName)
    }

private data class ContainerDescription(val vendor: DatabaseVendor, val dockerImageName: String) {
    enum class DatabaseVendor {
        MYSQL, POSTGRES, MSSQL
    }
}
