package domain.usecase.simulation

import domain.model.simulation.Simulation
import domain.model.simulation.SimulationStatus
import domain.repository.SimulationRepository
import presentation.plugins.NotFoundException
import presentation.plugins.ValidationException
import java.time.LocalDateTime
import java.util.UUID

class StopSimulationUseCase(
    private val simulationRepository: SimulationRepository,
) {
    fun execute(id: UUID): Simulation {
        val simulation = simulationRepository.findById(id)
            ?: throw NotFoundException("Simulation with id $id not found")

        if (simulation.status != SimulationStatus.RUNNING) {
            throw ValidationException("Simulation with id $id is not running")
        }

        return simulationRepository.update(
            simulation.copy(
                status = SimulationStatus.COMPLETED,
                endedAt = LocalDateTime.now()
            )
        )
    }
}