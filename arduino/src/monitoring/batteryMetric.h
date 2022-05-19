
#ifndef Battery_metric_h
#define Battery_metric_h

#define DEBUG // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include <ESP8266WiFi.h>

/**
 *
 * Responsible for reading battery cells voltage levels and current load
 *
 */

#define VOLTAGE1 13
#define VOLTAGE2 15
#define CURRENT 2

// Voltage dividor R values defined

#define VOLTAGE_DIVIDER_R1 100000
#define VOLTAGE_DIVIDER_R2 10000

struct _reading;
typedef struct _reading *reading;
typedef struct _reading
{
    float voltage1;
    float voltage2;
    float current;
} Reading;

void metrics_init();
reading metrics_read();

#endif