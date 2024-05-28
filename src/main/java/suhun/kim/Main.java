package suhun.kim;

import suhun.kim.jco.JCoServerConfig;
import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;

public class Main {
    public static void main(String[] args) {
        new JCoServerConfig();
        Thread thread = new SocketServiceImpl();
        thread.start();
        SocketService socketService = new SocketServiceImpl();
        socketService.setServerSocketByKRW();
    }
}