#include "batteryMetric.h"

float voltage1;

const int analogInPin = A0;

void metrics_init()
{
#ifdef DEBUG
    Serial.printf("### Battery metric - pins init \n");
#endif

    pinMode(analogInPin, INPUT); // assigning the input port
}

float metrics_read()
{

#ifdef DEBUG
    Serial.printf("### Battery metric - reading data \n");
#endif

    voltage1 = readVoltage(analogInPin);

    return voltage1;
}

inline float readVoltage(int pin)
{
    int val;
    float Vin;

    val = analogRead(pin); // reads the analog input

    /**
     *
     * ESP reads voltage 0 - 1 V
     * Croduino Nova2 has voltage divider and increases range up to 0 - 5 V
     * Another voltage divider is added to increase voltage up
     * Analog read resolution -> 1024 = 1 V
     *
     * U - Input voltage to voltage dividers
     * U1 - Voltage at ESP8266 after voltage dividers
     *
     *-------------
     * U = 14 * U1
     *
     */

    Vin = (val * 14) / 1024.00; // formula for calculating voltage out

    return Vin;
}
