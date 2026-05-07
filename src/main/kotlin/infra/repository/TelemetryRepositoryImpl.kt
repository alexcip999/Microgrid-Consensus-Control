package infra.repository

import domain.model.telemetry.TelemetryEntry
import domain.repository.TelemetryRepository
import infra.db.entity.InverterEntity
import infra.db.entity.SimulationEntity
import infra.db.entity.TelemetryEntity
import infra.db.table.TelemetryTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class TelemetryRepositoryImpl : TelemetryRepository {

    override fun saveBatch(entries: List<TelemetryEntry>): List<TelemetryEntry> = transaction {
        entries.map { entry ->
            TelemetryEntity.new(entry.id) {
                simulation = SimulationEntity.findById(entry.simulationId)
                    ?: throw NoSuchElementException("Simulation not found: ${entry.simulationId}")
                inverter = InverterEntity.findById(entry.inverterId)
                    ?: throw NoSuchElementException("Inverter not found: ${entry.inverterId}")
                timestamp = entry.timestamp
                p = entry.p
                q = entry.q
                vMag = entry.vMag
                freq = entry.freq
                deltaOmega = entry.deltaOmega
                consensusError = entry.consensusError
                pNorm = entry.pNorm
                pProduction = entry.pProduction
                pLoad = entry.pLoad
                pNet = entry.pNet
            }.toDomain()
        }
    }

    override fun findBySimulation(simulationId: UUID): List<TelemetryEntry> = transaction {
        TelemetryEntity
            .find { TelemetryTable.simulationId eq simulationId }
            .orderBy(TelemetryTable.timestamp to SortOrder.ASC)
            .map { it.toDomain() }
    }

    override fun findBySimulationAndTimeRange(
        simulationId: UUID,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<TelemetryEntry> = transaction {
        TelemetryEntity
            .find {
                (TelemetryTable.simulationId eq simulationId) and
                        (TelemetryTable.timestamp greaterEq from) and
                        (TelemetryTable.timestamp lessEq to)
            }
            .orderBy(TelemetryTable.timestamp to SortOrder.ASC)
            .map { it.toDomain() }
    }

    override fun findBySimulationAndInverter(
        simulationId: UUID,
        inverterId: UUID
    ): List<TelemetryEntry> = transaction {
        TelemetryEntity
            .find {
                (TelemetryTable.simulationId eq simulationId) and
                        (TelemetryTable.inverterId eq inverterId)
            }
            .orderBy(TelemetryTable.timestamp to SortOrder.ASC)
            .map { it.toDomain() }
    }

    override fun findLatestPerInverter(simulationId: UUID): List<TelemetryEntry> = transaction {
        TelemetryEntity
            .find { TelemetryTable.simulationId eq simulationId }
            .orderBy(TelemetryTable.timestamp to SortOrder.DESC)
            .map { it.toDomain() }
            .distinctBy { it.inverterId }
    }
}