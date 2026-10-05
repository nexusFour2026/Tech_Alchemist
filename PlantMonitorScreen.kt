// ==========================================
// ALERT STATUS BANNER
// ==========================================
@Composable
fun AlertStatusCard(alerts: List<ConditionAlert>) {
    val isOptimal = alerts.any { it is ConditionAlert.Optimal } || alerts.isEmpty()
    val containerColor by animateColorAsState(if (isOptimal) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
    val contentColor = if (isOptimal) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(colors = CardDefaults.cardColors(containerColor = containerColor), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isOptimal) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = contentColor, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(if (isOptimal) "Plant Status Optimal" else "Alert Triggered!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
                alerts.forEach { alert ->
                    val text = when (alert) {
                        is ConditionAlert.WaterNeeded -> "• Soil dry (${alert.current}% < min ${alert.required}%)"
                        is ConditionAlert.HighTemperature -> "• Too hot (${alert.current}°C > max ${alert.limit}°C)"
                        is ConditionAlert.LowHumidity -> "• Air too dry (${alert.current}% < min ${alert.required}%)"
                        is ConditionAlert.Optimal -> "All conditions within safe boundaries."
                    }
                    Text(text, style = MaterialTheme.typography.bodySmall, color = contentColor)
                }
            }
        }
    }
}

// ==========================================
// SENSOR GAUGE CARDS
// ==========================================
@Composable
fun SoilMoistureCard(telemetry: Telemetry, minMoisture: Int) {
    val isWarning = telemetry.soilMoisture < minMoisture
    SensorGaugeCard("Soil Moisture", "${telemetry.soilMoisture}%", "Min: $minMoisture%", (telemetry.soilMoisture / 100f).coerceIn(0f, 1f), Icons.Default.Opacity, if (isWarning) Color(0xFFE53935) else Color(0xFF1E88E5), isWarning)
}

@Composable
fun TemperatureCard(telemetry: Telemetry, maxTemp: Double) {
    val isWarning = telemetry.airTemperature > maxTemp
    SensorGaugeCard("Air Temperature", "${telemetry.airTemperature}°C", "Max: $maxTemp°C", (telemetry.airTemperature / 50f).toFloat().coerceIn(0f, 1f), Icons.Default.Thermostat, if (isWarning) Color(0xFFE53935) else Color(0xFFE57373), isWarning)
}

@Composable
fun HumidityCard(telemetry: Telemetry, minHumidity: Double) {
    val isWarning = telemetry.airHumidity < minHumidity
    SensorGaugeCard("Air Humidity", "${telemetry.airHumidity}%", "Min: $minHumidity%", (telemetry.airHumidity / 100f).toFloat().coerceIn(0f, 1f), Icons.Default.Cloud, if (isWarning) Color(0xFFE53935) else Color(0xFF4DB6AC), isWarning)
}

// ==========================================
// THRESHOLD SLIDERS
// ==========================================
@Composable
fun ThresholdControlCard(thresholds: Thresholds, onThresholdsChanged: (Thresholds) -> Unit) {
    var minMoisture by remember(thresholds) { mutableStateOf(thresholds.minSoilMoisture.toFloat()) }
    var maxTemp by remember(thresholds) { mutableStateOf(thresholds.maxAirTemperature.toFloat()) }
    var minHumidity by remember(thresholds) { mutableStateOf(thresholds.minAirHumidity.toFloat()) }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Min Soil Moisture: ${minMoisture.roundToInt()}%")
                Slider(
                    value = minMoisture, onValueChange = { minMoisture = it },
                    onValueChangeFinished = { onThresholdsChanged(thresholds.copy(minSoilMoisture = minMoisture.roundToInt())) },
                    valueRange = 10f..80f
                )
            }
            HorizontalDivider()
            Column {
                Text("Max Air Temperature: ${String.format("%.1f", maxTemp)}°C")
                Slider(
                    value = maxTemp, onValueChange = { maxTemp = it },
                    onValueChangeFinished = { onThresholdsChanged(thresholds.copy(maxAirTemperature = (maxTemp * 10).roundToInt() / 10.0)) },
                    valueRange = 15f..45f
                )
            }
            HorizontalDivider()
            Column {
                Text("Min Air Humidity: ${String.format("%.1f", minHumidity)}%")
                Slider(
                    value = minHumidity, onValueChange = { minHumidity = it },
                    onValueChangeFinished = { onThresholdsChanged(thresholds.copy(minAirHumidity = (minHumidity * 10).roundToInt() / 10.0)) },
                    valueRange = 20f..80f
                )
            }
        }
    }
}