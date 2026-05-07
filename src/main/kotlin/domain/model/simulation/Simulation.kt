package domain.model.simulation

import java.time.LocalDateTime
import java.util.UUID

data class Simulation(
    val id: UUID,
    val gridId: UUID,
    val status: SimulationStatus,
    val fidelity: SimulationFidelity,
    val description: String?,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime?
)