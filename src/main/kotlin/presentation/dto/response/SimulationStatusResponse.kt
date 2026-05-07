package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SimulationStatusResponse(
    val id: String,
    val status: String,
    val fidelity: String,
    val startedAt: String,
    val endedAt: String?
)