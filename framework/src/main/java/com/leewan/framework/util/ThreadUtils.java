package com.leewan.framework.util;

public class ThreadUtils {
	
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
