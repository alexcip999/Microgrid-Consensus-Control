package com.microgrid.model.enums

enum class AlertType {
    FREQ_LOW,      // frecventa < 59.5 Hz
    FREQ_HIGH,     // frecventa > 60.5 Hz
    VOLT_LOW,      // tensiune < 0.95 pu
    VOLT_HIGH,     // tensiune > 1.05 pu
    CONSENSUS_DIVERGED,  // |x_i - x_j| > 0.1 dupa convergenta
    INVERTER_OFFLINE
}