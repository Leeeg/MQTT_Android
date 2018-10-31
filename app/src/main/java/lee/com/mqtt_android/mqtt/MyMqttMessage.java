package lee.com.mqtt_android.mqtt;

/**
 * CreateDate：18-10-30 on 下午5:36
 * Describe:
 * Coder: lee
 */
public class MyMqttMessage {

    private String type;
    private String typeNo;
    private byte[] notes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeNo() {
        return typeNo;
    }

    public void setTypeNo(String typeNo) {
        this.typeNo = typeNo;
    }

    public byte[] getNotes() {
        return notes;
    }

    public void setNotes(byte[] notes) {
        this.notes = notes;
    }



}
