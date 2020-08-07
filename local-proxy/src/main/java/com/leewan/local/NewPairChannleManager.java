package com.leewan.local;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * :新通道交互任务提交管理
 * @author Administrator
 *
 */
public class NewPairChannleManager extends Thread {

	private ChannelInteraction channelConnect;
	
	private BlockingQueue<Integer> channelIds = new LinkedBlockingDeque<Integer>();
	
	/**
	 * :新增通道
	 * @param channelId
	 */
	public void addChannelId(int channelId) {
		channelIds.offer(channelId);
	}
	
	
	
	public NewPairChannleManager(ChannelInteraction channelConnect) {
		super();
		this.channelConnect = channelConnect;
	}



	/**
	 * :1、从队列中拉取通道ID
	 * :2、构建内外网通道
	 * :3、将两个通道向交互进程中提交
	 */
	@Override
	public void run() {
		while(true) {
			Integer channelId = 0;
			try {
				channelId = channelIds.take();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				SocketChannel remote = SocketChannel.open();
				remote.connect(new InetSocketAddress(LocalProxyApplication.host_remote, LocalProxyApplication.port_remote));
		    	remote.finishConnect();
		    	
		    	//将通道ID发往外网代理 以表明自己身份
		    	ByteBuffer buf = ByteBuffer.allocate(4);
				buf.putInt(channelId);
				buf.flip();
		    	remote.write(buf);
		    	
		    	SocketChannel target = SocketChannel.open();
		    	target.connect(new InetSocketAddress(LocalProxyApplication.host_target, LocalProxyApplication.port_target));
		    	
		    	this.channelConnect.addChannelConnect(remote, target);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
