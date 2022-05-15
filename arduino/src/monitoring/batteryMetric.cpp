#include "batteryMetric.h"

Reading metric;

void metrics_init()
{
#ifdef DEBUG
    Serial.printf("### Battery metric - pins init ###");
#endif
}

reading metrics_read()
{

#ifdef DEBUG
    Serial.printf("### Battery metric - reading data ###");
#endif

    metric.current = 10.324;
    metric.voltage1 = 4.324;
    metric.voltage1 = 4.324;

    return &metric;
}
