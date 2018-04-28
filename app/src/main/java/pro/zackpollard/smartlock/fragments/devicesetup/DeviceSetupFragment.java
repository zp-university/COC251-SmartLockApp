package pro.zackpollard.smartlock.fragments.devicesetup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import pro.zackpollard.smartlock.MainActivity;
import pro.zackpollard.smartlock.R;
import pro.zackpollard.smartlock.retrofit.RetrofitInstance;
import pro.zackpollard.smartlock.retrofit.models.Device;
import pro.zackpollard.smartlock.retrofit.models.DeviceUuid;
import pro.zackpollard.smartlock.retrofit.models.SetupDetails;
import pro.zackpollard.smartlock.retrofit.models.SetupStatus;
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
    private BroadcastReceiver wifiChangeReceiver;

    private List<ScanResult> wifiList;
    private WifiManager wifi;
    private int netCount=0;
    private int currentNetworkId;
    private int newNetworkId;

    DeviceUuid deviceUuid;

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
        wifiChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.d("DeviceSetupFragment", "WifiChangeReceiver called");
                if(((NetworkInfo) intent.getParcelableExtra((WifiManager.EXTRA_NETWORK_INFO))).isConnected()) {
                    mainActivity.unregisterReceiver(wifiChangeReceiver);
                    Observable.timer(5, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new MinimalDisposableObserver<Long>() {
                                @Override
                                public void onNext(Long aLong) {
                                    retrofitInstance.userAPI()
                                            .userAddDevice(deviceUuid)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new MinimalDisposableObserver<Device>() {
                                                @Override
                                                public void onNext (Device device){
                                                    Log.d("DeviceSetupFragment", "onNext called " + device);
                                                }

                                                @Override
                                                public void onError (Throwable e){
                                                    Log.e("DeviceSetupFragment", "onError: Error", e);
                                                }

                                                @Override
                                                public void onComplete () {
                                                    Log.e("DeviceSetupFragment", "onComplete: Complete");
                                                }
                                            });
                                }
                            });
                }
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void updateResultsList() {
        wifiList = wifi.getScanResults();
        for(ScanResult scanResult : new ArrayList<>(wifiList)) {
            if(scanResult.SSID.startsWith("SMARTLOCK-")) {
                try {
                    mainActivity.unregisterReceiver(wifiScanReceiver);
                } catch(IllegalArgumentException e) {
                }

                lockSsidText.setText(scanResult.SSID);

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.SSID = "\"" + scanResult.SSID + "\"";

                currentNetworkId = wifi.getConnectionInfo().getNetworkId();

                wifi.disconnect();
                newNetworkId = wifi.addNetwork(wifiConfig);
                wifi.enableNetwork(newNetworkId, true);

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wifiDetailsGroup.setVisibility(View.VISIBLE);
                    }
                });
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

            mainActivity.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("DeviceSetupFragment", "onPause: Called");
        subscriptions.clear();
        try {
            mainActivity.unregisterReceiver(wifiScanReceiver);
        } catch(IllegalArgumentException e) {
        }
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
                    mainActivity.showLoadingBar();
                    SetupDetails setupDetails = new SetupDetails();
                    setupDetails.setWifiSsid(ssidEditText.getText().toString());
                    setupDetails.setWifiPassword(passwordEditText.getText().toString());
                    subscriptions.add(retrofitInstance.setupAPI().sendSetupDetails(setupDetails)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new MinimalDisposableObserver<DeviceUuid>() {
                                @Override
                                public void onNext(DeviceUuid uuid) {
                                    deviceUuid = uuid;
                                }

                                @Override
                                public void onError(Throwable e) {
                                    super.onError(e);
                                    Log.e("SETUP", "Error", e);
                                    subscriptions.clear();
                                    mainActivity.hideLoadingBar();
                                }
                            }));
                    subscriptions.add(retrofitInstance.setupAPI().getSetupStatus()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                                @Override
                                public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                                    return objectObservable.delay(1, TimeUnit.SECONDS);
                                }
                            })
                            .takeUntil(new Predicate<SetupStatus>() {
                                @Override
                                public boolean test(SetupStatus setupStatus) throws Exception {
                                    if (setupStatus.getStatus().equals("failed") || setupStatus.getStatus().equals("completed")) {
                                        return true;
                                    }
                                    return false;
                                }
                            })
                            .subscribeWith(new MinimalDisposableObserver<SetupStatus>() {
                                @Override
                                public void onNext(SetupStatus setupStatus) {
                                    if(setupStatus.getStatus().equals("failed")) {
                                        mainActivity.hideLoadingBar();
                                        Snackbar.make(view, "Setup failed, the provided details were incorrect!", Snackbar.LENGTH_LONG).show();
                                        subscriptions.clear();
                                    } else if(setupStatus.getStatus().equals("completed")) {
                                        mainActivity.hideLoadingBar();
                                        wifi.removeNetwork(newNetworkId);
                                        wifi.disconnect();
                                        mainActivity.registerReceiver(wifiChangeReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                                        wifi.enableNetwork(currentNetworkId, true);
                                    }
                                }
                            })
                    );
                }

                break;
            }
        }
    }
}