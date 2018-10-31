package lee.com.mqtt_android.model;

/**
 * CreateDate：18-10-31 on 上午10:36
 * Describe:
 * Coder: lee
 */
public class StartMessage {
    private int type;
    private String userId;
    private String targetId;

    private int number;
    private int online;
    private int isCall;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getIsCall() {
        return isCall;
    }

    public void setIsCall(int isCall) {
        this.isCall = isCall;
    }
}
