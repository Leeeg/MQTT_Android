package lee.com.mqtt_android;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import lee.com.mqtt_android.model.StartMessage;
import lee.com.mqtt_android.mqtt.MQTTManager;
import lee.com.mqtt_android.mqtt.MyMQTTCallback;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MqttFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MqttFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MqttFragment extends Fragment implements MyMQTTCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button mqttStartBt, mqttStopBt, mqttSub, mqttStartCall, mqttStopCall;
    private EditText mqttTopicEt, mqttSendToEt;

    private RecyclerView recyclerView;
    private LogAdapter logAdapter;
    private List<String> logList = new ArrayList<>();

    private MQTTManager mqttManager;
    private String host = "tcp://192.168.0.25:61613";
    private String userName = "admin";
    private String passWord = "password";

    public MqttFragment() {
        // Required empty public constructor
    }

    public static MqttFragment newInstance(String param1, String param2) {
        MqttFragment fragment = new MqttFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        mqttStartBt = rootView.findViewById(R.id.bt_mqtt_start);
        mqttStartBt.setOnClickListener((v) -> {
            mqttStartBt.setEnabled(false);
            mqttStopBt.setEnabled(true);
            startMqttService();
        });
        mqttStopBt = rootView.findViewById(R.id.bt_mqtt_stop);
        mqttStopBt.setOnClickListener((v) -> {
            mqttStopBt.setEnabled(false);
            mqttStartBt.setEnabled(true);
            stopMqttService();
        });
        mqttTopicEt = rootView.findViewById(R.id.et_mqtt_topic);
        mqttSendToEt = rootView.findViewById(R.id.et_mqtt_send);
        mqttSub = rootView.findViewById(R.id.bt_mqtt_sub);
        mqttSub.setOnClickListener((v) -> {
            if (null != mqttTopicEt.getText() && !mqttTopicEt.getText().toString().isEmpty()) {
                mqttData(" --->  " + mqttTopicEt.getText().toString());
                subMqttTopic(mqttTopicEt.getText().toString());
            }
        });

        mqttStartCall = rootView.findViewById(R.id.bt_mqtt_start_call);
        mqttStartCall.setOnClickListener((v) -> {
            if (null != mqttSendToEt.getText() && !mqttSendToEt.getText().toString().isEmpty()) {
                mqttData(" --->  " + mqttSendToEt.getText().toString());
                startCall(0, Integer.valueOf(mqttSendToEt.getText().toString()));
            }
        });
        mqttStopCall = rootView.findViewById(R.id.bt_mqtt_stop_call);
        mqttStopCall.setOnClickListener((v) -> {
            stopCall();
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_log);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getInstance().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        logAdapter = new LogAdapter(logList);
        recyclerView.setAdapter(logAdapter);
        recyclerView.setItemAnimator(null);

        mqttManager = MQTTManager.getInstance()
                .init(MyApplication.getInstance().getApplicationContext());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void mqttData(String data) {
        logAdapter.addNewItem(data);
        recyclerView.scrollToPosition(logAdapter.getItemCount() - 1);
    }

    @Override
    public void onMQTTError(int code, String err) {
        Log.d("Lee_mqtt", "onMQTTError : code = " + code + "  err" + err);
    }

    @Override
    public void onStartCallResponse(StartMessage startMessage) {
        Log.d("Lee_mqtt", "onStartCallResponse : startMessage = " + startMessage);
    }


    @Override
    public void onReceiveCall() {
        Log.d("Lee_mqtt", "onReceiveCall");
    }

    @Override
    public void onEndCall() {
        Log.d("Lee_mqtt", "onEndCall");
    }

    @Override
    public void onMQTTResule(int type, boolean connect) {
        Log.d("Lee_mqtt", "onMQTTResule : type = " + type + "  connect" + connect);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void startMqttService() {
        mqttData("onBindService --- ");
        mqttManager.bindMQTTService(this);
    }

    private void stopMqttService() {
        mqttData("onUnbindService --- ");
        mqttManager.unBindMQTTService();
    }

    private void subMqttTopic(String topic) {
        mqttManager.subscribeTopic(Integer.valueOf(topic));
    }

    private void startCall(int type, int toId) {
        mqttManager.startCall(type, toId);
    }

    private void stopCall() {
        mqttManager.stopCall();
    }


}
