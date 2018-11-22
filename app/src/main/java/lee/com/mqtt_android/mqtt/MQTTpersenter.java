package lee.com.mqtt_android.mqtt;

/**
 * CreateDate：18-10-31 on 上午11:31
 * Describe:
 * Coder: lee
 */
public interface MQTTpersenter {

    MQTTManager bindMQTTService(MyMQTTCallback myMQTTCallback);

    void unBindMQTTService();

    void startCall(int type, int targetId);

    void stopCall();

    void subscribeTopic(int topic);

}
