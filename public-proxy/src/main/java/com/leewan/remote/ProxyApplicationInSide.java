package com.leewan.remote;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ProxyApplicationInSide extends Thread {

    public static Socket controlSocket;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(OutSideProxyApplication.port_for_inside);

            while (true){
                Socket socket = serverSocket.accept();
                SocketManager.handleInSideSocket(socket);
            }
        }catch (Exception e){

        }
    }




}
