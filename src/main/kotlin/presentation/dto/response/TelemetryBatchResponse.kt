package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryBatchResponse(
    val simulationId: String,
    val count: Int,
    val entries: List<TelemetryEntryResponse>
)
