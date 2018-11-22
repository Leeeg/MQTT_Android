package lee.com.mqtt_android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CreateDate：18-10-31 on 上午10:36
 * Describe:
 * Coder: lee
 */
public class StartMessage implements Parcelable {
    private int type;
    private int userId;
    private int targetId;
    private int callId;

    private int number;
    private int online;
    private int isCall;

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

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
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

    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
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
        dest.writeInt(this.targetId);
        dest.writeInt(this.callId);
        dest.writeInt(this.number);
        dest.writeInt(this.online);
        dest.writeInt(this.isCall);
    }

    public StartMessage() {
    }

    protected StartMessage(Parcel in) {
        this.type = in.readInt();
        this.userId = in.readInt();
        this.targetId = in.readInt();
        this.callId = in.readInt();
        this.number = in.readInt();
        this.online = in.readInt();
        this.isCall = in.readInt();
    }

    public static final Parcelable.Creator<StartMessage> CREATOR = new Parcelable.Creator<StartMessage>() {
        @Override
        public StartMessage createFromParcel(Parcel source) {
            return new StartMessage(source);
        }

        @Override
        public StartMessage[] newArray(int size) {
            return new StartMessage[size];
        }
    };
}
