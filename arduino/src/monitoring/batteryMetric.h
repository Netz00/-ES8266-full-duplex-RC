
#ifndef Battery_metric_h
#define Battery_metric_h

#include <ESP8266WiFi.h>

/**
 *
 * Responsible for reading battery cells voltage levels and current load
 *
 */

#define VOLTAGE1 2
#define VOLTAGE2 3
#define CURRENT 4

struct _reading;
typedef struct _reading *reading;
typedef struct _reading
{
    float voltage1;
    float voltage2;
    float current;
} Reading;

void init_metrics();
reading readMetrics();

#endif