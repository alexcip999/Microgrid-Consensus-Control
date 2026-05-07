package presentation.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import infra.db.table.GridsTable
import infra.db.table.InvertersTable
import infra.db.table.SimulationsTable
import infra.db.table.TelemetryTable
import infra.db.table.UsersTable
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val config = HikariConfig().apply {
        jdbcUrl = environment.config.property("database.url").getString()
        username = environment.config.property("database.user").getString()
        password = environment.config.property("database.password").getString()
        driverClassName = environment.config.property("database.driver").getString()
        maximumPoolSize = environment.config.property("database.maxPoolSize")
            .getString().toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            GridsTable,
            InvertersTable,
            SimulationsTable,
            TelemetryTable
        )
    }

    log.info("Database connected and schema verified")
}