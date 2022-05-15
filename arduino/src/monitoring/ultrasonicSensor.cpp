#include "ultrasonicSensor.h"

// defines variables
long duration;
// variable for the duration of sound wave travel
int distance;
// variable for the distance measurement

void init_ultrasonic_sensor()
{
    // Sets the trigPin as an OUTPUT
    pinMode(trigPin, OUTPUT);

    // Sets the echoPin as an INPUT
    pinMode(echoPin, INPUT);
}
float get_ultrasonic_distance()
{
    // Clears the trigPin condition
    digitalWrite(trigPin, LOW); //
    delayMicroseconds(20000);
    delay((distance % 100) * 10); // Sets the trigPin HIGH (ACTIVE) for 10 microseconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);        // Reads the echoPin, returns the sound wave travel time in microseconds
    duration = pulseIn(echoPin, HIGH); // Calculating the distance
    distance = duration * 0.034 / 2;   // Speed of sound wave divided by 2 (go and back)

    /*    if (distance <= 30)
        {
            Serial.print("Distance: ");
            Serial.print(distance);
            Serial.println(" cm");
        }
        */
    return distance;
}
