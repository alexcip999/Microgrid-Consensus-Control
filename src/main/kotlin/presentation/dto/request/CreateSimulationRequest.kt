package presentation.dto.request

import domain.model.simulation.SimulationFidelity
import kotlinx.serialization.Serializable

@Serializable
data class CreateSimulationRequest(
    val gridId: String,
    val fidelity: SimulationFidelity = SimulationFidelity.LOW,
    val description: String? = null
)
