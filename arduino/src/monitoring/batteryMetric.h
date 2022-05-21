
#ifndef Battery_metric_h
#define Battery_metric_h

#define DEBUG // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include <ESP8266WiFi.h>

/**
 *
 * Responsible for reading battery cells voltage levels and current load
 *
 */

void metrics_init();
float metrics_read();
inline float readVoltage(int pin);

#endif