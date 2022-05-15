#include "communication.h"

// Acess point credentials
// const char *ssid = "1234";
// const char *password = "qwertz1234";

const char *ssid = "5BC0D9";
const char *password = "vsmv37rx2c";

WiFiUDP Udp;

IPAddress remoteIp;

int packetSize;
int len;

void communication_init()
{

#ifdef DEBUG
    Serial.printf("### Communication - init #\n");
#endif

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

void communication_read_data(char *in_buffer, int bufer_size)
{
#ifdef DEBUG
    Serial.printf("### Communication - reading data \n");
#endif

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

void communication_send_data(char *out_buffer)
{

#ifdef DEBUG
    Serial.printf("### Communication - sending data \n");
#endif

    // Send packet to remote UDP server
    Udp.beginPacket(remoteIp, remoteUdpPort);
    Udp.write(out_buffer);
    Udp.endPacket();
}

// Read signal strengths
long communication_read_rssi()
{

#ifdef DEBUG
    Serial.printf("### Communication - reading RSSI \n");
#endif
    return WiFi.RSSI();
}