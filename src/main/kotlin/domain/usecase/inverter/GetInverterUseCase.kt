package domain.usecase.inverter

import domain.model.grid.inverter.Inverter
import domain.repository.InverterRepository
import java.util.UUID

class GetInverterUseCase(
    private val inverterRepository: InverterRepository
) {
    fun execute(id: UUID): Inverter =
        inverterRepository.findById(id) ?: throw NoSuchElementException("Inverter with id $id not found")
}