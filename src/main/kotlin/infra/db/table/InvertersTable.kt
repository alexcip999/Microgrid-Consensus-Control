package infra.db.table

import org.jetbrains.exposed.dao.id.UUIDTable

object InvertersTable : UUIDTable("inverters") {
    val gridId = reference("grid_id", GridsTable)
    val label = varchar("label", 50)
    val index = integer("index")

    val pMax = double("p_max").default(1.0)
    val p0Ref = double("p0_ref").default(0.5)
    val q0Ref = double("q0_ref").default(0.0)
    val kdroopP = double("kdroop_p").default(0.02)
    val kdroopQ = double("kdroop_q").default(0.05)

    val rLine = double("r_line").default(0.05)
    val lLine = double("l_line").default(0.0005)

    val epsilonP = double("epsilon_p").default(0.20)
    val epsilonQ = double("epsilon_q").default(0.05)
    val isActive = bool("is_active").default(true)
}