/***
 *
 * Responsible for establishing listening socket,
 * listening for UDP datagrams from client
 * and storing them into in_buffer
 *
 * INPUT - UDP datagrams from client
 * OUTPUT - in_buffer
 *
 * */

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#define localUdpPort 4210 // port ESP8266 will be listening
#define IN_BUFFER_SIZE 255

// Acess point credentials
const char *ssid = "1234";
const char *password = "qwertz1234";

WiFiUDP Udp;

int packetSize;
int len;

// Establishing listening socket
void init_communictaion();

// Checking for inbound messages and storing them inside in_buffer
void read_data(char *in_buffer);