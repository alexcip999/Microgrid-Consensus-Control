package infra.db.entity

import domain.model.simulation.Simulation
import infra.db.table.SimulationsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class SimulationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SimulationEntity>(SimulationsTable)

    var grid        by GridEntity referencedOn SimulationsTable.gridId
    var status      by SimulationsTable.status
    var fidelity    by SimulationsTable.fidelity
    var description by SimulationsTable.description
    var startedAt   by SimulationsTable.startedAt
    var endedAt     by SimulationsTable.endedAt

    fun toDomain() = Simulation(
        id = id.value,
        gridId = grid.id.value,
        status = status,
        fidelity = fidelity,
        description = description,
        startedAt = startedAt,
        endedAt = endedAt
    )
}