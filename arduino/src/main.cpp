#include "main.h"

// in_buffer used for transfering data, client -> communications -> controls
Buffer in_buffer;

// out_buffer used for transfering data, sensors -> communications -> client
Buffer out_buffer;

void setup()
{

#ifdef DEBUG
  delay(1000);
  Serial.begin(115200);
#endif

  // Initialize buffers

  in_buffer.data = NULL;
  in_buffer.data = (char *)malloc(IN_BUFFER_SIZE * sizeof(char));
  in_buffer.size = IN_BUFFER_SIZE;

#ifdef DEBUG
  if (in_buffer.data == NULL)
    Serial.printf("Failed to allocate memory.\n");
#endif

  out_buffer.data = NULL;
  out_buffer.data = (char *)malloc(OUT_BUFFER_SIZE * sizeof(char));
  out_buffer.size = OUT_BUFFER_SIZE;

#ifdef DEBUG
  if (out_buffer.data == NULL)
    Serial.printf("Failed to allocate memory.\n");
#endif

  // Initialize controls
  control_init();

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
   *
   */

  // Read ultrasonicSensor

  float ultrasonicSensor = ultrasonic_sensor_read();

  // Read batteryMetric

  reading batteryMetric = metrics_read();

  // Read singla strength

  int8_t rssi = communication_read_rssi();

  // Combine readings into shared buffer

  // appendFloatToBuffer(&out_buffer, ultrasonicSensor);
  // appendReadingToBuffer(&out_buffer, batteryMetric);
  // appendLongToBuffer(&out_buffer, rssi);

  int written_bytes = snprintf(out_buffer.data, OUT_BUFFER_SIZE, "%.2f&%.2f&%.2f&%.2f&%d\0", batteryMetric->current, batteryMetric->voltage1, batteryMetric->voltage2, ultrasonicSensor, rssi);

  if (written_bytes > 0)
    communication_send_data(out_buffer.data); // send data to remote UDP server

  /**
   * Controls PART
   * Android client --> UDP server
   *
   */

  // read incoming UDP stream
  boolean newData = communication_read_data(in_buffer.data, in_buffer.size);
  // check if any new message
  if (newData)
    // send incoming stream to processing
    control_update(in_buffer.data);

#ifdef DEBUG
  Serial.printf("END OF LOOP\n\n");
  delay(1000);
#endif

#ifndef DEBUG
  delay(10);
#endif
}
