package infra.db.table

import domain.model.simulation.SimulationFidelity
import domain.model.simulation.SimulationStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object SimulationsTable : UUIDTable("simulations") {
    val gridId      = reference("grid_id", GridsTable)
    val status      = enumerationByName("status", 20, SimulationStatus::class)
    val fidelity    = enumerationByName("fidelity", 10, SimulationFidelity::class)
    val description = varchar("description", 500).nullable()
    val startedAt   = datetime("started_at")
    val endedAt     = datetime("ended_at").nullable()
}