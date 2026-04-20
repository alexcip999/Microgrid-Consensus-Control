package presentation.dto.request

import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import kotlinx.serialization.Serializable

@Serializable
data class CreateGridRequest(
    val name: String,
    val phase: GridPhase,
    val topology: GridTopology = GridTopology.RING,
    val fNom: Double = 60.0,
    val vNom: Double = 1.0
)
