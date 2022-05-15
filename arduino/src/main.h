#ifndef Main_H
#define Main_H

#define IN_BUFFER_SIZE 20  // client -> arduino buffer size
#define OUT_BUFFER_SIZE 23 // arduino -> client buffer size

#define DEBUG // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include <ESP8266WiFi.h>
#include "./controls/controls.h"
#include "./communication/communication.h"
#include "./monitoring/batteryMetric.h"
#include "./monitoring/ultrasonicSensor.h"

// Buffer implentation
struct _buffer;
typedef struct _buffer *buffer;
typedef struct _buffer
{
  char *data;
  size_t size;
} Buffer;

#endif