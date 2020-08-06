package com.leewan.remote;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leewan.framework.util.IOUtils;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ProxyApplicationInSide extends Thread {

    public static Socket controlSocket;
    
    static int BUF_SIZE = 1024;

    @Override
    public void run() {
        try {
        	ServerSocketChannel server = ServerSocketChannel.open();
        	Selector selector = Selector.open();
        	server.configureBlocking(false);
        	server.bind(new InetSocketAddress(OutSideProxyApplication.port_for_inside));
        	server.register(selector, SelectionKey.OP_ACCEPT);
        	
        	while(true) {
        		int select = selector.select();
        		if(select > 0) {
        			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            		while(iterator.hasNext()) {
            			SelectionKey key = iterator.next();
            			if(key.isAcceptable()) {
            				handleAccept(key);
            			}
            			if(key.isReadable()) {
            				handleRead(key);
            			}
            			iterator.remove();
            		}
        		}
        	}
        }catch (Exception e){
        	e.printStackTrace();

        }
    }
    
    private void handleAccept(SelectionKey key) {
    	ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			SocketChannel channel = server.accept();
			channel.configureBlocking(false);
			channel.register(key.selector(), SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    static Map<SocketChannel, Object> channels = new HashMap<SocketChannel, Object>();
    
    private void handleRead(SelectionKey key) {
    	SocketChannel channel = (SocketChannel) key.channel();
    	
    	try {
    		
    		if(SocketChannelManager.hasChannle(channel)) {
        		ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
        		SocketChannelManager.handleChannelReadContent(channel);
        	}else {
        		//第一次读
        		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        		channel.read(buffer);
        		buffer.flip();
    			int pair = buffer.getInt();
    			SocketChannelManager.matchedPairs(pair, channel);
        	}
		} catch (IOException e) {
			SocketChannelManager.close(channel);
			key.cancel();
			e.printStackTrace();
		}
    	
    	
    }




}
