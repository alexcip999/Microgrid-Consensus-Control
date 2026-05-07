package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryBatchRequest(
    val simulationId: String,
    val points: List<TelemetryPointRequest>
)
