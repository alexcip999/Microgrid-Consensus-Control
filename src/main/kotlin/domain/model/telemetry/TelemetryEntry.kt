package domain.model.telemetry

import java.time.LocalDateTime
import java.util.UUID

data class TelemetryEntry(
    val id: UUID,
    val simulationId: UUID,
    val inverterId: UUID,
    val timestamp: LocalDateTime,

    // ── Mărimi electrice ──────────────────────────────────────────────────
    val p: Double,              // putere activa masurata [pu]
    val q: Double,              // putere reactiva masurata [pu]
    val vMag: Double,           // magnitudine tensiune [pu]
    val freq: Double,           // frecventa [Hz]

    // ── Algoritm consens ──────────────────────────────────────────────────
    val deltaOmega: Double,     // corecția de frecvență din consens δω [rad/s]
    val consensusError: Double, // |x_i - x_j| față de vecinul principal [pu]
    val pNorm: Double,          // x_i = P_i / P_max — valoarea normalizată din algoritm [pu]

    // ── Producție și consum ───────────────────────────────────────────────
    val pProduction: Double,    // putere produsa de sursa PV [pu]
    val pLoad: Double,          // putere consumata de sarcina locala [pu]
    val pNet: Double            // putere neta = productie - consum [pu]
)
