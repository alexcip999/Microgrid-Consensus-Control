package domain.usecase.grid

import domain.model.grid.Grid
import domain.model.grid.GridPhase
import domain.model.grid.GridTopology
import domain.repository.GridRepository
import java.time.LocalDateTime
import java.util.UUID

class CreateGridUseCase(
    private val gridRepository: GridRepository
) {
    data class Input(
        val name: String,
        val phase: GridPhase,
        val topology: GridTopology,
        val fNom: Double,
        val vNom: Double,
        val ownerId: UUID
    )

    fun execute(input: Input): Grid {
        require(input.name.isNotBlank()) { "Grid name cannot be blank" }
        require(input.fNom in 50.0..60.0) { "Nominal frequency must be between 50 and 60 Hz" }
        require(input.vNom in 0.9..1.1) { "Nominal voltage must be between 0.9 and 1.1 pu" }

        return gridRepository.save(
            Grid(
                id = UUID.randomUUID(),
                name = input.name,
                phase = input.phase,
                topology = input.topology,
                fNom = input.fNom,
                vNom = input.vNom,
                ownerId = input.ownerId,
                createdAt = LocalDateTime.now()
            )
        )
    }
}