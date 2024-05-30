package suhun.kim.util;

import java.io.File;
import java.util.TimerTask;

public class APIUtil extends TimerTask {

    @Override
    public void run() {
        String filePath = "test.bat";
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists() && fileToDelete.delete()) {
            System.out.println("O");
        } else {
            System.out.println("X");
        }
    }
}

