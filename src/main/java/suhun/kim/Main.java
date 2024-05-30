package suhun.kim;

import suhun.kim.jco.JCoServerConfig;
import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;
import suhun.kim.util.APIUtil;

import java.time.LocalDateTime;
import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        new JCoServerConfig();
        Thread thread = new SocketServiceImpl();
        thread.start();

        Timer timer = new Timer();
        LocalDateTime targetDateTime = LocalDateTime.now().plusSeconds(5);
        timer.schedule(new APIUtil(), java.sql.Timestamp.valueOf(targetDateTime));

        System.out.println("a");
        SocketService socketService = new SocketServiceImpl();
        socketService.setServerSocketByKRW();
        System.out.println("b");
    }
}