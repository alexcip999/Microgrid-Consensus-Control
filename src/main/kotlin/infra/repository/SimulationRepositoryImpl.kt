package infra.repository

import domain.model.simulation.Simulation
import domain.model.simulation.SimulationStatus
import domain.repository.SimulationRepository
import infra.db.entity.GridEntity
import infra.db.entity.SimulationEntity
import infra.db.table.SimulationsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SimulationRepositoryImpl : SimulationRepository {
    override fun findById(id: UUID): Simulation? = transaction {
        SimulationEntity.findById(id)?.toDomain()
    }

    override fun findAllByGrid(gridId: UUID): List<Simulation> = transaction {
        SimulationEntity.find { SimulationsTable.gridId eq gridId }
            .orderBy(SimulationsTable.startedAt to SortOrder.DESC).map { it.toDomain() }
    }

    override fun findActiveByGrid(gridId: UUID): Simulation? = transaction {
        SimulationEntity.find {
                (SimulationsTable.gridId eq gridId) and (SimulationsTable.status inList listOf(
                    SimulationStatus.PENDING, SimulationStatus.RUNNING
                ))
            }.firstOrNull()?.toDomain()
    }

    override fun save(simulation: Simulation): Simulation = transaction {
        SimulationEntity.new(simulation.id) {
            grid = GridEntity.findById(simulation.gridId)
                ?: throw NoSuchElementException("Grid not found: ${simulation.gridId}")
            status = simulation.status
            fidelity = simulation.fidelity
            description = simulation.description
            startedAt = simulation.startedAt
            endedAt = simulation.endedAt
        }.toDomain()
    }

    override fun update(simulation: Simulation): Simulation = transaction {
        val entity = SimulationEntity.findById(simulation.id)
            ?: throw NoSuchElementException("Simulation not found: ${simulation.id}")

        entity.status = simulation.status
        entity.endedAt = simulation.endedAt

        entity.toDomain()
    }

}