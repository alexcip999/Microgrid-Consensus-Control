package domain.usecase.inverter

import domain.model.grid.inverter.Inverter
import domain.repository.InverterRepository
import java.util.UUID

class ListInvertersUseCase(
    private val inverterRepository: InverterRepository
) {
    fun execute(gridId: UUID): List<Inverter> =
        inverterRepository.findAllByGrid(gridId)
}