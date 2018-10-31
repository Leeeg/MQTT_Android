package lee.com.mqtt_android.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import lee.com.mqtt_android.model.Container;
import lee.com.mqtt_android.model.MyMqttMessage;
import lee.com.mqtt_android.model.StartMessage;

/**
 * CreateDate：18-9-15 on 上午10:33
 * Describe:
 * Coder: lee
 * <p>
 * <p>
 * <p>
 * MQTT注册主题：
 * <p>
 * <p>
 * 1.起呼方 与 服务器 交互
 * <p>
 * 起呼方 --> 发布起呼信息 --> 服务器订阅信息：
 * topic：in-topic-proxy/ready-call/userId
 * 内容：json字符串：{"type": "user/team", "typeNo": userId/teamNo}
 * <p>
 * 服务器订阅后 --> 发布确认信息 --> 起呼方订阅信息：
 * topic：out-topic-proxy/ready-call/userId
 * 内容：返回原始内容
 * <p>
 * <p>
 * <p>
 * 2.服务器 与 被呼方 交互
 * <p>
 * 服务器订阅后 --> 发布起呼信息 --> 被呼方订阅信息：
 * topic：out-topic-issue/ready-call/userId
 * 内容：返回原始内容
 * <p>
 * 被呼方订阅后 --> 发布确认信息 --> 服务器订阅信息：
 * topic：in-topic-issue/ready-call/userId
 * 内容：返回原始内容
 * <p>
 * <p>
 * <p>
 * 其中，userId 和 teamNo 是 用户id 和 群组编号，user 和 team 是 被呼标记（单呼或群呼）
 */
public class MQTTService extends Service {
    public static final String TAG = "Lee_log_MQTT";

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private String host = "tcp://192.168.0.25:61613";
    private String userName = "admin";
    private String passWord = "password";
    //    private String host = "tcp://47.106.253.152:1883";
//    private String userName = "mosquitto";
//    private String passWord = "mosquitto";
    private String clientId;
    private boolean isConnected;
    private String subOutTopicRoot = "out-topic-proxy/ready-call/";
    private String subInTopicRoot = "out-topic-issue/ready-call/";
    private String subStartCallTopicRoot = "out-topic-issue/start-call/";
    private String subEndCallOutTopicRoot = "out-topic-proxy/end-call/";
    private String subEndCallInTopicRoot = "out-topic-issue/end-call/";

    private String publishInTopicRoot = "in-topic-proxy/ready-call/";
    private String publishOutTopicRoot = "in-topic-issue/ready-call/";
    private String publishStartCallTopicRoot = "in-topic-proxy/start-call/";
    private String publishEndCallOutTopicRoot = "in-topic-proxy/end-call/";
    private String publishEndCallInTopicRoot = "in-topic-issue/end-call";
    private String topicIn, topicOut, topicStartCall, topicEndCallIn, topicEndCallOut;
    private String userId, toId;

    private MyMqttCallback myMqttCallback;
    private Handler handler;

    public void setMqttCallback(MyMqttCallback myMqttCallback) {
        this.myMqttCallback = myMqttCallback;
    }

    public MQTTService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MqttService : onCreate");
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (null != myMqttCallback)
                    myMqttCallback.mqttData("" + msg.obj);
            }
        };

        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MqttService : onStartCommand");
        if (intent.getBooleanExtra(Container.MQTT_ISTOPIC, false)) {
            Log.i(TAG, "subTopics ");
            userId = intent.getStringExtra(Container.MQTT_KEY);
            topicOut = subOutTopicRoot + userId;
            topicIn = subInTopicRoot + userId;
            topicStartCall = subStartCallTopicRoot + userId;
            topicEndCallIn = subEndCallOutTopicRoot + userId;
            topicEndCallOut = subEndCallInTopicRoot + userId;
            subTopics();
        } else if (intent.getBooleanExtra(Container.MQTT_STARTCALL, false)) {
            toId = intent.getStringExtra(Container.MQTT_TOID);
            int type = intent.getIntExtra(Container.MQTT_TYPE, 0);
            Log.i(TAG, "start call : " + toId);
            startCall(type);
        } else if (intent.getBooleanExtra(Container.MQTT_STOPCALL, false)) {
            Log.i(TAG, "stop call");
            sendOutEnd();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 注册topic
     */
    private void subTopics() {
        if (isConnected) {
            Log.i(TAG, "subTopics ： topicOut = " + topicOut);
            Log.i(TAG, "subTopics ： topicIn = " + topicIn);
            try {
                // 订阅myTopic话题
                client.subscribe(topicIn, 1, null, iMqttSubListener);
                client.subscribe(topicOut, 1, null, iMqttSubListener);
                client.subscribe(topicStartCall, 1, null, iMqttSubListener);
                client.subscribe(topicEndCallIn, 1, null, iMqttSubListener);
                client.subscribe(topicEndCallOut, 1, null, iMqttSubListener);
            } catch (MqttException e) {
                Log.e(TAG, "subTopics ERROR : " + e);
            }
        }
    }

    private void init() {
        Log.d(TAG, "MqttService : init");
        sendBack("init MqttService");
        clientId = UUID.randomUUID().toString();
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());

        if (!isConnected) {
            doClientConnection();
        }

    }

    private void sendBack(String msg) {
        Message message = handler.obtainMessage();
        message.obj = msg;
        handler.sendMessage(message);
    }

    @Override
    public void onDestroy() {
        sendBack("destroy MqttService");
        try {
            client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, "MqttService ： ERROR : " + e);
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        sendBack("start to connect to MQTTService");
        Log.d(TAG, "MqttService : doClientConnection");
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                client.connect(conOpt, null, iMqttConnectListener);
            } catch (MqttException e) {
                Log.e(TAG, "MqttService ： ERROR : " + e);
                e.printStackTrace();
            }
        }

    }

    /**
     * 发起呼叫
     */
    private void startCall(int type) {
        StartMessage startMessage = new StartMessage();
        startMessage.setType(type);
        startMessage.setUserId(userId);
        startMessage.setTargetId(toId);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(GsonInner.getInstance().toJson(startMessage).getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(publishInTopicRoot + userId);
            client.publish(publishInTopicRoot + userId, mqttMessage, null, iMqttPublishListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送起呼头部
     */
    private void sendOutHead() {
        byte[] bytes = {1, 2, 3, 4, 5};

        MyMqttMessage myMqttMessage = new MyMqttMessage();
        myMqttMessage.setType("user");
        myMqttMessage.setTypeNo(toId);
        myMqttMessage.setNotes(bytes);

        String s = GsonInner.getInstance().toJson(myMqttMessage);

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(s.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(publishStartCallTopicRoot + userId);
            client.publish(publishStartCallTopicRoot + userId, mqttMessage, null, iMqttPublishListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送终止
     */
    private void sendOutEnd() {
        byte[] bytes = {6, 7, 8, 9, 10};

        MyMqttMessage myMqttMessage = new MyMqttMessage();
        myMqttMessage.setType("user");
        myMqttMessage.setTypeNo(toId);
        myMqttMessage.setNotes(bytes);

        String s = GsonInner.getInstance().toJson(myMqttMessage);

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(s.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(publishEndCallOutTopicRoot + userId);
            client.publish(publishEndCallOutTopicRoot + userId, mqttMessage, null, iMqttPublishListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     * 收到呼叫后
     */
    private void receiveCall() {
        String content = "ok";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(content.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(publishOutTopicRoot + userId);
            client.publish(publishOutTopicRoot + userId, mqttMessage, null, iMqttPublishListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到结束后
     */
    private void receiveEnd() {
        String content = "ok";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(content.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(publishEndCallInTopicRoot + userId);
            client.publish(publishEndCallInTopicRoot + userId, mqttMessage, null, iMqttPublishListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * MQTT消息发送成功
     */
    private IMqttActionListener iMqttPublishListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "消息发送成功 ");
            sendBack("mqtt publish success ");
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.e(TAG, "消息发送成功");
            sendBack("mqtt publish failed ");
        }
    };

    /**
     * MQTT是否注册成功
     */
    private IMqttActionListener iMqttSubListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            Log.i(TAG, "注册成功 ");
            sendBack("subscribe Mqtt topic success");
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            Log.e(TAG, "注册失败");
            sendBack("subscribe Mqtt topic failed");
        }
    };

    /**
     * MQTT是否连接成功
     */
    private IMqttActionListener iMqttConnectListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            sendBack("connect to MQTTService success");
            isConnected = true;
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.e(TAG, "连接失败   重连-------");
            sendBack("connect to MQTTService failed and try reConnect");
            isConnected = false;
            if (!isConnected) {
//                doClientConnection();
            }
        }
    };

    private void setStartCallResponse(byte[] bytes) {

        sendBack(new String(bytes));
        StartMessage startMessage = GsonInner.getInstance().fromJson(new String(bytes), StartMessage.class);

    }

    /**
     * MQTT监听并且接受消息
     */
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) {

            String str2 = "收到消息： " + topic + ";qos: " + message.getQos() + ";   retained: " + message.isRetained();
            Log.d(TAG, str2);

            sendBack(str2);

            if (topic.equals(topicOut)) {//发布起呼的返回
                Log.e(TAG, "do startRecord ");
                setStartCallResponse(message.getPayload());
                sendOutHead();
            } else if (topic.equals(topicIn)) {//收到呼叫
                receiveCall();
            } else if (topic.equals(topicEndCallOut)) {//收到结束会话
                receiveEnd();
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.e(TAG, "失去连接   重连-------");
            sendBack("lost connect");
            isConnected = false;
            if (!isConnected) {
//                doClientConnection();
            }
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        Log.d(TAG, "MqttService ： isConnectIsNomarl : ");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            sendBack("net type : " + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            sendBack("do not have Internet");
            return false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MqttBinder();
    }

    public class MqttBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }


    /**
     * 获取ip地址
     *
     * @param context
     * @return
     */
    private String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }


            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "" +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
