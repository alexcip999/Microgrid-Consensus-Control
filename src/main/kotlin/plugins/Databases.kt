package com.microgrid.plugins

import com.microgrid.model.tableobj.Alerts
import com.microgrid.model.tableobj.Grids
import com.microgrid.model.tableobj.Inverters
import com.microgrid.model.tableobj.Simulations
import com.microgrid.model.tableobj.TelemetryEntries
import com.microgrid.model.tableobj.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
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
        arrayOf<Table>(Users, Grids, Inverters, Simulations, TelemetryEntries, Alerts)
        Unit
    }

    log.info("Database connected and schema verified")
}