package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateInverterRequest(
    val label: String,
    val index: Int,
    val pMax: Double     = 1.0,
    val p0Ref: Double    = 0.5,
    val q0Ref: Double    = 0.0,
    val kdroopP: Double  = 0.02,
    val kdroopQ: Double  = 0.05,
    val rLine: Double    = 0.05,
    val lLine: Double    = 0.0005,
    val epsilonP: Double = 0.20,
    val epsilonQ: Double = 0.05
)