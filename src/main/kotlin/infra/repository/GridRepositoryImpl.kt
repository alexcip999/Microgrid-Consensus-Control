package infra.repository

import domain.model.grid.Grid
import domain.repository.GridRepository
import infra.db.entity.GridEntity
import infra.db.entity.InverterEntity
import infra.db.entity.UserEntity
import infra.db.table.GridsTable
import infra.db.table.InvertersTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class GridRepositoryImpl : GridRepository {
    override fun findById(id: UUID): Grid? = transaction {
        GridEntity.findById(id)?.toDomain()
    }

    override fun findAllByOwner(ownerId: UUID): List<Grid> = transaction {
        GridEntity.find { GridsTable.ownerId eq ownerId }.toList().map { it.toDomain() }
    }

    override fun save(grid: Grid): Grid = transaction {
        GridEntity.new(grid.id) {
            name = grid.name
            phase = grid.phase
            topology = grid.topology
            fNom = grid.fNom
            vNom = grid.vNom
            owner = UserEntity.findById(grid.ownerId)
                ?: throw NoSuchElementException("User not found with id: ${grid.ownerId}")
            createdAt = grid.createdAt
        }.toDomain()
    }


    override fun delete(id: UUID): Boolean = transaction {
        val entity = GridEntity.findById(id) ?: return@transaction false

        InverterEntity
            .find { InvertersTable.gridId eq id }
            .forEach { it.delete() }


        entity.delete()
        true
    }
}