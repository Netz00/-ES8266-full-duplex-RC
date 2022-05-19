package com.fesb.carduino;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpServerThread extends Thread {

    int serverPort;
    DatagramSocket socket;
    String message;
    boolean running;

    public UdpServerThread(int serverPort) {
        super();
        this.serverPort = serverPort;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {

        running = true;

        try {
            Log.i("UdpServerThread", "Starting UDP Server");
            socket = new DatagramSocket(serverPort);

            Log.i("UdpServerThread", "UDP Server is running");
            Log.e("UdpServerThread", "UDP Server is running");

            while (running) {
                byte[] buf = new byte[31];
                String datagram;
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow

                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();


                datagram = new String(buf, 0, 31);
                String[] tokens = datagram.split("\0");
                message = tokens[0];

                Log.e("UdpServerThread", "Request from: " + address + ":" + port + " message: " + message + "\n");
            }

            Log.e("UdpServerThread", "UDP Server ended");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
                Log.e("UdpServerThread", "socket.close()");
            }
        }
    }
}
