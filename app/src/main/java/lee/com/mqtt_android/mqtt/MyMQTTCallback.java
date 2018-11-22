package lee.com.mqtt_android.mqtt;

import lee.com.mqtt_android.model.StartMessage;

/**
 * CreateDate：18-10-30 on 下午1:38
 * Describe:
 * Coder: lee
 */
public interface MyMQTTCallback {

    void mqttData(String data);

    /**
     * @param code (-1:连接断开; 2:网络无效)
     * @param err
     */
    void onMQTTError(int code, String err);

    /**
     * 发起呼叫时返回给起呼方的数据
     * @param startMessage
     */
    void onStartCallResponse(StartMessage startMessage);

    /**
     * 收到被呼
     */
    void onReceiveCall();

    /**
     * 收到结束呼叫
     */
    void onEndCall();

    /**
     * @param type (1:MQTT发起连接回调  2：MQTT注册主题回调  3:MQTT发送起呼消息回调  4:MQTT发送终止消息回调)
     * @param connect
     */
    void onMQTTResule(int type, boolean connect);

}
