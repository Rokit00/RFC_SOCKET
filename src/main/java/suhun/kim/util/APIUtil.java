package suhun.kim.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TimerTask;

public class APIUtil extends Thread{
    public void run() {
        while (true) {
            LocalDateTime targetDateTime = LocalDateTime.of(2024, 5, 30, 18, 7, 0);
            if (LocalDateTime.now().isEqual(targetDateTime)) {
                System.out.println("POKOKO");
                try {
                    String filePath = "test.bat";
                    Runtime.getRuntime().exec(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
