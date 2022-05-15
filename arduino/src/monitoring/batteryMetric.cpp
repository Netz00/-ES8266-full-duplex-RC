#include "batteryMetric.h"

Reading metric;

void metrics_init()
{
#ifdef DEBUG
    Serial.printf("### Battery metric - pins init \n");
#endif
}

reading metrics_read()
{

#ifdef DEBUG
    Serial.printf("### Battery metric - reading data \n");
#endif

    metric.current = 10.324;
    metric.voltage1 = 4.324;
    metric.voltage2 = 4.324;

    return &metric;
}
