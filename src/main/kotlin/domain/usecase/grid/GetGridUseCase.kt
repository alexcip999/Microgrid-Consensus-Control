package domain.usecase.grid

import domain.model.grid.Grid
import domain.repository.GridRepository
import presentation.plugins.NotFoundException
import java.util.UUID

class GetGridUseCase(
    private val gridRepository: GridRepository,
) {
    fun execute(id: UUID): Grid =
        gridRepository.findById(id)
            ?: throw NotFoundException("Grid with id $id not found")
}