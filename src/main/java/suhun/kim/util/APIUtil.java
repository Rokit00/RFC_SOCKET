package suhun.kim.util;

import java.io.IOException;
import java.util.TimerTask;

public class APIUtil extends TimerTask {

    @Override
    public void run() {
        try {
            String filePath = "test.bat";
            Runtime.getRuntime().exec(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

