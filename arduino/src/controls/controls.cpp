#include "controls.h"

CarPostion position;

void control_init()
{

#ifdef DEBUG
    Serial.printf("### Controls - init \n");
#endif

    delay(500);

    // Updating PWM frequency

    analogWriteFreq(new_frequency);

    // Initializing GPIO pins

    pinMode(PWM_F, OUTPUT);
    pinMode(PWM_B, OUTPUT);
    pinMode(PWM_L, OUTPUT);
    pinMode(PWM_R, OUTPUT);

    // Updating GPIO pins

    analogWrite(PWM_F, 0);
    analogWrite(PWM_B, 0);
    analogWrite(PWM_L, 0);
    analogWrite(PWM_R, 0);
}

void control_update(char *in_buffer)
{

#ifdef DEBUG
    Serial.printf("### Controls - update pins \n");
#endif

    // Intepreting buffer position into local variables

    sscanf(in_buffer, "%u&%u:%u&%ue", &(position.forward), &(position.backward), &(position.left), &(position.right));

    // Updating GPIO pins state by local variables values

    if (position.forward != position.backward) // if not 0 both
    {
        if (position.backward == 0)
        {
            analogWrite(PWM_B, 0);
            delayMicroseconds(200);
            analogWrite(PWM_F, position.forward);
        }
        if (position.forward == 0)
        {
            analogWrite(PWM_F, 0);
            delayMicroseconds(200);
            analogWrite(PWM_B, position.backward);
        }
    }
    else
    {
        analogWrite(PWM_F, 0);
        analogWrite(PWM_B, 0);
    }

    if (position.left != position.right) // if not 0 both
    {
        if (position.right == 0)
        {
            analogWrite(PWM_R, 0);
            delayMicroseconds(200);
            analogWrite(PWM_L, position.left);
        }
        if (position.left == 0)
        {
            analogWrite(PWM_L, 0);
            delayMicroseconds(200);
            analogWrite(PWM_R, position.right);
        }
    }
    else
    {
        analogWrite(PWM_L, 0);
        analogWrite(PWM_R, 0);
    }

    // in_buffer[0] = '\0';
}