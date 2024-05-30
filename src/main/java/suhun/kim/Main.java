package suhun.kim;

import suhun.kim.jco.JCoServerConfig;
import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;
import suhun.kim.util.APIUtil;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        new JCoServerConfig();
        LocalDateTime targetDateTime = LocalDateTime.of(2024, 5, 30, 18, 14, 0);
        System.out.println(LocalDateTime.now());
        System.out.println(targetDateTime);
        Thread thread = new SocketServiceImpl();
        thread.start();
        Thread a = new APIUtil();
        a.start();
        System.out.println("a");
        SocketService socketService = new SocketServiceImpl();
        socketService.setServerSocketByKRW();
        System.out.println("b");
    }
}