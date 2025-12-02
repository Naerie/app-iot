#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include "HX711.h"

// -------------------------------------------------------------
// CONFIG WIFI
// -------------------------------------------------------------
const char* ssid = "Katja";
const char* password = "Tomate124";

// -------------------------------------------------------------
// CONFIG FIREBASE (REST API)
// -------------------------------------------------------------
const char* firebase_host = "https://dispensador-b07b1-default-rtdb.firebaseio.com";

// Rutas Firebase
String estado_url      = String(firebase_host) + "/dispensador/estado.json";
String control_url     = String(firebase_host) + "/dispensador/control.json";
String horarios_url    = String(firebase_host) + "/dispensador/horarios.json";

// -------------------------------------------------------------
// HARDWARE
// -------------------------------------------------------------
const int DT = D2;
const int CLK = D3;
const int RELAY_PIN = D5;

HX711 scale;
float calibration_factor = 971.664;
const long PESO_TAZA_VACIA = 6;

// -------------------------------------------------------------
// VARIABLES
// -------------------------------------------------------------
long pesoActual = 0;
bool tazaPresente = false;
bool llenando = false;
int nivelAgua = 100;

bool modoAutomatico = false;
bool programacionActiva = false;

long cantidadManual = 0;

long objetivoPeso = 0;
bool llenadoEnCurso = false;

unsigned long lastSensorRead = 0;
unsigned long lastEstadoUpdate = 0;
unsigned long lastControlRead = 0;
unsigned long lastScheduleCheck = 0;

// -------------------------------------------------------------
// WIFI CLIENT HTTPS
// -------------------------------------------------------------
WiFiClientSecure client;

// -------------------------------------------------------------
// PROTOTIPOS
// -------------------------------------------------------------
bool firebasePUT(String url, String json);
bool firebaseGET(String url, String &response);

void leerControl();
void actualizarEstado();
void handleLogica();
void dispensar(long cantidad);
void activarBomba();
void desactivarBomba();
void leerHorarios();

// -------------------------------------------------------------
// SETUP
// -------------------------------------------------------------
void setup() {
  Serial.begin(115200);
  delay(300);

  // HX711
  scale.begin(DT, CLK);
  scale.set_scale(calibration_factor);
  scale.tare();

  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);

  // WiFi
  Serial.print("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado.");

  // HTTPS sin verificar certificado (desarrollo)
  client.setInsecure();

  Serial.println("Sistema listo.");
}

// -------------------------------------------------------------
// LOOP PRINCIPAL
// -------------------------------------------------------------
void loop() {
  unsigned long now = millis();

  // Leer sensor cada 1s
  if (now - lastSensorRead >= 1000) {
    if (scale.is_ready()) {
      pesoActual = scale.get_units(5);
      tazaPresente = (pesoActual > PESO_TAZA_VACIA);
    }
    lastSensorRead = now;
  }

  // Lógica de dispensado
  handleLogica();

  // Leer control cada 3s
  if (now - lastControlRead >= 3000) {
    leerControl();
    lastControlRead = now;
  }

  // Actualizar estado cada 5s
  if (now - lastEstadoUpdate >= 5000) {
    actualizarEstado();
    lastEstadoUpdate = now;
  }

  // Leer horarios cada 1 minuto
  if (now - lastScheduleCheck >= 60000) {
    leerHorarios();
    lastScheduleCheck = now;
  }
}

// -------------------------------------------------------------
// FUNCIONES FIREBASE REST
// -------------------------------------------------------------
bool firebasePUT(String url, String json) {
  HTTPClient https;

  if (!https.begin(client, url)) return false;

  https.addHeader("Content-Type", "application/json");
  int code = https.PUT(json);

  if (code == 200 || code == 204) {
    https.end();
    return true;
  }
  Serial.printf("PUT error %d: %s\n", code, https.errorToString(code).c_str());
  https.end();
  return false;
}

bool firebaseGET(String url, String &response) {
  HTTPClient https;

  if (!https.begin(client, url)) return false;
  int code = https.GET();

  if (code == 200) {
    response = https.getString();
    https.end();
    return true;
  }
  Serial.printf("GET error %d: %s\n", code, https.errorToString(code).c_str());
  https.end();
  return false;
}

// -------------------------------------------------------------
// LEER CONTROL DESDE FIREBASE
// -------------------------------------------------------------
void leerControl() {
  String response;
  if (!firebaseGET(control_url, response)) return;

  if (response == "null") return;

  StaticJsonDocument<400> doc;
  deserializeJson(doc, response);

  modoAutomatico       = doc["modoAutomatico"] | false;
  programacionActiva   = doc["programacionActiva"] | false;

  long cantidad = doc["cantidadDispensado"] | 0;

  if (cantidad > 0) {
    cantidadManual = cantidad;
    Serial.printf("Dispensado manual solicitado: %ld\n", cantidadManual);
    dispensar(cantidadManual);

    // Reset en Firebase
    firebasePUT(control_url, "{\"cantidadDispensado\":0}");
  }
}

// -------------------------------------------------------------
// ACTUALIZAR ESTADO EN FIREBASE
// -------------------------------------------------------------
void actualizarEstado() {
  StaticJsonDocument<300> j;
  j["peso"] = pesoActual;
  j["tazaPresente"] = tazaPresente;
  j["llenando"] = llenando;
  j["nivelAgua"] = nivelAgua;

  String json;
  serializeJson(j, json);
  firebasePUT(estado_url, json);

  Serial.println("Estado actualizado.");
}

// -------------------------------------------------------------
// LÓGICA DE DISPENSADO
// -------------------------------------------------------------
void handleLogica() {
  if (llenadoEnCurso) {
    if (pesoActual < objetivoPeso) {
      activarBomba();
    } else {
      desactivarBomba();
      llenadoEnCurso = false;
      Serial.println("Dispensado completado.");
    }
    return;
  }

  // Automático basado en horarios
  if (modoAutomatico && programacionActiva) {
    leerHorarios();
  }
}

void dispensar(long cantidad) {
  if (!tazaPresente) {
    Serial.println("❌ No hay taza.");
    return;
  }

  if (llenadoEnCurso) return;

  objetivoPeso = pesoActual + cantidad;
  llenadoEnCurso = true;

  Serial.printf("Iniciando dispensado: objetivo %ld g\n", objetivoPeso);
}

void activarBomba() {
  if (!llenando) {
    digitalWrite(RELAY_PIN, HIGH);
    llenando = true;
  }
}

void desactivarBomba() {
  digitalWrite(RELAY_PIN, LOW);
  llenando = false;
}

// -------------------------------------------------------------
// LEER HORARIOS (SIMPLE)
// -------------------------------------------------------------
void leerHorarios() {
  String response;
  if (!firebaseGET(horarios_url, response)) return;

  if (response == "null") return;

  Serial.println("Horarios leídos (no ejecutados, solo debug):");
  Serial.println(response);
}
