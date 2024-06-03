package suhun.kim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suhun.kim.jco.JCoServerConfig;
import suhun.kim.socket.SocketService;
import suhun.kim.socket.SocketServiceImpl;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        new JCoServerConfig();
        Thread thread = new SocketServiceImpl();
        thread.start();
//        Timer timer = new Timer();
//        LocalDateTime targetDateTime = LocalDateTime.now().plusSeconds(5);
//        timer.schedule(new APIUtil(), java.sql.Timestamp.valueOf(targetDateTime));
        SocketService socketService = new SocketServiceImpl();
        socketService.setServerSocket();
    }
}