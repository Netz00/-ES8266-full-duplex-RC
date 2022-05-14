#include <ESP8266WiFi.h>

#define IN_BUFFER_SIZE 20  // client -> arduino buffer size
#define OUT_BUFFER_SIZE 23 // arduino -> client buffer size

#define DEBUG 1 // DEBUG = 0 -> DEBUG mode ON, otherwise OFF

#include "./controls/controls.h"
//#include "./communication/communication.h"
#include "./monitoring/batteryMetric.h"
//#include "./monitoring/ultrasonicSensor.h"


// Buffer implentation
struct _buffer;
typedef struct _buffer *buffer;
typedef struct _buffer
{
  char *end_of_buffer;
  char *data;
  size_t remaining_space;
  size_t size;
} Buffer;

/*
void appendFloatToBuffer(buffer buffer, float toAppend);
void appendLongToBuffer(buffer buffer, long toAppend);
void appendReadingToBuffer(buffer buffer, reading toAppend);
*/

// in_buffer used for transfering data, client -> communications -> controls
buffer in_buffer;

// out_buffer used for transfering data, sensors -> communications -> client
buffer out_buffer;