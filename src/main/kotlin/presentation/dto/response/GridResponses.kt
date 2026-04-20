package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GridResponse(
    val id: String,
    val name: String,
    val phase: String,
    val topology: String,
    val fNom: Double,
    val vNom: Double,
    val ownerId: String,
    val createdAt: String,
    val inverters: List<InverterResponse> = emptyList()
)