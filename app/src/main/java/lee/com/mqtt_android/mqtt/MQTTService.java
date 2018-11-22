package lee.com.mqtt_android.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.CountDownTimer;
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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lee.com.mqtt_android.model.Container;
import lee.com.mqtt_android.model.StartMessage;
import lee.com.mqtt_android.model.TestMessage;
import lee.com.mqtt_android.util.DataUtil;
import lee.com.mqtt_android.util.GsonInner;
import lee.com.mqtt_android.util.LimitList;
import lee.com.mqtt_android.util.MLog;

/**
 * CreateDate：18-9-15 on 上午10:33
 * Describe:
 * Coder: lee
 */
public class MQTTService extends Service {

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private boolean isConnected;
    private String topicIn,     //收到起呼
            topicOut,           //收到发起呼叫的返回
            topicStartCall,
            topicEndCallIn,
            topicEndCallOut;
    private int userId, toId, type;
    private String[] topics = new String[5];

    private MyMQTTCallback myMQTTCallback;
    private Handler handler;

    private LimitList limitList;

    final ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(100));

    public void setMqttCallback(MyMQTTCallback myMQTTCallback) {
        this.myMQTTCallback = myMQTTCallback;
    }

    public MQTTService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        MLog.D("MqttService : onCreate");
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (null != myMQTTCallback)
                    myMQTTCallback.mqttData(System.currentTimeMillis() + ": " + msg.obj);
            }
        };

        init();
        limitList = LimitList.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MLog.D("MqttService : onStartCommand");
        if (intent.getBooleanExtra(Container.MQTT_ISTOPIC, false)) {
            userId = intent.getIntExtra(Container.MQTT_KEY, 0);

            topicOut = Container.OUT_TOPIC_PROXY_READY_CALL + userId;
            topicIn = Container.OUT_TOPIC_ISSUE_READY_CALL + userId;
            topicStartCall = Container.OUT_TOPIC_ISSUE_START_CALL + userId;
            topicEndCallIn = Container.OUT_TOPIC_PROXY_END_CALL + userId;
            topicEndCallOut = Container.OUT_TOPIC_ISSUE_END_CALL + userId;
            topics[0] = topicOut;
            topics[1] = topicIn;
            topics[2] = topicStartCall;
            topics[3] = topicEndCallIn;
            topics[4] = topicEndCallOut;
            subTopics();
        } else if (intent.getBooleanExtra(Container.MQTT_STARTCALL, false)) {
            toId = intent.getIntExtra(Container.MQTT_TOID, 0);
            type = intent.getIntExtra(Container.MQTT_TYPE, 0);
            startCall();
        } else if (intent.getBooleanExtra(Container.MQTT_STOPCALL, false)) {
            endCall();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 注册topic
     */
    private void subTopics() {
        MLog.D("MqttService ： subTopics isConnected = " + isConnected);
        if (isConnected) {
            MLog.D("go to subTopics ");
            try {
                // 订阅myTopic话题
                int[] qos = {1, 1, 1, 1, 1};
                client.subscribe(topics, qos, null, mqttSubListener);
//                client.subscribe(topicOut, 1, null, mqttSubListener);
//                client.subscribe(topicStartCall, 1, null, mqttSubListener);
//                client.subscribe(topicEndCallIn, 1, null, mqttSubListener);
//                client.subscribe(topicEndCallOut, 1, null, mqttSubListener);
            } catch (MqttException e) {
                MLog.E("subTopics ERROR : " + e);
            }
        }
    }

    private void init() {
        MLog.D("MqttService : init");
        sendBack("init MqttService");
        //每次注册服务的id不同
        String clientId = UUID.randomUUID().toString();
        // 服务器地址（协议+地址+端口号）
        Log.e("MQTT_lee", "host" + MQTTManager.getInstance().getHost());
        client = new MqttAndroidClient(this, MQTTManager.getInstance().getHost(), clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttMessageCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(MQTTManager.getInstance().getUserName());
        // 密码
        conOpt.setPassword(MQTTManager.getInstance().getPassWord().toCharArray());
        conOpt.setMaxInflight(1000);


        if (!isConnected) {
            doClientConnection();
        }


    }

    private void sendBack(String msg) {
        Message message = handler.obtainMessage();
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        MLog.D("MqttService : doClientConnection");
        sendBack("startTest to connect to MQTTService");
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                MLog.D("MqttService : startTest to connect ......  ");
                client.connect(conOpt, null, mqttConnectListener);
            } catch (MqttException e) {
                MLog.E("MqttService ： ERROR : " + e);
                e.printStackTrace();
            }
        }

    }

    /**
     * 发起呼叫
     */
    private void startCall() {

        MLog.D("startTest call : type = " + type + "   targetId = " + toId);
        StartMessage startMessage = new StartMessage();
        startMessage.setType(type);
        startMessage.setUserId(userId);
        startMessage.setTargetId(toId);
        startMessage.setCallId(new Random().nextInt(1000));
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(GsonInner.getInstance().toJson(startMessage).getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(Container.IN_TOPIC_PROXY_READY_CALL + userId);
            if (isConnected)
                client.publish(Container.IN_TOPIC_PROXY_READY_CALL + userId, mqttMessage, null, mqttPublishStartCallListener);
        } catch (MqttException e) {
            MLog.E("startCall ERROR : " + e);
            e.printStackTrace();
        }

    }


    private void startTestCall(int userId, TestMessage testMessage) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(GsonInner.getInstance().toJson(testMessage).getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(Container.IN_TOPIC_PROXY_READY_CALL + userId);
            if (isConnected) {
                client.publish(Container.IN_TOPIC_PROXY_READY_CALL + testMessage, mqttMessage, null, mqttPublishStartCallListener);
            }
        } catch (MqttException e) {
            MLog.E("startCall ERROR : " + e);
            e.printStackTrace();
        }
    }

//    /**
//     * 发送起呼头部
//     */
//    private void sendOutHead() {
//        MLog.D( "MqttService  sendOutHead");
//
//        byte[] bytes = {1, 2, 3, 4, 5};
//
//        MyMqttMessage myMqttMessage = new MyMqttMessage();
//        myMqttMessage.setType(type);
//        myMqttMessage.setTypeNo(toId);
//        myMqttMessage.setNotes(bytes);
//
//        String s = GsonInner.getInstance().toJson(myMqttMessage);
//
//        MqttMessage mqttMessage = new MqttMessage();
//        mqttMessage.setQos(1);
//        mqttMessage.setPayload(s.getBytes());
//        mqttMessage.setRetained(false);
//        try {
//            sendBack(Container.IN_TOPIC_PROXY_START_CALL + userId);
//            client.publish(Container.IN_TOPIC_PROXY_START_CALL + userId, mqttMessage, null, mqttPublishStartCallListener);
//        } catch (MqttException e) {
//            MLog.E( "sendOutHead ERROR : " + e);
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 发送终止
     */
    private void endCall() {
        MLog.D("MqttService ： endCall");
        StartMessage startMessage = new StartMessage();
        startMessage.setType(type);
        startMessage.setUserId(userId);
        startMessage.setTargetId(toId);

        String s = GsonInner.getInstance().toJson(startMessage);

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(s.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(Container.IN_TOPIC_PROXY_END_CALL + userId);
            if (isConnected)
                client.publish(Container.IN_TOPIC_PROXY_END_CALL + userId, mqttMessage, null, mqttPublishEndCallListener);
        } catch (MqttException e) {
            MLog.E("endCall ERROR : " + e);
            e.printStackTrace();
        }

    }

    /**
     * 收到起呼
     */
    private void onReceiveCall() {
        MLog.D("MqttService ： onReceiveCall");
        String content = "ok";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(content.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(Container.IN_TOPIC_ISSUE_READY_CALL + userId);
            if (isConnected)
                client.publish(Container.IN_TOPIC_ISSUE_READY_CALL + userId, mqttMessage);
            /**
             * 通知上层
             */
            if (null != myMQTTCallback)
                myMQTTCallback.onReceiveCall();
        } catch (MqttException e) {
            MLog.E("onReceiveCall ERROR : " + e);
            e.printStackTrace();
        }
    }

    /**
     * 收到终止
     */
    private void onEndCall() {
        MLog.D("MqttService ： onEndCall");
        String content = "ok";
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(content.getBytes());
        mqttMessage.setRetained(false);
        try {
            sendBack(Container.IN_TOPIC_ISSUE_END_CALL + userId);
            if (isConnected)
                client.publish(Container.IN_TOPIC_ISSUE_END_CALL + userId, mqttMessage);
            /**
             * 通知上层
             */
            if (null != myMQTTCallback)
                myMQTTCallback.onEndCall();
        } catch (MqttException e) {
            MLog.E("onEndCall ERROR : " + e);
            e.printStackTrace();
        }
    }

    /**
     * MQTT发送起呼回调
     */
    private IMqttActionListener mqttPublishStartCallListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            MLog.D("起呼消息发送成功 ");
            sendBack("mqtt publish success ");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(3, true);
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            if (null != arg1)
                arg1.printStackTrace();
            MLog.E("起呼消息发送失败");
            sendBack("mqtt publish failed ");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(3, false);
        }
    };

    /**
     * MQTT发送终止回调
     */
    private IMqttActionListener mqttPublishEndCallListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            MLog.D("终止消息发送成功 ");
            sendBack("mqtt publish success ");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(4, true);
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            MLog.E("终止消息发送成功");
            sendBack("mqtt publish failed ");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(4, false);
        }
    };

    /**
     * MQTT是否注册成功
     */
    private IMqttActionListener mqttSubListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            MLog.D("注册成功 ");
            sendBack("subscribe Mqtt topic success");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(2, true);
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            MLog.E("注册失败");
            sendBack("subscribe Mqtt topic failed");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(2, false);
        }
    };


    private static final int SLEEPTIME = 10;
    private boolean run;
    private CountDownTimer countDownTimer;

    public void startTest() {
        countDownTimer = new CountDownTimer(Integer.MAX_VALUE, 10_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                MLog.E("onTick -------------------------------- ");
                run = !run;
                if (run) {
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            while (run) {
                                TestMessage message = new TestMessage();
                                message.setUserId(3221);//1504
                                MLog.D("Send message1 : callId = " + message.getCallId());
                                startTestCall(message.getUserId(), message);
                                try {
                                    Thread.sleep(SLEEPTIME);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            while (run) {
                                TestMessage message = new TestMessage();
                                message.setUserId(3227);
                                MLog.D("Send message2 : callId = " + message.getCallId());
                                startTestCall(message.getUserId(), message);
                                try {
                                    Thread.sleep(SLEEPTIME);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            while (run) {

                                TestMessage message = new TestMessage();
                                message.setUserId(3225);
                                MLog.D("Send message3 : callId = " + message.getCallId());
                                startTestCall(message.getUserId(), message);
                                try {
                                    Thread.sleep(SLEEPTIME);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
//                    threadPoolExecutor.shutdown();
                }
            }

            @Override
            public void onFinish() {
            }
        };
        countDownTimer.start();

    }

    public void startTestE() {
        Random random = new Random();
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    TestMessage message = new TestMessage();
                    message.setUserId(3223);
                    message.setTargetId(DataUtil.teams[random.nextInt(DataUtil.teams.length)]);
                    MLog.D("Send message : setTargetId = " + message.getTargetId());
                    startTestCall(message.getUserId(), message);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * MQTT是否连接成功
     */
    private IMqttActionListener mqttConnectListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            MLog.D("连接成功 ");
            sendBack("connect to MQTTService success");
            isConnected = true;
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(1, isConnected);

//            startTest();
            startTestE();
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            MLog.E("连接失败   -------");
            sendBack("connect to MQTTService failed and try reConnect");
            isConnected = false;
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTResule(1, isConnected);
            if (!isConnected) {
//                doClientConnection();
            }
        }
    };

    /**
     * 返回起呼信息到上层
     *
     * @param bytes
     */
    private void setStartCallResponse(byte[] bytes) {

        MLog.D("MqttService ： setStartCallResponse");

        sendBack(new String(bytes));

        if (null != myMQTTCallback) {
            StartMessage startMessage = GsonInner.getInstance().fromJson(new String(bytes), StartMessage.class);
            MLog.D("setStartCallResponse : limitList.size() = " + limitList.size() + "  CallId = " + startMessage.getCallId());
            if (limitList.add(startMessage.getCallId()))
                myMQTTCallback.onStartCallResponse(startMessage);
        }

    }

    /**
     * MQTT监听并且接受消息
     */
    private MqttCallback mqttMessageCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) {

            String str2 = "收到消息： " + topic + ";qos: " + message.getQos() + ";   retained: " + message.isRetained();
            MLog.D(str2);

            sendBack(str2);

            if (topic.equals(topicOut)) {//发布起呼的返回
                MLog.D("receive the startCall back message ");
                setStartCallResponse(message.getPayload());
//                sendOutHead();
            } else if (topic.equals(topicIn)) {//收到呼叫
                MLog.D("receive the inCall message ");
                onReceiveCall();
            } else if (topic.equals(topicEndCallOut)) {//收到结束会话
                MLog.D("receive the endCall message ");
                onEndCall();
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            MLog.E("失去连接   -------");
            sendBack("lost connect");
            isConnected = false;
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTError(-1, "connection lost !");
            if (!isConnected) {
//                doClientConnection();
            }
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        MLog.D("MqttService ： isConnectIsNomarl : ");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            MLog.D("MQTT当前网络名称：" + name);
            sendBack("net type : " + name);
            return true;
        } else {
            MLog.E("MQTT 没有可用网络");
            sendBack("do not have Internet");
            if (null != myMQTTCallback)
                myMQTTCallback.onMQTTError(2, "Internet error ");
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

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        MLog.D("MqttService ： onDestroy ");
        sendBack("destroy MqttService");
        try {
            threadPoolExecutor.shutdownNow();
            client.unregisterResources();
            client.unsubscribe(topics);
            client.disconnect();
            client = null;
        } catch (MqttException e) {
            MLog.E("MqttService ： ERROR : " + e);
            e.printStackTrace();
        }
        super.onDestroy();
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