package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotResponse(
    val simulationId: String,
    val gridId: String,
    val snapshots: List<InverterSnapshotResponse>
)
