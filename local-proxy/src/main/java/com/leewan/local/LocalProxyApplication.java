package com.leewan.local;


import com.leewan.framework.util.BufferUtils;
import com.leewan.framework.util.IOUtils;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * :程序入口
 * @author Administrator
 *
 */
public class LocalProxyApplication {

    static Logger logger = LoggerFactory.getLogger(LocalProxyApplication.class);

    static CountDownLatch latch;
    
    static SocketChannel controllChannel;
    static Selector selector;

    public static void main(String[] args) throws ParseException {
        initOptions();
        parseArgs(args);

        //该socket为控制通道
        initControlChannel();
        
        //内外网内容交互
        ChannelInteraction channelInteraction = new ChannelInteraction();
        //提交新的交互通道
        NewPairChannleManager channleManager = new NewPairChannleManager(channelInteraction);
        
        channelInteraction.start();
        channleManager.start();
        
        while(true) {
        	try {
        		int select = selector.select();
        		if(select > 0) {
        			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        			while(iterator.hasNext()) {
        				SelectionKey key = iterator.next();
        				if(key.isReadable()) {
        					SocketChannel channel = (SocketChannel) key.channel();
        					ByteBuffer buf = ByteBuffer.allocate(4);
        					channel.read(buf);
        					buf.flip();
        					//添加到队列中，由channleManager线程执行具体的任务提交
        					channleManager.addChannelId(buf.getInt());
        				}
        				
        				if(key.isConnectable()) {
        					controllChannel.finishConnect();
        					key.interestOps(SelectionKey.OP_WRITE);
        				}
        				
        				if(key.isWritable()) {
        					//发送标志信息，表明自己是控制通道
        					controllChannel.write(BufferUtils.controlChannelId());
        					key.interestOps(SelectionKey.OP_READ);
        				}
        				iterator.remove();
        			}
        		}
			} catch (Exception e) {
				e.printStackTrace();
				initControlChannel();
			}
        	
        }
    }
    
    public static void initControlChannel() {
    	boolean initSuccess = false;
    	while(!initSuccess) {
        	try {
            	
            	controllChannel = SocketChannel.open();
            	controllChannel.configureBlocking(false);
            	
            	selector = Selector.open();
            	//先注册事件，再连接，顺序不能变
            	controllChannel.register(selector, SelectionKey.OP_CONNECT);
            	controllChannel.connect(new InetSocketAddress(host_remote, port_remote));
            	initSuccess = true;
    		} catch (Exception e) {
    			e.printStackTrace();
    			IOUtils.close(controllChannel);
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
