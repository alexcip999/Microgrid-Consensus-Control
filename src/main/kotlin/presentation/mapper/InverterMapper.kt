package presentation.mapper

import domain.model.grid.inverter.Inverter
import presentation.dto.response.InverterResponse

fun Inverter.toResponse() = InverterResponse(
    id = id.toString(),
    gridId = gridId.toString(),
    label = label,
    index = index,
    pMax = pMax,
    p0Ref = p0Ref,
    q0Ref = q0Ref,
    kdroopP = kdroopP,
    kdroopQ = kdroopQ,
    rLine = rLine,
    lLine = lLine,
    epsilonP = epsilonP,
    epsilonQ = epsilonQ,
    isActive = isActive
)