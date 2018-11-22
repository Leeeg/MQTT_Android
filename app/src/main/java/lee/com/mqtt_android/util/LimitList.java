package lee.com.mqtt_android.util;

import java.util.LinkedList;

/**
 * CreateDate：18-11-1 on 下午5:27
 * Describe:
 * Coder: lee
 */
public class LimitList extends LinkedList<Integer> {

    private static final int SIZE = 99;

    public static LimitList getInstance() {
        return LimitList.LimitListHolder.instance;
    }

    private static class LimitListHolder {
        private static final LimitList instance = new LimitList();
    }

    public LimitList() {
        super();
    }


    public boolean add(int id) {
        for (Integer callId : getInstance()) {
            if (id == callId) return false;
        }

        if (size() > SIZE)
            removeLast();

        return super.add(id);
    }

}
