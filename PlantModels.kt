package com.example.plantmonitor.model

data class DeviceItem(
    val deviceId: String = "",
    val deviceName: String = "ESP32 Sensor Node",
    val ownerUid: String? = null
)

data class Telemetry(
    val soilMoisture: Int = 0,
    val airTemperature: Double = 0.0,
    val airHumidity: Double = 0.0,
    val lastUpdated: Long = 0L
)

data class Thresholds(
    val minSoilMoisture: Int = 40,
    val maxAirTemperature: Double = 35.0,
    val minAirHumidity: Double = 40.0
)

sealed class ConditionAlert {
    data class WaterNeeded(val current: Int, val required: Int) : ConditionAlert()
    data class HighTemperature(val current: Double, val limit: Double) : ConditionAlert()
    data class LowHumidity(val current: Double, val required: Double) : ConditionAlert()
    object Optimal : ConditionAlert()
}