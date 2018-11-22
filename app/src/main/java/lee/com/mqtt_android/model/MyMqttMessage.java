package lee.com.mqtt_android.model;

/**
 * CreateDate：18-10-30 on 下午5:36
 * Describe:
 * Coder: lee
 */
public class MyMqttMessage {

    private int type;
    private int typeNo;
    private byte[] notes;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTypeNo() {
        return typeNo;
    }

    public void setTypeNo(int typeNo) {
        this.typeNo = typeNo;
    }

    public byte[] getNotes() {
        return notes;
    }

    public void setNotes(byte[] notes) {
        this.notes = notes;
    }



}
