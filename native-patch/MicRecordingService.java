package com.vodiymdf.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MicRecordingService extends Service {

    private static final String CHANNEL_ID = "mic_recording_channel";
    private static final int NOTIF_ID = 1001;
    private MediaRecorder recorder;
    public static String lastFilePath = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if ("START".equals(action)) {
            startRecording();
        } else if ("STOP".equals(action)) {
            stopRecording();
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void startRecording() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ovoz yozilmoqda")
                .setContentText("Vodiy MDF fonda ovoz yozib bormoqda")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(NOTIF_ID, builder.build());
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".m4a";
        File dir = getExternalFilesDir(null);
        File outFile = new File(dir, fileName);
        lastFilePath = outFile.getAbsolutePath();

        recorder = new MediaRecorder();
        // VOICE_COMMUNICATION - qo'ng'iroqlarda ishlatiladigan manba.
        // Telefon ishlab chiqaruvchisining o'zi (Samsung va h.k.) shovqinni
        // bostirish va ovozni tozalash zanjirini shu manba uchun tizim
        // darajasida avtomatik ulaydi - qo'shimcha kod yozish shart emas.
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(128000);
        recorder.setOutputFile(lastFilePath);

        try {
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Ovoz yozish", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
