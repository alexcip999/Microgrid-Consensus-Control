package domain.usecase.grid

import domain.repository.GridRepository
import java.util.UUID

class DeleteGridUseCase(
    private val gridRepository: GridRepository,
) {
    fun execute(id: UUID) {
        val deleted = gridRepository.delete(id)
        if (!deleted) throw NoSuchElementException("Grid not found with id: $id")
    }
}