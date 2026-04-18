package com.microgrid.model.tableobj

import com.microgrid.model.enums.SimulationFidelity
import com.microgrid.model.enums.SimulationStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object Simulations : UUIDTable("simulations") {
    val gridId      = reference("grid_id", Grids)
    val status      = enumerationByName("status", 20, SimulationStatus::class)
    val fidelity    = enumerationByName("fidelity", 10, SimulationFidelity::class)
    val description = varchar("description", 500).nullable()
    val startedAt   = datetime("started_at")
    val endedAt     = datetime("ended_at").nullable()
}