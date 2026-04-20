package domain.repository

import domain.model.grid.Grid
import java.util.UUID

interface GridRepository {
    fun findById(id: UUID): Grid?
    fun findAllByOwner(ownerId: UUID): List<Grid>
    fun save(grid: Grid): Grid
    fun delete(id: UUID): Boolean
}