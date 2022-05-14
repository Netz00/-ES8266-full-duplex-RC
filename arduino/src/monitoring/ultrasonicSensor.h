
#include <ESP8266WiFi.h>
// ----------------------------------------------------------------
//
// Arduino Ultrasoninc Sensor HC-SR04
// Re-writed by Arbi Abdul Jabbaar
// Using Arduino IDE 1.8.7
// Using HC-SR04 Module
// Tested on 17 September 2019
// ----------------------------------------------------------------
//
#define echoPin 2
// attach pin D2 Arduino to pin Echo of HC-SR04
#define trigPin 3
// attach pin D3 Arduino to pin Trig of HC-SR04
// defines variables
long duration;
// variable for the duration of sound wave travel
int distance;
// variable for the distance measurement

void init_ultrasonic_sensor();
float get_ultrasonic_distance();