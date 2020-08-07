package com.leewan.framework.util;

import java.nio.ByteBuffer;

public class BufferUtils {

	public static ByteBuffer controlChannelId() {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(Integer.MAX_VALUE);
		buf.flip();
		return buf;
	}
}
