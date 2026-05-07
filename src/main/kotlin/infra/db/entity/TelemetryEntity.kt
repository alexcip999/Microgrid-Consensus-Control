package infra.db.entity

import domain.model.telemetry.TelemetryEntry
import infra.db.table.TelemetryTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class TelemetryEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TelemetryEntity>(TelemetryTable)

    var simulation by SimulationEntity referencedOn TelemetryTable.simulationId
    var inverter by InverterEntity referencedOn TelemetryTable.inverterId
    var timestamp by TelemetryTable.timestamp

    // ── Mărimi electrice ──────────────────────────────────────────────────
    var p by TelemetryTable.p
    var q by TelemetryTable.q
    var vMag by TelemetryTable.vMag
    var freq by TelemetryTable.freq

    // ── Algoritm consens ──────────────────────────────────────────────────
    var deltaOmega by TelemetryTable.deltaOmega
    var consensusError by TelemetryTable.consensusError
    var pNorm by TelemetryTable.pNorm

    // ── Producție și consum ───────────────────────────────────────────────
    var pProduction by TelemetryTable.pProduction
    var pLoad by TelemetryTable.pLoad
    var pNet by TelemetryTable.pNet

    fun toDomain() = TelemetryEntry(
        id = id.value,
        simulationId = simulation.id.value,
        inverterId = inverter.id.value,
        timestamp = timestamp,
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
}