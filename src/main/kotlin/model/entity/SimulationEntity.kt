package com.microgrid.model.entity

import com.microgrid.model.tableobj.Simulations
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class SimulationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SimulationEntity>(Simulations)

    var grid        by GridEntity referencedOn Simulations.gridId
    var status      by Simulations.status
    var fidelity    by Simulations.fidelity
    var description by Simulations.description
    var startedAt   by Simulations.startedAt
    var endedAt     by Simulations.endedAt
}