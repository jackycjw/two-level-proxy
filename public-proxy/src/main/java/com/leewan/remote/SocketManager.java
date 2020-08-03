package com.leewan.remote;



import com.leewan.framework.util.MathUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketManager {
    public static final int CONTROL_FLAG = Integer.MAX_VALUE;

    public static AtomicInteger sequence = new AtomicInteger();

    public static Socket controlSocket;

    public static Map<Integer, SocketPair> sockets = new ConcurrentHashMap<Integer, SocketPair>();

    public static void handleOutSideSocket(Socket socket){
        int pair = sequence.incrementAndGet();
        if(controlSocket != null && controlSocket.isConnected()){
            try {
                SocketPair socketPair = SocketPair.getNewInstance(socket);
                sockets.put(pair, socketPair);
                controlSocket.getOutputStream().write(MathUtil.getBytes(pair));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleInSideSocket(Socket socket){
        new ManagerThread(socket).start();
    }


}

class SocketPair {
    Socket outside;
    Socket inside;

    static SocketPair getNewInstance(Socket outside){
        SocketPair pair = new SocketPair();
        pair.outside = outside;
        return pair;
    }

    void start() throws IOException {
        new StreamRunner(this.outside.getInputStream(), this.inside.getOutputStream()).start();
        new StreamRunner(this.inside.getInputStream(), this.outside.getOutputStream()).start();
    }
}

class ManagerThread extends Thread {
    Socket socket;
    public ManagerThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try{
            InputStream inputStream = this.socket.getInputStream();
            byte[] bs = new byte[4];
            inputStream.read(bs);
            int flag = MathUtil.getInt(bs);
            if(flag == SocketManager.CONTROL_FLAG){
                SocketManager.controlSocket = this.socket;
            }else{
                SocketPair socketPair = SocketManager.sockets.get(flag);
                if(socketPair != null){
                    socketPair.inside = this.socket;
                    socketPair.start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}