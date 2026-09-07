package com.vodiymdf.app;

import android.Manifest;
import android.content.Intent;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PermissionState;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "MicRecorder",
    permissions = {
        @Permission(strings = { Manifest.permission.RECORD_AUDIO }, alias = "microphone")
    }
)
public class MicRecorderPlugin extends Plugin {

    @PluginMethod
    public void start(PluginCall call) {
        try {
            if (getPermissionState("microphone") != PermissionState.GRANTED) {
                requestPermissionForAlias("microphone", call, "startPermCallback");
                return;
            }
            startRecordingService(call);
        } catch (Exception e) {
            call.reject("start() xatosi: " + e.getMessage(), e);
        }
    }

    @PermissionCallback
    private void startPermCallback(PluginCall call) {
        try {
            if (getPermissionState("microphone") == PermissionState.GRANTED) {
                startRecordingService(call);
            } else {
                call.reject("Mikrofonga ruxsat berilmadi");
            }
        } catch (Exception e) {
            call.reject("permCallback xatosi: " + e.getMessage(), e);
        }
    }

    private void startRecordingService(PluginCall call) {
        try {
            Intent intent = new Intent(getContext(), MicRecordingService.class);
            intent.setAction("START");
            getContext().startForegroundService(intent);
            JSObject ret = new JSObject();
            ret.put("started", true);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Xizmatni ishga tushirib bo'lmadi: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            Intent intent = new Intent(getContext(), MicRecordingService.class);
            intent.setAction("STOP");
            getContext().startService(intent);
            JSObject ret = new JSObject();
            ret.put("filePath", MicRecordingService.lastFilePath);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("stop() xatosi: " + e.getMessage(), e);
        }
    }
}
