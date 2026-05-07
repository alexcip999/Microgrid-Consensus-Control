package domain.usecase.simulation

import com.typesafe.config.ConfigException
import domain.model.simulation.Simulation
import domain.model.simulation.SimulationFidelity
import domain.model.simulation.SimulationStatus
import domain.repository.GridRepository
import domain.repository.SimulationRepository
import presentation.plugins.NotFoundException
import presentation.plugins.ValidationException
import java.time.LocalDateTime
import java.util.UUID

class CreateSimulationUseCase(
    private val simulationRepository: SimulationRepository,
    private val gridRepository: GridRepository
){
    data class Input(
        val gridId: UUID,
        val fidelity: SimulationFidelity = SimulationFidelity.LOW,
        val description: String? = null,
    )

    fun execute(input: Input): Simulation {
        gridRepository.findById(input.gridId)
            ?: throw NotFoundException("Grid with id ${input.gridId} does not exist")

        val active = simulationRepository.findActiveByGrid(input.gridId)

        if (active != null) {
            throw ValidationException("Grid already has an active simulation: ${active.id}")
        }

        return simulationRepository.save(
            Simulation(
                id          = UUID.randomUUID(),
                gridId      = input.gridId,
                status      = SimulationStatus.PENDING,
                fidelity    = input.fidelity,
                description = input.description,
                startedAt   = LocalDateTime.now(),
                endedAt     = null
            )
        )
    }
}