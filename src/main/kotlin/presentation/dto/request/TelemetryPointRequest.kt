package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryPointRequest(
    val inverterId: String,
    val timestamp: String,
    val p: Double,
    val q: Double,
    val vMag: Double,
    val freq: Double,

    val deltaOmega: Double,
    val consensusError: Double,
    val pNorm: Double,

    val pProduction: Double,
    val pLoad: Double,
    val pNet: Double
)
