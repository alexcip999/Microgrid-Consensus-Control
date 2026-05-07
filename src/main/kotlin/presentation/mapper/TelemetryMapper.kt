package presentation.mapper

import domain.model.telemetry.TelemetryEntry
import domain.usecase.telemetry.GetLatestSnapshotUseCase
import presentation.dto.response.InverterSnapshotResponse
import presentation.dto.response.SnapshotResponse
import presentation.dto.response.TelemetryEntryResponse

fun TelemetryEntry.toResponse() = TelemetryEntryResponse(
    id = id.toString(),
    simulationId = simulationId.toString(),
    inverterId = inverterId.toString(),
    timestamp = timestamp.toString(),
    p = p,
    q = q,
    vMag = vMag,
    freq = freq,
    deltaOmega = deltaOmega,
    consensusError = consensusError,
    pNorm = pNorm,
    pProduction = pProduction,
    pLoad = pLoad,
    pNet = pNet
)

fun GetLatestSnapshotUseCase.Output.toResponse() = SnapshotResponse(
    simulationId = simulationId.toString(),
    gridId = gridId.toString(),
    snapshots = snapshots.map { it.toResponse() }
)

fun GetLatestSnapshotUseCase.InverterSnapshot.toResponse() = InverterSnapshotResponse(
    inverterId = inverterId.toString(),
    label = label,
    p = p,
    q = q,
    vMag = vMag,
    freq = freq,
    deltaOmega = deltaOmega,
    consensusError = consensusError,
    pNorm = pNorm,
    pProduction = pProduction,
    pLoad = pLoad,
    pNet = pNet,
    timestamp = timestamp
)