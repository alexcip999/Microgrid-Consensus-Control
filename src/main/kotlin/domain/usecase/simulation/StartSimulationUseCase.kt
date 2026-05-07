package domain.usecase.simulation

import domain.model.simulation.Simulation
import domain.model.simulation.SimulationStatus
import domain.repository.SimulationRepository
import presentation.plugins.NotFoundException
import presentation.plugins.ValidationException
import java.util.UUID

class StartSimulationUseCase(
    private val simulationRepository: SimulationRepository,
) {
    fun execute(id: UUID): Simulation {
        val simulation = simulationRepository.findById(id)
            ?: throw NotFoundException("Simulation with id $id not found")

        if (simulation.status != SimulationStatus.PENDING) {
            throw ValidationException("Simulation with id $id does not match status ${simulation.status}")
        }

        return simulationRepository.update(
            simulation.copy(status = SimulationStatus.RUNNING)
        )
    }
}