package lee.com.mqtt_android.main;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import lee.com.mqtt_android.MyApplication;
import lee.com.mqtt_android.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MqttFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MqttFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MqttFragment extends Fragment implements MyMqttCallback {
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
            if (null != mqttSendToEt.getText() && !mqttSendToEt.getText().toString().isEmpty())
                mqttData(" --->  " + mqttSendToEt.getText().toString());
                startCall(mqttSendToEt.getText().toString());
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
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        connection = new MyServiceConnection();

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void startCall(String toId);

        void stopCall();

        void onMqttSub(String topic);
    }

    private void startMqttService() {
//        if (null != mListener) mListener.startCall(0);
        mqttData("onBindService --- ");
        onBindService();
    }

    private void stopMqttService() {
//        if (null != mListener) mListener.startCall(-1);
        mqttData("onUnbindService --- ");
        onUnBindService();
    }

    private void subMqttTopic(String topic) {
        if (null != mListener) mListener.onMqttSub(topic);
    }

    private void startCall(String toId) {
        if (null != mListener) mListener.startCall(toId);
    }

    private void stopCall() {
        if (null != mListener) mListener.stopCall();
    }

    public void onBindService() {
        Intent i = new Intent(getActivity(), MQTTService.class);
        getActivity().bindService(i, connection, getActivity().BIND_AUTO_CREATE);
    }

    public void onUnBindService() {
        getActivity().unbindService(connection);
    }

    MyServiceConnection connection;
    MQTTService mqttService;

    public class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MQTTService.MqttBinder) service).getService();
            mqttService.setMqttCallback(MqttFragment.this);
            mqttData("onServiceUnConnected --- ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mqttService.setMqttCallback(null);
            mqttData("onServiceUnConnected --- ");
        }
    }

}
