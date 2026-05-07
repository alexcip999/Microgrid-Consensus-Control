package domain.usecase.telemetry

import domain.model.simulation.SimulationStatus
import domain.model.telemetry.TelemetryEntry
import domain.repository.InverterRepository
import domain.repository.SimulationRepository
import domain.repository.TelemetryRepository
import presentation.plugins.NotFoundException
import presentation.plugins.ValidationException
import java.time.LocalDateTime
import java.util.UUID

class IngestTelemetryUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val simulationRepository: SimulationRepository,
    private val inverterRepository: InverterRepository
) {
    data class TelemetryPoint(
        val inverterId: UUID,
        val timestamp: LocalDateTime,

        val p: Double,
        val q: Double,
        val vMag: Double,
        val freq: Double,

        val deltaOmega: Double,
        val consensusError: Double,
        val pNorm: Double,

        val pProduction: Double,
        val pLoad: Double,
        val pNet: Double
    )

    data class Input(
        val simulationId: UUID,
        val points: List<TelemetryPoint>
    )

    fun execute(input: Input): List<TelemetryEntry> {
        require(input.points.isNotEmpty()) { "Telemetry batch cannot be empty" }
        require(input.points.size <= 1000) { "Batch size cannot exceed 1000 points" }

        val simulation = simulationRepository.findById(input.simulationId)
            ?: throw NotFoundException("Simulation not found: ${input.simulationId}")

        if (simulation.status != SimulationStatus.RUNNING) {
            throw ValidationException(
                "Cannot ingest telemetry — simulation status: ${simulation.status}"
            )
        }

        val gridInverters = inverterRepository
            .findAllByGrid(simulation.gridId)
            .map { it.id }
            .toSet()

        val unknownInverters = input.points
            .map { it.inverterId }
            .toSet()
            .subtract(gridInverters)

        if (unknownInverters.isNotEmpty()) {
            throw ValidationException(
                "Unknown inverter ids: $unknownInverters"
            )
        }

        val entries = input.points.map { point ->
            TelemetryEntry(
                id             = UUID.randomUUID(),
                simulationId   = input.simulationId,
                inverterId     = point.inverterId,
                timestamp      = point.timestamp,
                p              = point.p,
                q              = point.q,
                vMag           = point.vMag,
                freq           = point.freq,
                deltaOmega     = point.deltaOmega,
                consensusError = point.consensusError,
                pNorm          = point.pNorm,
                pProduction    = point.pProduction,
                pLoad          = point.pLoad,
                pNet           = point.pNet
            )
        }

        return telemetryRepository.saveBatch(entries)
    }
}