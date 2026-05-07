package domain.usecase.simulation

import domain.model.simulation.Simulation
import domain.repository.SimulationRepository
import java.util.UUID

class ListSimulationsUseCase(
    private val simulationRepository: SimulationRepository,
) {
    fun execute(gridId: UUID): List<Simulation> =
        simulationRepository.findAllByGrid(gridId)
}