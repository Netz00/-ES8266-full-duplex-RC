#ifndef Main_H
#define Main_H

#define IN_BUFFER_SIZE 20  // client -> arduino buffer size
#define OUT_BUFFER_SIZE 31 // arduino -> client buffer size

// current voltage1 voltage2 ultrasonicSensor rssi
//    5   &   5    &   5    &       5        & 6  \0
// => 31 character

// #define DEBUG // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include <ESP8266WiFi.h>
#include "./controls/controls.h"
#include "./communication/communication.h"
#include "./monitoring/batteryMetric.h"
#include "./monitoring/ultrasonicSensor.h"

#endif