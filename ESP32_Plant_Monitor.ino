#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <DHT.h>

#define WIFI_SSID "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"
#define DATABASE_URL "https://YOUR_FIREBASE_PROJECT.firebaseio.com/"
#define DATABASE_SECRET "YOUR_FIREBASE_DATABASE_SECRET"

#define DEVICE_ID "ESP32_PLANT_01"
#define DEVICE_NAME "Balcony Fern Node"

// Sensor Pins & Config
#define PIN_SOIL_MOISTURE 34
#define PIN_DHT 4
#define DHTTYPE DHT11 // Change to DHT22 if using that model

DHT dht(PIN_DHT, DHTTYPE);

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastSendTime = 0;
const unsigned long sendInterval = 5000;

void setup() {
  Serial.begin(115200);
  dht.begin();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // Register device node if it does not exist
  String devPath = "/devices/" + String(DEVICE_ID);
  if (!Firebase.RTDB.pathExists(&fbdo, devPath)) {
    Firebase.RTDB.setString(&fbdo, devPath + "/deviceName", DEVICE_NAME);
    Firebase.RTDB.setString(&fbdo, devPath + "/ownerUid", ""); 
    Firebase.RTDB.setInt(&fbdo, devPath + "/thresholds/minSoilMoisture", 40);
    Firebase.RTDB.setFloat(&fbdo, devPath + "/thresholds/maxAirTemperature", 35.0);
    Firebase.RTDB.setFloat(&fbdo, devPath + "/thresholds/minAirHumidity", 40.0);
  }
}

void loop() {
  if (Firebase.ready() && (millis() - lastSendTime > sendInterval)) {
    lastSendTime = millis();

    // Read Soil Moisture
    int rawMoisture = analogRead(PIN_SOIL_MOISTURE);
    int moisturePct = map(rawMoisture, 4095, 1500, 0, 100);
    moisturePct = constrain(moisturePct, 0, 100);

    // Read DHT Sensor
    float humidity = dht.readHumidity();
    float temperature = dht.readTemperature();

    // Check if DHT read failed
    if (isnan(humidity) || isnan(temperature)) {
      Serial.println("Failed to read from DHT sensor!");
      return;
    }

    String telemPath = "/devices/" + String(DEVICE_ID) + "/telemetry";
    
    FirebaseJson json;
    json.set("soilMoisture", moisturePct);
    json.set("airTemperature", temperature);
    json.set("airHumidity", humidity);
    json.set("lastUpdated", (int)time(NULL));

    Firebase.RTDB.setJSON(&fbdo, telemPath, &json);
  }
}