package lee.com.mqtt_android.model;

import android.os.Parcel;
import android.os.Parcelable;

import lee.com.mqtt_android.util.DataUtil;

/**
 * CreateDate：18-11-6 on 下午4:01
 * Describe:
 * Coder: lee
 */
public class TestMessage implements Parcelable {

    private int type = 1;
    private int userId;
    private String targetId = "1020180405090000701";
    private String callId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.userId);
        dest.writeString(this.targetId);
        dest.writeString(this.callId);
    }

    public TestMessage() {
        callId = DataUtil.getCallId();
    }

    protected TestMessage(Parcel in) {
        this.type = in.readInt();
        this.userId = in.readInt();
        this.targetId = in.readString();
        this.callId = in.readString();
    }

    public static final Parcelable.Creator<TestMessage> CREATOR = new Parcelable.Creator<TestMessage>() {
        @Override
        public TestMessage createFromParcel(Parcel source) {
            return new TestMessage(source);
        }

        @Override
        public TestMessage[] newArray(int size) {
            return new TestMessage[size];
        }
    };
}
