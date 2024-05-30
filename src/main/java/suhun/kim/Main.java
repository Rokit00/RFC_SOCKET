package suhun.kim;

import suhun.kim.jco.JCoServerConfig;
import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;
import suhun.kim.util.APIUtil;

public class Main {
    public static void main(String[] args) {
        new JCoServerConfig();
        Thread thread = new SocketServiceImpl();
        thread.start();
        System.out.println("a");
        SocketService socketService = new SocketServiceImpl();
        socketService.setServerSocketByKRW();
        System.out.println("b");
        Thread a = new APIUtil();
        a.start();
    }
}