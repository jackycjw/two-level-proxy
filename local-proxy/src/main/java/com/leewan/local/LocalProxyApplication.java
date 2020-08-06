package com.leewan.local;


import com.leewan.framework.util.MathUtil;
import com.leewan.framework.util.ThreadUtils;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class LocalProxyApplication {

    static Logger logger = LoggerFactory.getLogger(LocalProxyApplication.class);

    static CountDownLatch latch;

    public static void main(String[] args) throws ParseException, InterruptedException, IOException {
        initOptions();
        parseArgs(args);

        //该socket为控制通道
        boolean flag = false;
        Socket remote = null;
        try {
        	remote = new Socket(LocalProxyApplication.host_remote, LocalProxyApplication.port_remote);
            remote.getOutputStream().write(MathUtil.getBytes(Integer.MAX_VALUE));
		} catch (Exception e) {
			e.printStackTrace();
		}
        

        while(true){
            try {
                InputStream inputStream = remote.getInputStream();
                byte[] bs  = new byte[4];
                inputStream.read(bs);
                System.out.println("接受新信号 "+MathUtil.getInt(bs));
                new ProxyRunner(bs).start();

            } catch (Exception e) {
            	try {
            		remote = new Socket(LocalProxyApplication.host_remote, LocalProxyApplication.port_remote);
                    remote.getOutputStream().write(MathUtil.getBytes(Integer.MAX_VALUE));
                    ThreadUtils.sleep(1000);
				} catch (Exception e2) {
				}
                
            }
        }

    }


    static String PORT_TARGET = "pt";
    static int port_target;

    static String HOST_TARGET = "ht";
    static String host_target;

    static String PORT_REMOTE = "pr";
    static int port_remote;

    static String HOST_REMOTE = "hr";
    static String host_remote;

    static Options options;
    static void initOptions() {
        options = new Options();
        Option help = new Option("help", "print this message");
        options.addOption(help);

        Option opt = Option.builder(PORT_TARGET).longOpt("代理端口").hasArg().build();
        options.addOption(opt);

        opt = Option.builder(HOST_TARGET).longOpt("代理地址").hasArg().build();
        options.addOption(opt);

        opt = Option.builder(PORT_REMOTE).longOpt("远程代理端口").hasArg().build();
        options.addOption(opt);

        opt = Option.builder(HOST_REMOTE).longOpt("远程代理地址").hasArg().build();
        options.addOption(opt);
    }

    static void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption(PORT_TARGET)) {
            port_target = Integer.parseInt(line.getOptionValue(PORT_TARGET));
        } else {
            throw new IllegalArgumentException("need \"pd\" arguments. means target port");
        }

        if (line.hasOption(HOST_TARGET)) {
            host_target = line.getOptionValue(HOST_TARGET);
        } else {
            logger.info("There are no parameters for target host, this will default use \"localhost\"!");
        }

        if (line.hasOption(PORT_REMOTE)) {
            port_remote = Integer.parseInt(line.getOptionValue(PORT_REMOTE));
        } else {
            throw new IllegalArgumentException("need \"pr\" arguments. means remote port");
        }

        if (line.hasOption(HOST_REMOTE)) {
            host_remote = line.getOptionValue(HOST_REMOTE);
        } else {
            throw new IllegalArgumentException("need \"hr\" arguments. means dest port");
        }
    }
}
