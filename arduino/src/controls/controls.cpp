#include "controls.h"

CarPosition position;

void control_init()
{

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

    // Intepreting buffer position into local variables

    sscanf(in_buffer, "%u&%u:%u&%ue", &(position.forward), &(position.backward), &(position.left), &(position.right));

#ifdef DEBUG
    Serial.printf("### Controls - update pins \nforward: %u backward: %u left: %u right: %u \n", position.forward, position.backward, position.left, position.right);
#endif

    // Updating GPIO pins state by local variables values

    if (position.forward != position.backward) // if not 0 both
    {
        if (position.backward == 0)
        {
            analogWrite(PWM_B, 0);
            analogWrite(PWM_F, position.forward);
        }
        if (position.forward == 0)
        {
            analogWrite(PWM_F, 0);
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
            analogWrite(PWM_L, position.left);
        }
        if (position.left == 0)
        {
            analogWrite(PWM_L, 0);
            analogWrite(PWM_R, position.right);
        }
    }
    else
    {
        analogWrite(PWM_L, 0);
        analogWrite(PWM_R, 0);
    }

}