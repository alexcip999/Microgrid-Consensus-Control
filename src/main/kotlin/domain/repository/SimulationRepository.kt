package domain.repository

import domain.model.simulation.Simulation
import java.util.UUID

interface SimulationRepository {
    fun findById(id: UUID): Simulation?
    fun findAllByGrid(gridId: UUID): List<Simulation>
    fun findActiveByGrid(gridId: UUID): Simulation?
    fun save(simulation: Simulation): Simulation
    fun update(simulation: Simulation): Simulation
}