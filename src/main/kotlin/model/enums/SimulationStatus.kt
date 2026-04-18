package com.microgrid.model.enums

enum class SimulationStatus {
    PENDING,    // creat, nu a pornit inca
    RUNNING,    // simularea Simulink e activa
    COMPLETED,  // s-a terminat normal
    FAILED      // eroare in Simulink sau backend
}