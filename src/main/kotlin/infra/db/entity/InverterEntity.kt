package infra.db.entity

import domain.model.grid.inverter.Inverter
import infra.db.table.InvertersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class InverterEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<InverterEntity>(InvertersTable)

    var grid     by GridEntity referencedOn InvertersTable.gridId
    var label    by InvertersTable.label
    var index    by InvertersTable.index
    var pMax     by InvertersTable.pMax
    var p0Ref    by InvertersTable.p0Ref
    var q0Ref    by InvertersTable.q0Ref
    var kdroopP  by InvertersTable.kdroopP
    var kdroopQ  by InvertersTable.kdroopQ
    var rLine    by InvertersTable.rLine
    var lLine    by InvertersTable.lLine
    var epsilonP by InvertersTable.epsilonP
    var epsilonQ by InvertersTable.epsilonQ
    var isActive by InvertersTable.isActive

    fun toDomain() = Inverter(
        id = id.value,
        gridId = grid.id.value,
        label = label,
        index = index,
        pMax = pMax,
        p0Ref = p0Ref,
        q0Ref = q0Ref,
        kdroopP = kdroopP,
        kdroopQ = kdroopQ,
        rLine = rLine,
        lLine = lLine,
        epsilonP = epsilonP,
        epsilonQ = epsilonQ,
        isActive = isActive
    )
}