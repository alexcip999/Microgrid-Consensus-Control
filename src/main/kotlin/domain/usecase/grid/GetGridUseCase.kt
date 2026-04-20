package domain.usecase.grid

import domain.model.grid.Grid
import domain.repository.GridRepository
import java.util.UUID

class GetGridUseCase(
    private val gridRepository: GridRepository,
) {
    fun execute(id: UUID): Grid =
        gridRepository.findById(id)
            ?: throw NoSuchElementException("Grid with id $id not found")
}