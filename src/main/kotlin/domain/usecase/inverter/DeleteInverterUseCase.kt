package domain.usecase.inverter

import domain.repository.InverterRepository
import java.util.UUID

class DeleteInverterUseCase(
    private val inverterRepository: InverterRepository
) {
    fun execute(id: UUID) {
        val deleted = inverterRepository.delete(id)
        if (!deleted) throw NoSuchElementException("Inverter not found with id: $id")
    }
}