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
    
    //隧道号序号生成
    public static AtomicInteger sequence = new AtomicInteger();
    
    //内网控制通道
    public static SocketChannel controlSocket;

    //用于内外网channel之间映射
    public static Map<Integer, SocketChannel> pair2Channel = new ConcurrentHashMap<Integer, SocketChannel>();
    private static Map<SocketChannel, SocketChannel> channelPairs = new ConcurrentHashMap<SocketChannel, SocketChannel>();

    /**
     * :通知内网通往发起连接
     * @param channel
     */
    public static void notifyInSideConnect(SocketChannel channel) {
    	int pair = sequence.incrementAndGet();
    	try {
			if(controlSocket != null && controlSocket.isConnected()) {
				pair2Channel.put(pair, channel);
				ByteBuffer allocate = ByteBuffer.allocate(4);
				allocate.putInt(pair);
				allocate.flip();
				controlSocket.write(allocate);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    
    /**
     * :将通道读取的内容发送到映射通道去
     * @param channel
     * @throws IOException
     */
    public static void handleChannelReadContent(SocketChannel channel) throws IOException {
    	ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
    	while(channel.read(buf) > 0) {
    		buf.flip();
    		SocketChannel channelPair = channelPairs.get(channel);
    		channelPair.write(buf);
    		buf.flip();
    	}
    }
    
    /**
     * :通道映射
     * @param pair
     * @param channel
     */
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
    
    /**
     * :判断内存中是否已存在该通道
     * @param channel
     * @return
     */
    public static boolean hasChannle(SocketChannel channel) {
    	return channelPairs.containsKey(channel);
    }

    /**
     * :关闭通道，并关闭其映射的通道
     * @param channel
     */
    public static void close(SocketChannel channel) {
    	SocketChannel channelPair = channelPairs.get(channel);
    	channelPairs.remove(channel);
    	channelPairs.remove(channelPair);
    	IOUtils.close(channel);
    	IOUtils.close(channelPair);
    }


}