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

    private PluginCall pendingStartCall;

    @PluginMethod
    public void start(PluginCall call) {
        if (getPermissionState("microphone") != PermissionState.GRANTED) {
            pendingStartCall = call;
            requestPermissionForAlias("microphone", call, "startPermCallback");
            return;
        }
        startRecordingService(call);
    }

    @PermissionCallback
    private void startPermCallback(PluginCall call) {
        if (getPermissionState("microphone") == PermissionState.GRANTED) {
            startRecordingService(call);
        } else {
            call.reject("Mikrofonga ruxsat berilmadi");
        }
    }

    private void startRecordingService(PluginCall call) {
        Intent intent = new Intent(getContext(), MicRecordingService.class);
        intent.setAction("START");
        getContext().startForegroundService(intent);
        JSObject ret = new JSObject();
        ret.put("started", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Intent intent = new Intent(getContext(), MicRecordingService.class);
        intent.setAction("STOP");
        getContext().startService(intent);
        JSObject ret = new JSObject();
        ret.put("filePath", MicRecordingService.lastFilePath);
        call.resolve(ret);
    }
}
