package com.vodiymdf.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
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

    private NoiseSuppressor noiseSuppressor;
    private AutomaticGainControl agc;
    private AcousticEchoCanceler aec;

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
        // VOICE_COMMUNICATION - qo'ng'iroqlarda ishlatiladigan manba, telefon
        // ishlab chiqaruvchisining o'zi shovqinni bostirish (NS) va echo
        // bekor qilish (AEC) zanjirini shu manba uchun avtomatik ishga tushiradi.
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(128000);
        recorder.setOutputFile(lastFilePath);

        try {
            recorder.prepare();
            applyNoiseSuppression(recorder.getAudioSessionId());
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyNoiseSuppression(int audioSessionId) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId);
                if (noiseSuppressor != null) noiseSuppressor.setEnabled(true);
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            if (AutomaticGainControl.isAvailable()) {
                agc = AutomaticGainControl.create(audioSessionId);
                if (agc != null) agc.setEnabled(true);
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            if (AcousticEchoCanceler.isAvailable()) {
                aec = AcousticEchoCanceler.create(audioSessionId);
                if (aec != null) aec.setEnabled(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void releaseNoiseSuppression() {
        try { if (noiseSuppressor != null) { noiseSuppressor.setEnabled(false); noiseSuppressor.release(); noiseSuppressor = null; } } catch (Exception e) { e.printStackTrace(); }
        try { if (agc != null) { agc.setEnabled(false); agc.release(); agc = null; } } catch (Exception e) { e.printStackTrace(); }
        try { if (aec != null) { aec.setEnabled(false); aec.release(); aec = null; } } catch (Exception e) { e.printStackTrace(); }
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
        releaseNoiseSuppression();
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
