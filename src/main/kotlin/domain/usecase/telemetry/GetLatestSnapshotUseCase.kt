package domain.usecase.telemetry

import domain.repository.SimulationRepository
import domain.repository.TelemetryRepository
import presentation.plugins.NotFoundException
import java.util.UUID

class GetLatestSnapshotUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val simulationRepository: SimulationRepository
) {
    data class InverterSnapshot(
        val inverterId: UUID,
        val label: String,

        val p: Double,
        val q: Double,
        val vMag: Double,
        val freq: Double,

        val deltaOmega: Double,
        val consensusError: Double,
        val pNorm: Double,

        val pProduction: Double,
        val pLoad: Double,
        val pNet: Double,
        val timestamp: String
    )

    data class Output(
        val simulationId: UUID,
        val gridId: UUID,
        val snapshots: List<InverterSnapshot>
    )

    fun execute(simulationId: UUID): Output {
        val simulation = simulationRepository.findById(simulationId)
            ?: throw NotFoundException("Simulation not found: $simulationId")

        val latest = telemetryRepository.findLatestPerInverter(simulationId)

        val snapshots = latest.map { entry ->
            InverterSnapshot(
                inverterId     = entry.inverterId,
                label          = entry.inverterId.toString(),
                p              = entry.p,
                q              = entry.q,
                vMag           = entry.vMag,
                freq           = entry.freq,
                deltaOmega     = entry.deltaOmega,
                consensusError = entry.consensusError,
                pNorm          = entry.pNorm,
                pProduction    = entry.pProduction,
                pLoad          = entry.pLoad,
                pNet           = entry.pNet,
                timestamp      = entry.timestamp.toString()
            )
        }

        return Output(
            simulationId = simulationId,
            gridId       = simulation.gridId,
            snapshots    = snapshots
        )
    }
}