package domain.model.grid

import java.time.LocalDateTime
import java.util.UUID

data class Grid(
    val id: UUID,
    val name: String,
    val phase: GridPhase,
    val topology: GridTopology,
    val fNom: Double,
    val vNom: Double,
    val ownerId: UUID,
    val createdAt: LocalDateTime
)
