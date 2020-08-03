package com.leewan.local;

import com.leewan.framework.util.MathUtil;

import java.io.InputStream;
import java.net.Socket;
import java.util.Random;

public class ProxyRunner extends Thread {

    byte[] pair;

    public ProxyRunner(byte[] pair) {
        this.pair = pair;
    }

    @Override
    public void run() {
        try {
            Socket remote = new Socket(LocalProxyApplication.host_remote, LocalProxyApplication.port_remote);
            remote.getOutputStream().write(pair);
            Socket target = new Socket(LocalProxyApplication.host_target, LocalProxyApplication.port_target);

            new StreamRunner(remote.getInputStream(), target.getOutputStream()).start();
            new StreamRunner(target.getInputStream(), remote.getOutputStream()).start();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
        }
    }

}
