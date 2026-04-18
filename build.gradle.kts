plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "ktor-simulation-server.com"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // ── Ktor (toate pluginurile printr-un singur bundle) ───
    implementation(libs.bundles.ktor.server)

    // ── Bază de date (Exposed + PostgreSQL + HikariCP) ────
    implementation(libs.bundles.exposed)
    implementation(libs.postgresql)
    implementation(libs.hikari)

    // ── Auth ──────────────────────────────────────────────
    implementation(libs.java.jwt)
    implementation(libs.jbcrypt)

    // ── Logging ───────────────────────────────────────────
    implementation(libs.logback.classic)

    // ── Test ──────────────────────────────────────────────
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}