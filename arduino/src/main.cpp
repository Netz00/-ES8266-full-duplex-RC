#include "main.h"

// in_buffer used for transfering data, client -> communications -> controls
char *in_buffer;

// out_buffer used for transfering data, sensors -> communications -> client
char *out_buffer;

byte counter = 0;
int ultrasonicSensor = 50;

float voltage;
int8_t rssi;

void setup()
{

#ifdef DEBUG
  delay(1000);
  Serial.begin(115200);
#endif

  // Initialize controls
  control_init();

  // Initialize buffers

  in_buffer = NULL;
  in_buffer = (char *)malloc(IN_BUFFER_SIZE * sizeof(char));

#ifdef DEBUG
  if (in_buffer == NULL)
    Serial.printf("Failed to allocate memory.\n");
#endif

  out_buffer = NULL;
  out_buffer = (char *)malloc(OUT_BUFFER_SIZE * sizeof(char));

#ifdef DEBUG
  if (out_buffer == NULL)
    Serial.printf("Failed to allocate memory.\n");
#endif

  // Initialize metrics
  metrics_init();

  // Initialize ultrasonicSensor
  ultrasonic_sensor_init();

  // Initialize communication
  communication_init();

#ifdef DEBUG
  Serial.printf("END OF INIT PHASE.\n\n");
#endif
}

void loop()
{

  /**
   * Monitoring PART
   * Android client --> UDP server
   **/

  if ((counter++) % 12 == 0)
  {

    // Read ultrasonicSensor

    ultrasonicSensor = ultrasonic_sensor_read();

    // Read batteryMetric

    voltage = metrics_read();

    // Read singla strength

    rssi = communication_read_rssi();

    // Combine readings into shared buffer

    int written_bytes = snprintf(
        out_buffer,
        OUT_BUFFER_SIZE,
        "%.2f&%.2f&%.2f&%d&%d\0",
        voltage,
        0.00,
        0.00,
        ultrasonicSensor,
        rssi);

    if (written_bytes > 0)
      communication_send_data(out_buffer); // send data to remote UDP server
  }
  /**
   * Controls PART
   * Android client --> UDP server
   *
   */

  // read incoming UDP stream + check if any new message
  if (communication_read_data(in_buffer, IN_BUFFER_SIZE))
    // send incoming stream to processing
    control_update(in_buffer);

#ifdef DEBUG
  Serial.printf("END OF LOOP\n\n");
#endif

  delay(10);
}
