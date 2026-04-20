package domain.model.grid.inverter

import java.util.UUID

data class Inverter(
    val id: UUID,
    val gridId: UUID,
    val label: String,
    val index: Int,
    val pMax: Double,
    val p0Ref: Double,
    val q0Ref: Double,
    val kdroopP: Double,
    val kdroopQ: Double,
    val rLine: Double,
    val lLine: Double,
    val epsilonP: Double,
    val epsilonQ: Double,
    val isActive: Boolean
)
