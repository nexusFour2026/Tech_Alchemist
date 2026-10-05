private fun evaluateConditions() {
    val current = _telemetry.value
    val limits = _thresholds.value
    val activeAlerts = mutableListOf<ConditionAlert>()

    // Soil Moisture Check
    if (current.soilMoisture < limits.minSoilMoisture) {
        activeAlerts.add(ConditionAlert.WaterNeeded(current.soilMoisture, limits.minSoilMoisture))
        notifier.triggerAlarm("Watering Required!", "Soil Moisture is ${current.soilMoisture}% (Min: ${limits.minSoilMoisture}%).", 101)
    }

    // Air Temperature Check
    if (current.airTemperature > limits.maxAirTemperature) {
        activeAlerts.add(ConditionAlert.HighTemperature(current.airTemperature, limits.maxAirTemperature))
        notifier.triggerAlarm("Too Hot!", "Temperature is ${current.airTemperature}°C (Max: ${limits.maxAirTemperature}°C). Move to shade.", 102)
    }

    // Air Humidity Check
    if (current.airHumidity < limits.minAirHumidity) {
        activeAlerts.add(ConditionAlert.LowHumidity(current.airHumidity, limits.minAirHumidity))
        notifier.triggerAlarm("Air Too Dry!", "Humidity is ${current.airHumidity}% (Min: ${limits.minAirHumidity}%). Mist the plant.", 103)
    }

    if (activeAlerts.isEmpty()) activeAlerts.add(ConditionAlert.Optimal)
    _alerts.value = activeAlerts
}