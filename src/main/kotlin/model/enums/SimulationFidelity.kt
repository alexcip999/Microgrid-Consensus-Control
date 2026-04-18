package com.microgrid.model.enums

enum class SimulationFidelity {
    LOW,   // model mediat, ~35s rulare — pentru tuning algoritm
    HIGH   // model complet cu PWM, ~394s — pentru rezultate finale teza
}