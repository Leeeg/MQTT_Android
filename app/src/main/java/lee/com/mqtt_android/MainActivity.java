package lee.com.mqtt_android;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import lee.com.mqtt_android.mqtt.MQTTManager;
import lee.com.mqtt_android.mqtt.MQTTService;
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


        Class c = null;
        try {
            c = Class.forName("lee.com.mqtt_android.mqtt.MQTTManager");
            Constructor con = c.getConstructor();
            Object obj = con.newInstance();

            Field element = c.getDeclaredField("userName");
            element.setAccessible(true);
            System.out.println("---------");
            System.out.println(element.get(obj));
            element.set(obj, "Test");
            System.out.println(element.get(obj));
            System.out.println("---------");


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }



}
