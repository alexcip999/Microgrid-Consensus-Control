package com.microgrid.model.entity

import com.microgrid.model.tableobj.Inverters
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class InverterEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<InverterEntity>(Inverters)

    var grid      by GridEntity referencedOn Inverters.gridId
    var label     by Inverters.label
    var index     by Inverters.index
    var pMax      by Inverters.pMax
    var p0Ref     by Inverters.p0Ref
    var q0Ref     by Inverters.q0Ref
    var kdroopP   by Inverters.kdroopP
    var kdroopQ   by Inverters.kdroopQ
    var rLine     by Inverters.rLine
    var lLine     by Inverters.lLine
    var epsilonP  by Inverters.epsilonP
    var epsilonQ  by Inverters.epsilonQ
    var isActive  by Inverters.isActive
}