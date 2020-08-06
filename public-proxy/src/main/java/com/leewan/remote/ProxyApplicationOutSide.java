package com.leewan.remote;



import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ProxyApplicationOutSide extends Thread {

    public static Socket outsideSocket;

    @Override
    public void run() {
        try {
        	ServerSocketChannel server = ServerSocketChannel.open();
        	server.bind(new InetSocketAddress(OutSideProxyApplication.port_for_outside));
        	server.configureBlocking(false);
        	Selector selector = Selector.open();
        	server.register(selector, SelectionKey.OP_ACCEPT);
        	System.out.println(selector);
            while (true){
            	int select = selector.select();
            	if(select > 0) {
            		Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            		while(iterator.hasNext()) {
            			SelectionKey key = iterator.next();
            			if(key.isAcceptable()) {
            				SocketChannelManager.handleOutSideAccept(key);
            			}
            			if(key.isReadable()) {
            				SocketChannelManager.handleChannelReadContent((SocketChannel)key.channel());
            			}
            			iterator.remove();
            		}
            	}
            }
        }catch (Exception e){
        	e.printStackTrace();
        }
    }
    
    

}
