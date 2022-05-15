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
  in_buffer.end_of_buffer = in_buffer.data;
  in_buffer.remaining_space = sizeof(buffer);
  in_buffer.size = IN_BUFFER_SIZE;

#ifdef DEBUG
  if (in_buffer.data == NULL)
    Serial.printf("Failed to allocate memory.\n");
#endif

  out_buffer.data = NULL;
  out_buffer.data = (char *)malloc(OUT_BUFFER_SIZE * sizeof(char));
  out_buffer.end_of_buffer = out_buffer.data;
  out_buffer.remaining_space = sizeof(buffer);
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

  long rssi = communication_read_rssi();

  // Combine readings into shared buffer

  appendFloatToBuffer(&out_buffer, ultrasonicSensor);

  appendReadingToBuffer(&out_buffer, batteryMetric);

  appendLongToBuffer(&out_buffer, rssi);

  // Add terminating char
  out_buffer.end_of_buffer[0] = '\0';

  // send data to remote UDP server
  communication_send_data(out_buffer.data);

  /**
   * Controls PART
   * Android client --> UDP server
   *
   */

  // read incoming UDP stream
  communication_read_data(in_buffer.data, in_buffer.size);
  // check if any new message
  if (strlen(in_buffer.data) > 0)
    // send incoming stream to processing
    control_update(in_buffer.data);

#ifdef DEBUG
  Serial.printf("END OF LOOP\n\n");
#endif
  delay(10);
}

void appendFloatToBuffer(buffer buffer, float toAppend)
{
  int written_bytes = snprintf(buffer->end_of_buffer, buffer->remaining_space, "%f&", toAppend);

  if (written_bytes > 0)
  {
    buffer->end_of_buffer += written_bytes;
    buffer->remaining_space -= written_bytes;
  }
}
void appendLongToBuffer(buffer buffer, long toAppend)
{
  int written_bytes = snprintf(buffer->end_of_buffer, buffer->remaining_space, "%ld&", toAppend);

  if (written_bytes > 0)
  {
    buffer->end_of_buffer += written_bytes;
    buffer->remaining_space -= written_bytes;
  }
}
void appendReadingToBuffer(buffer buffer, reading toAppend)
{
  int written_bytes = snprintf(buffer->end_of_buffer, buffer->remaining_space, "%lf&%lf&%lf&", toAppend->current, toAppend->voltage1, toAppend->voltage2);

  if (written_bytes > 0)
  {
    buffer->end_of_buffer += written_bytes;
    buffer->remaining_space -= written_bytes;
  }
}
