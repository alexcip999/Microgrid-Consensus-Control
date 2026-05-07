package domain.usecase.simulation

import domain.repository.SimulationRepository
import kotlinx.serialization.Serializable
import presentation.plugins.NotFoundException
import java.util.UUID

class GetSimulationStatusUseCase(
    private val simulationRepository: SimulationRepository
) {
    @Serializable
    data class Output(
        val id: String,
        val status: String,
        val fidelity: String,
        val startedAt: String,
        val endedAt: String?
    )

    fun execute(id: UUID): Output {
        val simulation = simulationRepository.findById(id)
            ?: throw NotFoundException("Simulation with id $id not found")

        return Output(
            id = simulation.id.toString(),
            status = simulation.status.name,
            fidelity = simulation.fidelity.name,
            startedAt = simulation.startedAt.toString(),
            endedAt = simulation.endedAt?.toString()
        )
    }
}