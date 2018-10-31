package lee.com.mqtt_android.util;

public class VoiceUtil {
    /**
     * 从音频数据中获取当前发送语音的用户ID
     *
     * @param req 语音数据
     * @return ID字符串，可能为null
     */

    public  static String getUserIdFromVoice(byte[] req) {
        if (req.length >= 40) {
            // 16
            return getUserId(req, 23);
        }
        return null;
    }

    /**
     * 从音频数据中获取目标ID(语音要发送给谁)
     *
     * @param req
     * @return
     */
    public  static String getTargetIdFromVoice(byte[] req) {
        if (req.length >= 40) {
            return getUserId(req, 31);
        }
        return null;
    }

    public  static String getUserIdFromHeart(byte[] req) {
        if (req.length == 24) {
            return getUserId(req, 15);
        }
        return null;
    }

    /**
     * 从语音数据鉴别是否是群聊
     */
    public  static boolean isTeamFromVoice(byte[] req) {
        if (req.length >= 40) {
            return req[32] == 2 || req[32] == 1;
        }
        return false;
    }

    public  static String getUserId(byte[] req, int endIndex) {
        long userId = req[endIndex--];
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (req[endIndex--] & 0x000000FF);
        return Long.toString(userId);
//        return String.valueOf(userId);
    }

    public  static void setOnline(byte[] req, int online, int groupNumber) {
        if (req.length >= 39) {
            req[36] = (byte) (online & 0x000000FF);
            req[37] = (byte) ((online >> 8) & 0x000000FF);
            req[38] = (byte) (groupNumber & 0x000000FF);
            req[39] = (byte) ((groupNumber >> 8) & 0x000000FF);
        }
    }

    public  static int getSequenceNumberFromVoice(byte[] req) {
        int sequenceNumber = 0x0;
        sequenceNumber = req[2] & 0x000000FF;
        sequenceNumber <<= 8;
        sequenceNumber |= req[3] & 0x000000FF;
        return sequenceNumber;
    }

    /**
     * 语音ID，一次通话过程语音ID不变
     */
    public  static int getVoiceId(byte[] req) {
        int endIndex = 8;
        int voiceId = req[endIndex++];
        voiceId <<= 8;
        voiceId |= (req[endIndex++] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex++] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex] & 0x000000FF);
        return voiceId;
    }

    public  static int getVoiceIdnew(byte[] req) {
        int endIndex = 7;
        int voiceId = req[endIndex--];
        voiceId <<= 8;
        voiceId |= (req[endIndex--] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex--] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex] & 0x000000FF);
        return voiceId;
    }
    public  static int getVoiceIdindex(byte[] req) {
        int endIndex = 3;
        int voiceId = req[endIndex--];
        voiceId <<= 8;
        voiceId |= (req[endIndex--] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex--] & 0x000000FF);
        voiceId <<= 8;
        voiceId |= (req[endIndex] & 0x000000FF);
        return voiceId;
    }
    public  static long getUserIdByLocationTimeMessage(byte[] res) {
        int endIndex = 15;
        long userId = res[endIndex--];
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex--] & 0x000000FF);
        userId <<= 8;
        userId |= (res[endIndex] & 0x000000FF);
        return userId;
    }
    public  static void setIp(byte[] req, String ip) {
        String[] ips = ip.split("\\.");
        if (ips.length != 4) {
            throw new RuntimeException();
        }
        req[1] = -1;
        int index = 2;
        req[index++] = (byte) Integer.valueOf(ips[0]).intValue();
        req[index++] = (byte) Integer.valueOf(ips[1]).intValue();
        req[index++] = (byte) Integer.valueOf(ips[2]).intValue();
        req[index++] = (byte) Integer.valueOf(ips[3]).intValue();
        req[index] = -1;
    }

    public  static  int getDataLength(byte[] data) {
        if (data.length < 40) {
            return 0;
        }
        int len;
        len = data[37];
        len <<= 8;
        len |= (data[36] & 0x000000FF);
        return len;
    }

}
