#include "ultrasonicSensor.h"

// defines variables
long duration; // variable for the duration of sound wave travel
int distance;  // variable for the distance measurement

void ultrasonic_sensor_init()
{

#ifdef DEBUG
    Serial.printf("### Ultrasonic sensor - init \n");
#endif

    // Sets the trigPin as an OUTPUT
    pinMode(trigPin, OUTPUT);

    // Sets the echoPin as an INPUT
    pinMode(echoPin, INPUT);
}
int ultrasonic_sensor_read()
{

    // Clears the trigPin condition

    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);

    // Sets the trigPin HIGH (ACTIVE) for 10 microseconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(5);
    digitalWrite(trigPin, LOW);

    // Reads the echoPin, returns the sound wave travel time in microseconds
    duration = pulseIn(echoPin, HIGH);

    // Convert the time into a distance
    distance = duration / 58.2; // Speed of sound wave divided by 2 (go and back)
    // => (duration/2) / 29.1;     // Divide by 29.1 or multiply by 0.0343

    if (distance > 50)
        distance = 50;

#ifdef DEBUG
    Serial.printf("### Ultrasonic sensor - reading distance \n");
    Serial.print("Distance: ");
    Serial.print(distance);
    Serial.println(" cm");

#endif

    return distance;
}
