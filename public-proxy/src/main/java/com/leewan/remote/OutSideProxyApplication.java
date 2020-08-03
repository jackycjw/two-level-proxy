package com.leewan.remote;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;

public class OutSideProxyApplication {

    static Logger logger = LoggerFactory.getLogger(OutSideProxyApplication.class);

    static CountDownLatch latch;

    public static void main(String[] args) throws IOException, ParseException {
        initOptions();
        parseArgs(args);

        new ProxyApplicationInSide().start();
        new ProxyApplicationOutSide().start();
    }


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
