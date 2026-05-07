package domain.usecase.simulation

import domain.model.simulation.Simulation
import domain.repository.SimulationRepository
import presentation.plugins.NotFoundException
import java.util.UUID

class GetSimulationUseCase(
    private val simulationRepository: SimulationRepository,
) {
    fun execute(id: UUID): Simulation =
        simulationRepository.findById(id)
            ?: throw NotFoundException("Simulation with id $id not found")
}