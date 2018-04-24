package pro.zackpollard.smartlock.fragments.devicescan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import pro.zackpollard.smartlock.MainActivity;
import pro.zackpollard.smartlock.R;
import pro.zackpollard.smartlock.retrofit.RetrofitInstance;
import pro.zackpollard.smartlock.retrofit.models.SetupDetails;
import pro.zackpollard.smartlock.retrofit.models.Token;
import pro.zackpollard.smartlock.utils.MinimalDisposableObserver;

/**
 * Created by Zack on 11/9/2017.
 */

public class DeviceSetupFragment extends Fragment implements View.OnClickListener {

    private CompositeDisposable subscriptions = new CompositeDisposable();
    private RetrofitInstance retrofitInstance;

    private MainActivity mainActivity;

    private TextView lockSsidText;
    private EditText ssidEditText;
    private EditText passwordEditText;
    private Button setupButton;
    private Group wifiDetailsGroup;

    private BroadcastReceiver wifiScanReceiver;
    private int wifiConfigId;

    private List<ScanResult> wifiList;
    private WifiManager wifi;
    int netCount=0;

    public static DeviceSetupFragment newInstance() {
        DeviceSetupFragment fragment = new DeviceSetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateResultsList();
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void updateResultsList() {
        wifiList = wifi.getScanResults();
        for(ScanResult scanResult : new ArrayList<>(wifiList)) {
            if(scanResult.SSID.startsWith("SMARTLOCK-")) {
                try {
                    getActivity().unregisterReceiver(wifiScanReceiver);
                } catch(IllegalArgumentException e) {
                }

                lockSsidText.setText(scanResult.SSID);

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.SSID = "\"" + scanResult.SSID + "\"";

                wifi.disconnect();
                wifiConfigId = wifi.addNetwork(wifiConfig);
                wifi.enableNetwork(wifiConfigId, true);

                wifiDetailsGroup.setVisibility(View.VISIBLE);
            }
        }
        netCount = wifiList.size();
        Log.d("Wifi", "Total Wifi Network" + netCount);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_setup, container, false);
        mainActivity = (MainActivity) getActivity();
        retrofitInstance = new RetrofitInstance(view.getContext());
        initViews(view);
        registerListeners();
        return view;
    }

    public void initViews(View view) {
        lockSsidText = view.findViewById(R.id.lock_ssid_text);

        ssidEditText = view.findViewById(R.id.ssid_edit);
        passwordEditText = view.findViewById(R.id.password_edit);

        setupButton = view.findViewById(R.id.setup_button);

        wifiDetailsGroup = view.findViewById(R.id.wifiDetailsGroup);
    }

    public void registerListeners() {
        setupButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateResultsList();

        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            getActivity().registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.clear();
        getActivity().unregisterReceiver(wifiScanReceiver);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.setup_button: {
                if(ssidEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "SSID must be entered to complete setup!", Snackbar.LENGTH_LONG).show();
                } else if(passwordEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Password must be entered to complete setup!", Snackbar.LENGTH_LONG).show();
                } else {
                    SetupDetails setupDetails = new SetupDetails();
                    setupDetails.setWifiSsid(ssidEditText.getText().toString());
                    setupDetails.setWifiPassword(passwordEditText.getText().toString());
                    setupDetails.setJwtToken("Hehe, this is a JWT, it can't see, because it isn't me!");
                    //TODO: Deal with JWT business
                    retrofitInstance.setupAPI().sendSetupDetails(setupDetails)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new MinimalDisposableObserver<Token>() {
                                @Override
                                public void onNext(Token token) {
                                    super.onNext(token);
                                    Snackbar.make(view, "Completed!", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    super.onError(e);
                                    Log.e("SETUP", "Error", e);
                                }
                            });
                }

                break;
            }
        }
    }
}