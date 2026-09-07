package com.vodiymdf.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MicRecorderPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
