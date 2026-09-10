package com.vodiymdf.app;

import android.app.AlertDialog;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

        showCrashLogIfExists();
    }

    private void showCrashLogIfExists() {
        try {
            File logFile = new File(getExternalFilesDir(null), "crash_log.txt");
            if (logFile.exists()) {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();

                new AlertDialog.Builder(this)
                    .setTitle("Oxirgi xatolik")
                    .setMessage(sb.toString())
                    .setPositiveButton("Yopish", (d, w) -> logFile.delete())
                    .setCancelable(true)
                    .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
