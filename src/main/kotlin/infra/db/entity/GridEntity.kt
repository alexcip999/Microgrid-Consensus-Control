package infra.db.entity

import domain.model.grid.Grid
import infra.db.table.GridsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class GridEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<GridEntity>(GridsTable)

    var name      by GridsTable.name
    var phase     by GridsTable.phase
    var topology  by GridsTable.topology
    var fNom      by GridsTable.fNom
    var vNom      by GridsTable.vNom
    var owner     by UserEntity referencedOn GridsTable.ownerId
    var createdAt by GridsTable.createdAt

    fun toDomain() = Grid(
        id = id.value,
        name = name,
        phase = phase,
        topology = topology,
        fNom = fNom,
        vNom = vNom,
        ownerId = owner.id.value,
        createdAt = createdAt
    )
}