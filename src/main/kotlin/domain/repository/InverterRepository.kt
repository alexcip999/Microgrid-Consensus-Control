package domain.repository

import domain.model.grid.inverter.Inverter
import java.util.UUID

interface InverterRepository {
    fun findById(id: UUID): Inverter?
    fun findAllByGrid(gridId: UUID): List<Inverter>
    fun save(inverter: Inverter): Inverter
    fun update(inverter: Inverter): Inverter
    fun delete(id: UUID): Boolean
}