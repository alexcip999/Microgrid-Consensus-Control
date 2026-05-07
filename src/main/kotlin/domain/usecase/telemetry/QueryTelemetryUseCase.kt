package domain.usecase.telemetry

import domain.model.telemetry.TelemetryEntry
import domain.repository.SimulationRepository
import domain.repository.TelemetryRepository
import presentation.plugins.NotFoundException
import java.time.LocalDateTime
import java.util.UUID

class QueryTelemetryUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val simulationRepository: SimulationRepository
) {
    data class Input(
        val simulationId: UUID,
        val inverterId: UUID? = null,
        val from: LocalDateTime? = null,
        val to: LocalDateTime? = null
    )

    fun execute(input: Input): List<TelemetryEntry> {
        simulationRepository.findById(input.simulationId)
            ?: throw NotFoundException("Simulation not found: ${input.simulationId}")

        return when {
            input.inverterId != null && input.from != null && input.to != null ->
                telemetryRepository.findBySimulationAndInverter(
                    input.simulationId,
                    input.inverterId
                ).filter { it.timestamp >= input.from && it.timestamp <= input.to }

            input.inverterId != null ->
                telemetryRepository.findBySimulationAndInverter(
                    input.simulationId,
                    input.inverterId
                )

            input.from != null && input.to != null ->
                telemetryRepository.findBySimulationAndTimeRange(
                    input.simulationId,
                    input.from,
                    input.to
                )

            else ->
                telemetryRepository.findBySimulation(input.simulationId)
        }
    }
}