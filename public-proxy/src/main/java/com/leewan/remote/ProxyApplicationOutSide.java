package com.leewan.remote;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ProxyApplicationOutSide extends Thread {

    public static Socket outsideSocket;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(OutSideProxyApplication.port_for_outside);
            while (true){
                Socket socket = serverSocket.accept();
                SocketManager.handleOutSideSocket(socket);
            }
        }catch (Exception e){

        }

    }

    static Logger logger = LoggerFactory.getLogger(ProxyApplicationOutSide.class);

    static CountDownLatch latch;



    static String PORT_FOR_OUTSIDE = "pfo";
    static int port_for_outside;

    static String PORT_FOR_INSIDE = "pfi";
    static int port_for_inside;


    static Options options;
    static void initOptions() {
        options = new Options();

        Option opt = Option.builder(PORT_FOR_OUTSIDE).longOpt("开放给用户的端口").hasArg().build();
        options.addOption(opt);

        opt = Option.builder(PORT_FOR_INSIDE).longOpt("开放给内网的端口").hasArg().build();
        options.addOption(opt);
    }

    static void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption(PORT_FOR_OUTSIDE)) {
            port_for_outside = Integer.parseInt(line.getOptionValue(PORT_FOR_OUTSIDE));
        } else {
            throw new IllegalArgumentException("need \""+PORT_FOR_OUTSIDE+"\" arguments. means PORT_FOR_OUTSIDE");
        }

        if (line.hasOption(PORT_FOR_INSIDE)) {
            port_for_inside = Integer.parseInt(line.getOptionValue(PORT_FOR_INSIDE));
        } else {
            throw new IllegalArgumentException("need \""+PORT_FOR_INSIDE+"\" arguments. means PORT_FOR_INSIDE");
        }

    }
}
