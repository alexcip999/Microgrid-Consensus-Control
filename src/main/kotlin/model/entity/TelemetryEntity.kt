package com.microgrid.model.entity

import com.microgrid.model.tableobj.TelemetryEntries
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class TelemetryEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TelemetryEntity>(TelemetryEntries)

    var time           by TelemetryEntries.time
    var inverter       by InverterEntity referencedOn TelemetryEntries.inverterId
    var simulation     by SimulationEntity referencedOn TelemetryEntries.simulationId
    var p              by TelemetryEntries.p
    var q              by TelemetryEntries.q
    var vMag           by TelemetryEntries.vMag
    var freq           by TelemetryEntries.freq
    var deltaOmega     by TelemetryEntries.deltaOmega
    var pNorm          by TelemetryEntries.pNorm
    var consensusError by TelemetryEntries.consensusError
}