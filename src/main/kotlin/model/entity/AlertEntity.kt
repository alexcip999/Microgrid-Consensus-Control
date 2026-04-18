package com.microgrid.model.entity

import com.microgrid.model.tableobj.Alerts
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class AlertEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AlertEntity>(Alerts)

    var inverter    by InverterEntity referencedOn Alerts.inverterId
    var simulation  by SimulationEntity referencedOn Alerts.simulationId
    var type        by Alerts.type
    var value       by Alerts.value
    var threshold   by Alerts.threshold
    var triggeredAt by Alerts.triggeredAt
}