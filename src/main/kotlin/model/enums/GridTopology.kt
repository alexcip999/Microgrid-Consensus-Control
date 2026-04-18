package com.microgrid.model.enums

enum class GridTopology {
    RING,   // I1-I2-I3-I4-I1 (topologia din teza)
    MESH,   // fiecare cu toti vecinii
    STAR    // un nod central, ceilalti conectati la el
}