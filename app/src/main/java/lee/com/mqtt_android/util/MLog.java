package lee.com.mqtt_android.util;

import android.util.Log;

import lee.com.mqtt_android.mqtt.MQTTManager;

/**
 * CreateDate：18-10-31 on 下午2:46
 * Describe:
 * Coder: lee
 */
public class MLog {

    private static final String TAG = "MQTT_Android >>> :";

    public static void E(String msg) {
        if (MQTTManager.getInstance().isDebug())
            Log.e(TAG, msg);
    }

    public static void D(String msg) {
        if (MQTTManager.getInstance().isDebug())
            Log.d(TAG, msg);
    }

}
