package com.microgrid.model.tableobj

import org.jetbrains.exposed.dao.id.UUIDTable

object Inverters : UUIDTable("inverters") {
    val gridId    = reference("grid_id", Grids)
    val label     = varchar("label", 50)               // "Inv1", "Inv2"...
    val index     = integer("index")                   // 0..3, pozitia in graf
    // Parametri droop
    val pMax      = double("p_max").default(1.0)       // pu
    val p0Ref     = double("p0_ref").default(0.5)      // pu, setpoint putere activa
    val q0Ref     = double("q0_ref").default(0.0)      // pu, setpoint putere reactiva
    val kdroopP   = double("kdroop_p").default(0.02)   // gain droop P-f
    val kdroopQ   = double("kdroop_q").default(0.05)   // gain droop Q-V
    // Impedanta de linie R-L
    val rLine     = double("r_line").default(0.05)     // Ohm
    val lLine     = double("l_line").default(0.0005)   // H
    // Parametri consens
    val epsilonP  = double("epsilon_p").default(0.20)  // step consens P
    val epsilonQ  = double("epsilon_q").default(0.05)  // step consens Q
    val isActive  = bool("is_active").default(true)
}