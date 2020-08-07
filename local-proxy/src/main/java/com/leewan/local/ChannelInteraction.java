package com.leewan.local;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.leewan.framework.util.IOUtils;

/**
 * :通道交互
 * @author Administrator
 *
 */
public class ChannelInteraction extends Thread {
	private Map<SocketChannel, SocketChannel> channelMap = new ConcurrentHashMap<SocketChannel, SocketChannel>();
	private Selector selector;
	private volatile boolean isAddingChannel = false;
	private volatile ReentrantLock lock = new ReentrantLock();
	
	public void addChannelConnect(SocketChannel remote, SocketChannel target) {
		//保存两个通道的映射关系
		channelMap.put(remote, target);
		channelMap.put(target, remote);
		
		try {
			//先锁死
			lock.lock();
			//设置状态为"正在添加。。"
			isAddingChannel = true;
			//唤醒阻塞的"select"行为
			Selector wakeup = this.selector.wakeup();
			//将两个通道注册至选择器
			remote.configureBlocking(false);
			target.configureBlocking(false);
			
			remote.register(wakeup, SelectionKey.OP_READ);
			target.register(wakeup, SelectionKey.OP_READ);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//通道添加完成后，解锁，并将状态设置为正常
			lock.unlock();
			isAddingChannel = true;
		}
		
		
	}
	
	public void run() {
		try {
			selector = Selector.open();
			while(true) {
				//判断是正在提交新的交互通道
				if(isAddingChannel) {
					//阻塞等待至新交互通道提交完成
					lock.lock();
					lock.unlock();
				}
				int select = selector.select();
				if(select > 0) {
					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while(iterator.hasNext()) {
						SelectionKey key = iterator.next();
						try {
							if(key.isReadable()) {
								handleReadContent(key);
							}
						} catch (IOException e) {
							key.cancel();
							IOUtils.close(key.channel());
						}
						iterator.remove();
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * :内容交互
	 * @param key
	 * @throws IOException
	 */
	private void handleReadContent(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		SocketChannel dest = channelMap.get(channel);
		ByteBuffer buf = ByteBuffer.allocateDirect(1024);
		while(channel.read(buf) > 0) {
			buf.flip();
			dest.write(buf);
			buf.flip();
		}
	}
}
