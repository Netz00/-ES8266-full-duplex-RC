#include "communication.h"

void init_communictaion()
{
#ifdef DEBUG
    Serial.printf("Connecting to %s ", ssid);
#endif

    // Connecting to Wi-Fi network

    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);

#ifdef DEBUG
        Serial.print(".");
#endif
    }

#ifdef DEBUG
    Serial.println(" connected");
#endif

    // Opeingn UDP socket

    Udp.begin(localUdpPort);
    Udp.flush();

#ifdef DEBUG
    Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.localIP().toString().c_str(), localUdpPort);
#endif
}

void read_data(char *in_buffer)
{
    // Checking if there any new messages from client

    packetSize = Udp.parsePacket();
    if (packetSize)
    {
        len = Udp.read(in_buffer, IN_BUFFER_SIZE);

        if (len > 0)
            in_buffer[len] = 0;

#ifdef DEBUG
        Serial.printf("Packet size: %d\nPacket: %s\n", len, in_buffer);
#endif
    }
}