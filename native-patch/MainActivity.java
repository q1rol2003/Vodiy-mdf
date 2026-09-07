package com.vodiymdf.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            try {
                java.io.File dir = getExternalFilesDir(null);
                java.io.File logFile = new java.io.File(dir, "crash_log.txt");
                java.io.FileWriter fw = new java.io.FileWriter(logFile, true);
                fw.write("\n===== " + new java.util.Date() + " =====\n");
                java.io.StringWriter sw = new java.io.StringWriter();
                ex.printStackTrace(new java.io.PrintWriter(sw));
                fw.write(sw.toString());
                fw.close();
            } catch (Exception e) { e.printStackTrace(); }
            System.exit(1);
        });

        registerPlugin(MicRecorderPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
