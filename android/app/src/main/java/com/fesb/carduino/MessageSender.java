package com.fesb.carduino;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MessageSender extends AsyncTask<String, Void, Void> {


    // Configuration required for communication
    public static InetAddress ip;
    public static int port;
    int msg_length;
    byte[] message;
    DatagramSocket s;
    DatagramPacket p;
    String udpStr;


    /**
     * Async task for sending messages
     *
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(String... voids) {

        if (this.ip != null) {
            this.udpStr = voids[0];

            this.msg_length = udpStr.length();
            this.message = udpStr.getBytes();

            DatagramSocket s = null;
            try {
                s = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            this.p = new DatagramPacket(message, msg_length, this.ip, this.port);
            try {
                s.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}