package com.microgrid.model.entity

import com.microgrid.model.tableobj.Grids
import com.microgrid.model.tableobj.Inverters
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class GridEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<GridEntity>(Grids)

    var name      by Grids.name
    var phase     by Grids.phase
    var topology  by Grids.topology
    var fNom      by Grids.fNom
    var vNom      by Grids.vNom
    var owner     by UserEntity referencedOn Grids.ownerId
    var createdAt by Grids.createdAt

    val inverters by InverterEntity referrersOn Inverters.gridId
}