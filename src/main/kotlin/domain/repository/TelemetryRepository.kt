package domain.repository

import domain.model.telemetry.TelemetryEntry
import java.time.LocalDateTime
import java.util.UUID

interface TelemetryRepository {

    fun saveBatch(entries: List<TelemetryEntry>): List<TelemetryEntry>

    fun findBySimulation(simulationId: UUID): List<TelemetryEntry>

    fun findBySimulationAndTimeRange(
        simulationId: UUID,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<TelemetryEntry>

    fun findBySimulationAndInverter(
        simulationId: UUID,
        inverterId: UUID
    ): List<TelemetryEntry>

    fun findLatestPerInverter(simulationId: UUID): List<TelemetryEntry>
}