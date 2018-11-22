package lee.com.mqtt_android.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Random;

import lee.com.mqtt_android.model.Container;

/**
 * CreateDate：18-10-31 on 上午11:16
 * Describe:
 * Coder: lee
 */
public class MQTTManager implements MQTTpersenter{

    private Context context;

    private MyServiceConnection connection;
    private MQTTService mqttService;
    private MyMQTTCallback myMQTTCallback;

//    private String host = new Random().nextBoolean()?"tcp://47.106.253.152:1883":"tcp://47.106.108.175:1883";
    private String host = "tcp://47.106.253.152:1883";
    private String userName = "mosquitto";
    private String passWord = "mosquitto";

    private boolean debug = true;

    public boolean isDebug() {
        return debug;
    }


    public MQTTManager debugAble(boolean debug) {
        this.debug = debug;
        return getInstance();
    }

    public String getHost() {
        return host;
    }

    public MQTTManager setHost(String host) {
        this.host = host;
        return getInstance();
    }

    public String getUserName() {
        return userName;
    }

    public MQTTManager setUserName(String userName) {
        this.userName = userName;
        return getInstance();
    }

    public String getPassWord() {
        return passWord;
    }

    public MQTTManager setPassWord(String passWord) {
        this.passWord = passWord;
        return getInstance();
    }

    public MQTTManager() {
    }

    public static MQTTManager getInstance(){
        return MQTTManagerHolder.instance;
    }

    private static class MQTTManagerHolder{
        private static final MQTTManager instance = new MQTTManager();
    }

    public MQTTManager init(Context context){
        this.context = context;
        return getInstance();
    }

    @Override
    public MQTTManager bindMQTTService(MyMQTTCallback myMQTTCallback) {
        onBindService(myMQTTCallback);
        return getInstance();
    }

    @Override
    public void unBindMQTTService() {
        onUnBindService();
    }

    @Override
    public void startCall(int type, int toId) {
        Intent mqttIntent = new Intent(context, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_STARTCALL, true);
        mqttIntent.putExtra(Container.MQTT_TYPE, type);
        mqttIntent.putExtra(Container.MQTT_TOID, toId);
        context.startService(mqttIntent);
    }

    @Override
    public void stopCall() {
        Intent mqttIntent = new Intent(context, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_STOPCALL, true);
        context.startService(mqttIntent);
    }

    @Override
    public void subscribeTopic(int topic) {
        Intent mqttIntent = new Intent(context, MQTTService.class);
        mqttIntent.putExtra(Container.MQTT_KEY, topic);
        mqttIntent.putExtra(Container.MQTT_ISTOPIC, true);
        context.startService(mqttIntent);
    }

    private void onBindService(MyMQTTCallback myMQTTCallback) {
        this.myMQTTCallback = myMQTTCallback;
        connection = new MyServiceConnection();
        Intent i = new Intent(context, MQTTService.class);
        context.bindService(i, connection, context.BIND_AUTO_CREATE);
    }

    private void onUnBindService() {
        context.unbindService(connection);
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MQTTService.MqttBinder) service).getService();
            mqttService.setMqttCallback(myMQTTCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mqttService.setMqttCallback(null);
            mqttService.stopSelf();
        }
    }

}
