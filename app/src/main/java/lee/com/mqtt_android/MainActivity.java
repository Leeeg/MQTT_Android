package lee.com.mqtt_android;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lee.com.mqtt_android.main.MQTTService;
import lee.com.mqtt_android.main.MqttFragment;
import lee.com.mqtt_android.model.Container;


public class MainActivity extends AppCompatActivity implements MqttFragment.OnFragmentInteractionListener {

    public static final String FRAGMENT_TAG_MQTT = "fragment_mqtt";

    FragmentManager fragmentManager;
    MqttFragment mqttFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        init();
    }

    private void init() {
        //初始化view
        fragmentManager = getFragmentManager();
        mqttFragment = (MqttFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG_MQTT);
        if (mqttFragment == null) {
            mqttFragment = MqttFragment.newInstance("", "");
            fragmentManager.beginTransaction().add(R.id.fragment_container, mqttFragment, FRAGMENT_TAG_MQTT).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void startCall(String toId) {
        Intent mqttIntent = new Intent(MainActivity.this, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_STARTCALL, true);
        mqttIntent.putExtra(Container.MQTT_TOID, toId);
        startService(mqttIntent);
    }

    @Override
    public void stopCall() {
        Intent mqttIntent = new Intent(MainActivity.this, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_STOPCALL, true);
        startService(mqttIntent);
    }

    @Override
    public void onMqttSub(String topic) {
        Intent mqttIntent = new Intent(MainActivity.this, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_KEY, topic);
        mqttIntent.putExtra(Container.MQTT_ISTOPIC, true);
        startService(mqttIntent);
    }

}
