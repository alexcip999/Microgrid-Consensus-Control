package com.microgrid.model.tableobj

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object TelemetryEntries : UUIDTable("telemetry") {
    val time           = datetime("time")              // timestamp masurare
    val inverterId     = reference("inverter_id", Inverters)
    val simulationId   = reference("simulation_id", Simulations)
    // Marimile electrice
    val p              = double("p")                   // putere activa [pu]
    val q              = double("q")                   // putere reactiva [pu]
    val vMag           = double("v_mag")               // magnitudine tensiune [pu]
    val freq           = double("freq")                // frecventa [Hz]
    // Date algoritm consens
    val deltaOmega     = double("delta_omega")         // corectia de frecventa din consens
    val pNorm          = double("p_norm")              // x_i = P_i / P_max
    val consensusError = double("consensus_error")     // |x_i - x_j| fata de vecin
}