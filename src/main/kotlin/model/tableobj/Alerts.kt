package com.microgrid.model.tableobj

import com.microgrid.model.enums.AlertType
import com.microgrid.model.tableobj.Inverters
import com.microgrid.model.tableobj.Simulations
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object Alerts : UUIDTable("alerts") {
    val inverterId   = reference("inverter_id", Inverters)
    val simulationId = reference("simulation_id", Simulations)
    val type         = enumerationByName("type", 30, AlertType::class)
    val value        = double("value")                 // valoarea care a depasit pragul
    val threshold    = double("threshold")             // pragul configurat
    val triggeredAt  = datetime("triggered_at")
}