/***
 *
 *
 * INPUT - UDP datagrams from client
 * OUTPUT - in_buffer
 *
 * */

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#define localUdpPort 4210 // port ESP8266 will be listening


#define remoteUdpPort 4211 // port ESP8266 will be connecting to


// Acess point credentials
const char *ssid = "1234";
const char *password = "qwertz1234";

WiFiUDP Udp;

IPAddress remoteIp;

int packetSize;
int len;

// Establishing wifi connection and listening socket for read_data
void init_communictaion();

/***
 *
 * Checking for inbound messages and storing them inside in_buffer
 * INPUT - UDP datagrams from client
 * OUTPUT - in_buffer
 *
 * */
void read_data(char *in_buffer, int bufer_size);

/***
 *
 * Sending messages to remote UDP server and adding signal strength to every message
 * INPUT - out_buffer
 * OUTPUT - UDP datagrams from client
 *
 * */
void send_data(char *out_buffer);

long read_rssi();