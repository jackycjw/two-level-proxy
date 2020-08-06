package com.leewan.remote;



import java.io.IOException;
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
            while (true){
            	int select = selector.select();
            	if(select > 0) {
            		Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            		while(iterator.hasNext()) {
            			SelectionKey key = iterator.next();
            			if(key.isAcceptable()) {
            				handleAccept(key);
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
    
    private void handleAccept(SelectionKey key) {
    	ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			SocketChannel channel = server.accept();
			channel.configureBlocking(false);
			channel.register(key.selector(), SelectionKey.OP_READ);
			SocketChannelManager.notifyInSideConnect(channel);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    

}
