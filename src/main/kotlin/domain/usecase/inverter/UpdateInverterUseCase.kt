package domain.usecase.inverter

import domain.model.grid.inverter.Inverter
import domain.repository.InverterRepository
import java.util.UUID

class UpdateInverterUseCase(
    private val inverterRepository: InverterRepository
) {
    data class Input(
        val id: UUID,
        val p0Ref: Double?    = null,
        val q0Ref: Double?    = null,
        val kdroopP: Double?  = null,
        val kdroopQ: Double?  = null,
        val epsilonP: Double? = null,
        val epsilonQ: Double? = null,
        val isActive: Boolean? = null
    )

    fun execute(input: Input): Inverter {
        val existing = inverterRepository.findById(input.id)
            ?: throw NoSuchElementException("Inverter not found with id: ${input.id}")

        input.kdroopP?.let { require(it > 0) { "Droop gain P must be greater than 0" } }
        input.kdroopQ?.let { require(it > 0) { "Droop gain Q must be greater than 0" } }
        input.epsilonP?.let { require(it in 0.0..0.5) { "Epsilon P must be between 0 and 0.5" } }
        input.epsilonQ?.let { require(it in 0.0..0.5) { "Epsilon Q must be between 0 and 0.5" } }

        val updated = existing.copy(
            p0Ref    = input.p0Ref    ?: existing.p0Ref,
            q0Ref    = input.q0Ref    ?: existing.q0Ref,
            kdroopP  = input.kdroopP  ?: existing.kdroopP,
            kdroopQ  = input.kdroopQ  ?: existing.kdroopQ,
            epsilonP = input.epsilonP ?: existing.epsilonP,
            epsilonQ = input.epsilonQ ?: existing.epsilonQ,
            isActive = input.isActive ?: existing.isActive
        )

        return inverterRepository.update(updated)
    }
}