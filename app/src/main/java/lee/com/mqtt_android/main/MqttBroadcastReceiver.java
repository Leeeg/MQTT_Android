package lee.com.mqtt_android.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MqttBroadcastReceiver extends BroadcastReceiver {

    private static final String SIM_READY = "com.android.when.simCard.ready";
    private static final String MQTT_TOPICS = "com.android.mqtt.topics";

    private static final String MQTT_KEY = "topics";
    private static final String MQTT_ISTOPIC = "isTopics";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent mqttIntent = new Intent(context, MQTTService.class);
        if (action.equals(SIM_READY)){
            Log.d("Lee_log_MQTT", "the simCard is readly and go to start MqttService !");
        }else if (action.equals(MQTT_TOPICS)){
            Log.d("Lee_log_MQTT", "receive the topicsMeaasge and send to MqttService !");
            mqttIntent.putExtra(MQTT_KEY, intent.getStringExtra(MQTT_KEY));
            mqttIntent.putExtra(MQTT_ISTOPIC, true);
        }
        context.startService(mqttIntent);
    }
}