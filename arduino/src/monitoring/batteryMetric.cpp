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

    val = analogRead(pin);        // reads the analog input
    Vin = (val * 5.00) / 2048.00; // formula for calculating voltage out

    return Vin;
}
