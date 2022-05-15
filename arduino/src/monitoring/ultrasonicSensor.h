
#ifndef Ultrasonic_sensor_h
#define Ultrasonic_sensor_h

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

// attach pin D2 Arduino to pin Echo of HC-SR04
#define echoPin 2

// attach pin D3 Arduino to pin Trig of HC-SR04
#define trigPin 3

void ultrasonic_sensor_init();
float ultrasonic_sensor_read();

#endif