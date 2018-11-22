package lee.com.mqtt_android.model;

/**
 * CreateDate：18-10-30 on 下午3:21
 * Describe:
 * Coder: lee
 */
public class Container {

    public static final String MQTT_ISTOPIC = "isTopics";
    public static final String MQTT_KEY = "topics";
    public static final String MQTT_STARTCALL = "start_call";
    public static final String MQTT_STOPCALL = "stop_call";
    public static final String MQTT_TOID= "to_id";
    public static final String MQTT_TYPE= "call_type";


    public static final String OUT_TOPIC_PROXY_READY_CALL = "out-topic-proxy/ready-call/";
    public static final String OUT_TOPIC_ISSUE_READY_CALL = "out-topic-issue/ready-call/";
    public static final String OUT_TOPIC_ISSUE_START_CALL = "out-topic-issue/startTest-call/";
    public static final String OUT_TOPIC_PROXY_END_CALL = "out-topic-proxy/end-call/";
    public static final String OUT_TOPIC_ISSUE_END_CALL = "out-topic-issue/end-call/";

    public static final String IN_TOPIC_PROXY_READY_CALL = "in-topic-proxy/ready-call/";
    public static final String IN_TOPIC_ISSUE_READY_CALL = "in-topic-issue/ready-call/";
    public static final String IN_TOPIC_PROXY_START_CALL = "in-topic-proxy/startTest-call/";
    public static final String IN_TOPIC_PROXY_END_CALL = "in-topic-proxy/end-call/";
    public static final String IN_TOPIC_ISSUE_END_CALL = "in-topic-issue/end-call";

}
