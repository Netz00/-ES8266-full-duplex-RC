/***
 *
 * Responsible for intepreting in_buffer into GPIO OUTPUTs
 *
 * INPUT - in_buffer
 * OUTPUT - GPIO pins
 *
 * */

#include <ESP8266WiFi.h>

#define new_frequency 50 // use PWM frequency optimised for your hardware

// Pinout definition

#define PWM_F 5  // forward, D2
#define PWM_B 16 // backward, D1
#define PWM_L 0  // left, D3
#define PWM_R 4  // right, D4

// structure used for controling GPIO states

struct _pos;
typedef struct _pos *pos;
typedef struct _pos
{
  size_t forward;
  size_t backward;
  size_t left;
  size_t right;
} CarPostion;

pos data;

// Initialising GPIO outputs
void control_init();

// Intepreting in_buffer into GPIO output pins state
void changeOfDirection(char *in_buffer);