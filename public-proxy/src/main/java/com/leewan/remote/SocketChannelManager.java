package com.leewan.remote;



import com.leewan.framework.util.IOUtils;
import com.leewan.framework.util.MathUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketChannelManager {
    public static final int CONTROL_FLAG = Integer.MAX_VALUE;
    
    public static int BUF_SIZE = 1024;
    
    public static Map<SocketChannel, Object> insideChannels = new ConcurrentHashMap<SocketChannel, Object>();

    public static AtomicInteger sequence = new AtomicInteger();

    public static SocketChannel controlSocket;

    public static Map<Integer, SocketChannel> pair2Channel = new ConcurrentHashMap<Integer, SocketChannel>();
    private static Map<SocketChannel, SocketChannel> channelPairs = new ConcurrentHashMap<SocketChannel, SocketChannel>();

    public static void handleOutSideAccept(SelectionKey key) {
    	ServerSocketChannel server = (ServerSocketChannel) key.channel();
    	int pair = sequence.incrementAndGet();
    	try {
			SocketChannel channel = server.accept();
			if(controlSocket != null && controlSocket.isConnected()) {
				pair2Channel.put(pair, channel);
				ByteBuffer allocate = ByteBuffer.allocate(4);
				allocate.putInt(pair);
				allocate.flip();
				controlSocket.write(allocate);
			}
			channel.configureBlocking(false);
			System.out.println(key.selector());
			channel.register(key.selector(), SelectionKey.OP_READ);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void handleChannelReadContent(SocketChannel channel) throws IOException {
    	ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
    	while(channel.read(buf) > 0) {
    		buf.flip();
    		SocketChannel channelPair = channelPairs.get(channel);
    		channelPair.write(buf);
    		buf.flip();
    	}
    }
    
    public static void matchedPairs(int pair, SocketChannel channel) {
    	if(pair == CONTROL_FLAG) {
    		controlSocket = channel;
    	}else {
    		SocketChannel socketChannel = pair2Channel.get(pair);
        	channelPairs.put(socketChannel, channel);
        	channelPairs.put(channel, socketChannel);
        	pair2Channel.remove(pair);
    	}
    }
    
    public static boolean hasChannle(SocketChannel channel) {
    	return channelPairs.containsKey(channel);
    }

    
    public static void close(SocketChannel channel) {
    	SocketChannel channelPair = channelPairs.get(channel);
    	channelPairs.remove(channel);
    	channelPairs.remove(channelPair);
    	IOUtils.close(channel);
    	IOUtils.close(channelPair);
    }


}