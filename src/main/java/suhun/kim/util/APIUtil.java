package suhun.kim.util;

import java.io.IOException;
import java.time.LocalDateTime;

public class APIUtil extends Thread {
    public void run() {
        while (true) {
            LocalDateTime targetDateTime = LocalDateTime.of(2024, 5, 30, 18, 14, 0);
            LocalDateTime currentDateTime = LocalDateTime.now();
            if (currentDateTime.isEqual(targetDateTime)) {
                System.out.println("POKOKO");
                try {
                    String filePath = "test.bat";
                    Runtime.getRuntime().exec(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 1초 마다 스레드 잠시 멈춤
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

