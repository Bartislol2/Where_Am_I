package pl.gawryszewski.am_projekt;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.google.android.gms.location.Priority;



public class SettingsFragment extends Fragment {
    public interface onSettingsChangeListener{
        void onSettingsChange(Bundle settings);
    }

    private Switch sw_updates, sw_gps;
    private TextView tv_sensors, tv_updates;
    private onSettingsChangeListener settingsChangeListener;
    public void setOnSettingsChangeListener(onSettingsChangeListener listener)
    {
        this.settingsChangeListener = listener;
    }


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(Bundle fragmentState) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBundle("fragmentState", fragmentState);
        fragment.setArguments(args);
        return fragment;
    }
    private void sendDataToActivity(Bundle data)
    {
        if(settingsChangeListener!=null){
            settingsChangeListener.onSettingsChange(data);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(@Nullable AccessToken oldToken, @Nullable AccessToken currentToken) {
                if(currentToken==null){
                    LoginManager.getInstance().logOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        tv_sensors = view.findViewById(R.id.val_sensor);
        tv_updates = view.findViewById(R.id.val_updates);
        Bundle fragmentState = getArguments().getBundle("fragmentState");
        if(fragmentState!=null)
        {
            tv_sensors.setText(fragmentState.getString("sensors"));
            tv_updates.setText(fragmentState.getString("updates"));
        }
        sw_gps = view.findViewById(R.id.sw_sensor);
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sw_gps.isChecked())
                {
                    Bundle data = new Bundle();
                    data.putInt("priority", Priority.PRIORITY_HIGH_ACCURACY);
                    sendDataToActivity(data);
                    tv_sensors.setText("Using GPS sensors");
                }
                else
                {
                    Bundle data = new Bundle();
                    data.putInt("priority", Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                    sendDataToActivity(data);
                    tv_sensors.setText("Cell Towers + Wifi");
                }
            }
        });
        sw_updates = view.findViewById(R.id.sw_location);
        sw_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sw_updates.isChecked())
                {
                    Bundle data = new Bundle();
                    data.putBoolean("updates", true);
                    sendDataToActivity(data);
                    tv_updates.setText("On");
                }
                else
                {
                    Bundle data = new Bundle();
                    data.putBoolean("updates", false);
                    sendDataToActivity(data);
                    tv_updates.setText("Off");
                }
            }
        });
        return view;
    }
    private void saveFragmentState()
    {
        Bundle fragmentState = new Bundle();
        fragmentState.putString("updates", tv_updates.getText().toString());
        fragmentState.putString("sensors", tv_sensors.getText().toString());
        getArguments().putBundle("fragmentState", fragmentState);
    }
    @Override
    public void onPause()
    {
        super.onPause();
        saveFragmentState();
    }

}