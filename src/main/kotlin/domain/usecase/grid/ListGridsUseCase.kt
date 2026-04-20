package domain.usecase.grid

import domain.model.grid.Grid
import domain.repository.GridRepository
import java.util.UUID

class ListGridsUseCase(
    private val gridRepository: GridRepository,
) {
    fun execute(ownerId: UUID): List<Grid> =
        gridRepository.findAllByOwner(ownerId)
}