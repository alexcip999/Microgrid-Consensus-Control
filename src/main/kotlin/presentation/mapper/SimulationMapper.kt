package presentation.mapper

import domain.model.simulation.Simulation
import presentation.dto.response.SimulationResponse
import presentation.dto.response.SimulationStatusResponse

fun Simulation.toResponse() = SimulationResponse(
    id = id.toString(),
    gridId = gridId.toString(),
    status = status.name,
    fidelity = fidelity.name,
    description = description,
    startedAt = startedAt.toString(),
    endedAt = endedAt?.toString()
)

fun Simulation.toStatusResponse() = SimulationStatusResponse(
    id = id.toString(),
    status = status.name,
    fidelity = fidelity.name,
    startedAt = startedAt.toString(),
    endedAt = endedAt?.toString()
)