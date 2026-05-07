package infra.db.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object TelemetryTable : UUIDTable("telemetry") {
    val simulationId   = reference("simulation_id", SimulationsTable)
    val inverterId     = reference("inverter_id", InvertersTable)
    val timestamp      = datetime("timestamp")

    val p              = double("p")
    val q              = double("q")
    val vMag           = double("v_mag")
    val freq           = double("freq")

    val deltaOmega     = double("delta_omega")
    val consensusError = double("consensus_error")
    val pNorm          = double("p_norm")

    val pProduction    = double("p_production")
    val pLoad          = double("p_load")
    val pNet           = double("p_net")
}