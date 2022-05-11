#include <ESP8266WiFi.h>

#define IN_BUFFER_SIZE 255  // client -> arduino buffer size
#define OUT_BUFFER_SIZE 255 // arduino -> client buffer size

#define DEBUG 1 // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include "controls.h"
#include "communication.h"

// in_buffer used for transfering data, client -> communications -> controls
char *in_buffer;

// out_buffer used for transfering data, sensors -> communications -> client
char *out_buffer;

void setup()
{

#ifdef DEBUG
  Serial.begin(115200);
#endif

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

  // Initialize controls
  control_init();

  // Initialize communication
  init_communictaion();
}

void loop()
{

  // check if any and if, then read incoming data stream
  read_data(in_buffer);

  // check if any new message
  if (strlen(in_buffer) > 0)
    // send stream to processing
    changeOfDirection(in_buffer);

  delay(10);
}
