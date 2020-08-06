package com.leewan.framework.util;

import java.nio.ByteBuffer;

public class Test {

	public static void main(String[] args) {
		ByteBuffer buf = ByteBuffer.allocate(10);
		buf.flip();
	}
}
