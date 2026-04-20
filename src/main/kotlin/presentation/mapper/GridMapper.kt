package presentation.mapper

import domain.model.grid.Grid
import presentation.dto.response.GridResponse
import presentation.dto.response.InverterResponse

fun Grid.toResponse(
    inverters: List<InverterResponse> = emptyList()
) = GridResponse(
    id = id.toString(),
    name = name,
    phase = phase.name,
    topology = topology.name,
    fNom = fNom,
    vNom = vNom,
    ownerId = ownerId.toString(),
    createdAt = createdAt.toString(),
    inverters = inverters
)