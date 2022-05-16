#include "batteryMetric.h"

Reading metric;

void metrics_init()
{
#ifdef DEBUG
    Serial.printf("### Battery metric - pins init \n");
#endif

    // pinMode(VOLTAGE1, INPUT); //assigning the input port
    // pinMode(VOLTAGE2, INPUT); //assigning the input port
    // pinMode(CURRENT, INPUT); //assigning the input port
}

reading metrics_read()
{

#ifdef DEBUG
    Serial.printf("### Battery metric - reading data \n");
#endif

    // metric.voltage1 = readVoltage(VOLTAGE1);
    // metric.voltage2 = readVoltage(VOLTAGE2);

    metric.current = 10.324;
    metric.voltage1 = 4.324;
    metric.voltage2 = 4.324;

    return &metric;
}

float readVoltage(int pin)
{
    int val, Vout, Vin;

    val = analogRead(VOLTAGE1);                                                    // reads the analog input
    Vout = (val * 5.00) / 1024.00;                                                 // formula for calculating voltage out i.e. V+, here 5.00
    Vin = Vout / (VOLTAGE_DIVIDER_R2 / (VOLTAGE_DIVIDER_R1 + VOLTAGE_DIVIDER_R2)); // formula for calculating voltage in i.e. GND
    if (Vin < 0.09)                                                                // condition
    {
        Vin = 0.00; // statement to quash undesired reading !
    }

    return Vin;
}
