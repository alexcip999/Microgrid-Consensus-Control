package domain.usecase.inverter

import domain.model.grid.inverter.Inverter
import domain.repository.GridRepository
import domain.repository.InverterRepository
import java.util.UUID

class CreateInverterUseCase(
    private val inverterRepository: InverterRepository,
    private val gridRepository: GridRepository,
) {
    data class Input(
        val gridId: UUID,
        val label: String,
        val index: Int,
        val pMax: Double    = 1.0,
        val p0Ref: Double   = 0.5,
        val q0Ref: Double   = 0.0,
        val kdroopP: Double = 0.02,
        val kdroopQ: Double = 0.05,
        val rLine: Double   = 0.05,
        val lLine: Double   = 0.0005,
        val epsilonP: Double = 0.20,
        val epsilonQ: Double = 0.05
    )

    fun execute(input: Input): Inverter {
        gridRepository.findById(input.gridId)
            ?: throw NoSuchElementException("Grid with id ${input.gridId} not found")

        require(input.label.isNotBlank()) { "Inverter label cannot be blank" }
        require(input.index >= 0) { "Inverter index must be >= 0" }
        require(input.pMax > 0) { "P_max must be greater than 0" }
        require(input.p0Ref in 0.0..input.pMax) { "P0_ref must be between 0 and P_max" }
        require(input.kdroopP > 0) { "Droop gain P must be greater than 0" }
        require(input.kdroopQ > 0) { "Droop gain Q must be greater than 0" }
        require(input.epsilonP in 0.0..0.5) { "Epsilon P must be between 0 and 0.5" }
        require(input.epsilonQ in 0.0..0.5) { "Epsilon Q must be between 0 and 0.5" }

        return inverterRepository.save(
            Inverter(
                id       = UUID.randomUUID(),
                gridId   = input.gridId,
                label    = input.label,
                index    = input.index,
                pMax     = input.pMax,
                p0Ref    = input.p0Ref,
                q0Ref    = input.q0Ref,
                kdroopP  = input.kdroopP,
                kdroopQ  = input.kdroopQ,
                rLine    = input.rLine,
                lLine    = input.lLine,
                epsilonP = input.epsilonP,
                epsilonQ = input.epsilonQ,
                isActive = true
            )
        )
    }
}