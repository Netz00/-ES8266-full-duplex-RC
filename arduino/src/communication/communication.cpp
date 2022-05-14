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

    remoteIp = WiFi.localIP();

    // Opening UDP socket

    Udp.begin(localUdpPort);
    Udp.flush();

#ifdef DEBUG
    Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.localIP().toString().c_str(), localUdpPort);
#endif
}

void read_data(char *in_buffer, int bufer_size)
{
    // Checking if there any new messages from client
    packetSize = Udp.parsePacket();
    if (packetSize)
    {

#ifdef DEBUG
        Serial.print("Received packet of size ");
        Serial.println(packetSize);
        Serial.print("From ");
        Serial.print(remoteIp);
        Serial.print(", port ");
        Serial.println(Udp.remotePort());
#endif

        remoteIp = Udp.remoteIP();

        len = Udp.read(in_buffer, bufer_size);

        if (len > 0)
            in_buffer[len] = 0;

#ifdef DEBUG
        Serial.printf("Packet size: %d\nPacket: %s\n", len, in_buffer);
#endif
    }
}

void send_data(char *out_buffer)
{
    // Send packet to remote UDP server
    Udp.beginPacket(remoteIp, remoteUdpPort);
    Udp.write(out_buffer);
    Udp.endPacket();
}

// Read signal strengths
long read_rssi()
{
    return WiFi.RSSI();
}