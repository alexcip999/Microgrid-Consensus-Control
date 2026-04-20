package infra.repository

import domain.model.grid.inverter.Inverter
import domain.repository.InverterRepository
import infra.db.entity.GridEntity
import infra.db.entity.InverterEntity
import infra.db.table.InvertersTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class InverterRepositoryImpl : InverterRepository {
    override fun findById(id: UUID): Inverter? = transaction {
        InverterEntity.findById(id)?.toDomain()
    }

    override fun findAllByGrid(gridId: UUID): List<Inverter> = transaction {
        InverterEntity
            .find { InvertersTable.gridId eq gridId }
            .map { it.toDomain() }
    }

    override fun save(inverter: Inverter): Inverter = transaction {
        InverterEntity.new(inverter.id) {
            grid = GridEntity.findById(inverter.gridId)
                ?: throw NoSuchElementException("Grid not found with id: ${inverter.gridId}")
            label = inverter.label
            index = inverter.index
            pMax = inverter.pMax
            p0Ref = inverter.p0Ref
            q0Ref = inverter.q0Ref
            kdroopP = inverter.kdroopP
            kdroopQ = inverter.kdroopQ
            rLine = inverter.rLine
            lLine = inverter.lLine
            epsilonP = inverter.epsilonP
            epsilonQ = inverter.epsilonQ
            isActive = inverter.isActive
        }.toDomain()
    }

    override fun update(inverter: Inverter): Inverter = transaction {
        val entity = InverterEntity.findById(inverter.id)
            ?: throw NoSuchElementException("Inverter not found with id: ${inverter.id}")

        entity.p0Ref = inverter.p0Ref
        entity.q0Ref = inverter.q0Ref
        entity.kdroopP = inverter.kdroopP
        entity.kdroopQ = inverter.kdroopQ
        entity.epsilonP = inverter.epsilonP
        entity.epsilonQ = inverter.epsilonQ
        entity.isActive = inverter.isActive

        entity.toDomain()
    }

    override fun delete(id: UUID): Boolean = transaction {
        val entity = InverterEntity.findById(id) ?: return@transaction false
        entity.delete()
        true
    }

}