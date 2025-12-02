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
const char* firebase_host = "https://dispensador-1c7a0-default-rtdb.firebaseio.com";

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

int nivelAgua = 0;
long pesoMaximoTaza = 250; // 100% = 250g

bool horarioEjecutado = false;

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
int calcularNivelAgua(long peso);

// -------------------------------------------------------------
// CÁLCULO PORCENTAJE DE LLENADO
// -------------------------------------------------------------
int calcularNivelAgua(long peso) {
  if (!tazaPresente) return 0;

  long pesoLiquido = peso - PESO_TAZA_VACIA;
  if (pesoLiquido < 0) pesoLiquido = 0;

  int porcentaje = (pesoLiquido * 100) / pesoMaximoTaza;

  if (porcentaje < 0) porcentaje = 0;
  if (porcentaje > 100) porcentaje = 100;

  return porcentaje;
}

// -------------------------------------------------------------
// SETUP
// -------------------------------------------------------------
void setup() {
  Serial.begin(115200);
  delay(300);

  scale.begin(DT, CLK);
  scale.set_scale(calibration_factor);
  scale.tare();

  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);

  Serial.print("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado.");

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

    nivelAgua = calcularNivelAgua(pesoActual);

    lastSensorRead = now;
  }

  handleLogica();

  if (now - lastControlRead >= 3000) {
    leerControl();
    lastControlRead = now;
  }

  if (now - lastEstadoUpdate >= 5000) {
    actualizarEstado();
    lastEstadoUpdate = now;
  }

  if (now - lastScheduleCheck >= 60000) {
    leerHorarios();
    lastScheduleCheck = now;
  }
}

// -------------------------------------------------------------
// FIREBASE PUT
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

// -------------------------------------------------------------
// FIREBASE GET
// -------------------------------------------------------------
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
// LEER CONTROL MANUAL
// -------------------------------------------------------------
void leerControl() {
  String response;
  if (!firebaseGET(control_url, response)) return;
  if (response == "null") return;

  StaticJsonDocument<400> doc;
  deserializeJson(doc, response);

  modoAutomatico     = doc["modoAutomatico"] | false;
  programacionActiva = doc["programacionActiva"] | false;

  long cantidad = doc["cantidadDispensado"] | 0;

  if (cantidad > 0) {
    Serial.printf("Dispensado manual solicitado: %ld\n", cantidad);
    dispensar(cantidad);
    firebasePUT(control_url, "{\"cantidadDispensado\":0}");
  }
}

// -------------------------------------------------------------
// ACTUALIZAR ESTADO
// -------------------------------------------------------------
void actualizarEstado() {
  StaticJsonDocument<300> j;

  j["peso"]         = pesoActual;
  j["tazaPresente"] = tazaPresente;
  j["llenando"]     = llenando;
  j["nivelAgua"]    = nivelAgua;

  String json;
  serializeJson(j, json);
  firebasePUT(estado_url, json);

  Serial.println("Estado actualizado.");
}

// -------------------------------------------------------------
// LÓGICA DE LLENADO
// -------------------------------------------------------------
void handleLogica() {
  if (llenadoEnCurso) {

    if (pesoActual >= pesoMaximoTaza) {
      Serial.println("⚠️ Límite de seguridad alcanzado. Deteniendo bomba.");
      desactivarBomba();
      llenadoEnCurso = false;
      return;
    }

    if (pesoActual < objetivoPeso) {
      activarBomba();
    } else {
      desactivarBomba();
      llenadoEnCurso = false;
      Serial.println("Dispensado completado.");
    }

    return;
  }
}

// -------------------------------------------------------------
// INICIAR DISPENSADO
// -------------------------------------------------------------
void dispensar(long cantidad) {
  if (!tazaPresente) {
    Serial.println("❌ No hay taza.");
    return;
  }

  if (llenadoEnCurso) return;

  objetivoPeso = pesoActual + cantidad;

  if (objetivoPeso > pesoMaximoTaza) {
    objetivoPeso = pesoMaximoTaza;
  }

  llenadoEnCurso = true;

  Serial.printf("Iniciando dispensado: objetivo %ld g\n", objetivoPeso);
}

// -------------------------------------------------------------
// CONTROL BOMBA
// -------------------------------------------------------------
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
// HORARIOS AUTOMÁTICOS
// -------------------------------------------------------------
void leerHorarios() {
  String response;
  if (!firebaseGET(horarios_url, response)) return;
  if (response == "null") return;

  StaticJsonDocument<300> doc;
  deserializeJson(doc, response);

  bool activo = doc["activo"] | false;
  if (!activo) return;

  if (!modoAutomatico || !programacionActiva) return;

  String horaProgramada = doc["hora"] | "";
  int cantidadMl = doc["cantidad"] | 0;

  JsonArray dias = doc["dias"];

  time_t now = time(nullptr);
  struct tm *t = localtime(&now);

  int diaSemana = t->tm_wday;

  bool diaCoincide = false;
  for (int d : dias) {
    if (d == diaSemana) diaCoincide = true;
  }
  if (!diaCoincide) return;

  char horaActual[6];
  snprintf(horaActual, 6, "%02d:%02d", t->tm_hour, t->tm_min);

  if (horaProgramada == horaActual) {

    if (!horarioEjecutado) {
      Serial.println("⏰ EJECUTANDO HORARIO AUTOMÁTICO");

      long cantidad = cantidadMl;

      if (pesoActual + cantidad > pesoMaximoTaza) {
        cantidad = pesoMaximoTaza - pesoActual;
        if (cantidad < 0) cantidad = 0;
      }

      if (cantidad > 0) dispensar(cantidad);

      horarioEjecutado = true;
    }
  } else {
    horarioEjecutado = false;
  }
}
