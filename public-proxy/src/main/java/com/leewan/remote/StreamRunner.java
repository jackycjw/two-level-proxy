package com.leewan.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamRunner extends Thread {

    InputStream in;
    OutputStream out;

    public StreamRunner(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        super.run();
        byte[] bs = new byte[1024];
        int len = -1;
        while (true){
            try {
                if ((len = in.read(bs)) > -1) {
                    out.write(bs, 0, len);
                    out.flush();
                };
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

        }

    }
}
