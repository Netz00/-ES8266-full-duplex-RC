#include "controls.h"

void control_init()
{

    delay(500);

    data = NULL;
    data = (pos)malloc(sizeof(CarPostion));

#ifdef DEBUG
    if (data == NULL)
        Serial.printf("Failed to allocate memory.\n");
#endif

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

void changeOfDirection(char *in_buffer)
{

    // Intepreting buffer data into local variables

    sscanf(in_buffer, "%u&%u:%u&%ue", &(data->forward), &(data->backward), &(data->left), &(data->right));

    // Updating GPIO pins state by local variables values

    if (data->forward != data->backward) // if not 0 both
    {
        if (data->backward == 0)
        {
            analogWrite(PWM_B, 0);
            delayMicroseconds(200);
            analogWrite(PWM_F, data->forward);
        }
        if (data->forward == 0)
        {
            analogWrite(PWM_F, 0);
            delayMicroseconds(200);
            analogWrite(PWM_B, data->backward);
        }
    }
    else
    {
        analogWrite(PWM_F, 0);
        analogWrite(PWM_B, 0);
    }

    if (data->left != data->right) // if not 0 both
    {
        if (data->right == 0)
        {
            analogWrite(PWM_R, 0);
            delayMicroseconds(200);
            analogWrite(PWM_L, data->left);
        }
        if (data->left == 0)
        {
            analogWrite(PWM_L, 0);
            delayMicroseconds(200);
            analogWrite(PWM_R, data->right);
        }
    }
    else
    {
        analogWrite(PWM_L, 0);
        analogWrite(PWM_R, 0);
    }

    // in_buffer[0] = '\0';
}