package pro.zackpollard.smartlock.fragments.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pro.zackpollard.smartlock.MainActivity;
import pro.zackpollard.smartlock.R;
import pro.zackpollard.smartlock.retrofit.RetrofitInstance;
import pro.zackpollard.smartlock.retrofit.models.Device;
import pro.zackpollard.smartlock.retrofit.models.LockStatus;
import pro.zackpollard.smartlock.utils.MinimalDisposableObserver;

/**
 * Created by Zack on 11/9/2017.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    private CompositeDisposable subscriptions = new CompositeDisposable();
    private RetrofitInstance retrofitInstance;

    private MainActivity mainActivity;

    private ImageView lockImageView;

    private boolean lockLocked;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = (MainActivity) getActivity();
        retrofitInstance = new RetrofitInstance(view.getContext());
        initViews(view);
        registerListeners();
        return view;
    }

    public void initViews(View view) {
        lockImageView = view.findViewById(R.id.lock_image);
    }

    public void registerListeners() {
        lockImageView.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions.add(retrofitInstance.userAPI().getUserDevice()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(1, TimeUnit.SECONDS);
                    }
                })
                .subscribeWith(new MinimalDisposableObserver<Device>() {
                    @Override
                    public void onNext(Device device) {
                        setLocked(device.isLocked());
                        lockLocked = device.isLocked();
                    }
                })
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.lock_image: {
                mainActivity.showLoadingBar();
                LockStatus lockStatus = new LockStatus();
                lockStatus.setLocked(!lockLocked);
                subscriptions.add(retrofitInstance.userAPI().userDeviceLock(lockStatus)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new MinimalDisposableObserver<Device>() {
                            @Override
                            public void onNext(Device device) {
                                mainActivity.hideLoadingBar();
                                setLocked(device.isLocked());
                            }

                            @Override
                            public void onError(Throwable e) {
                                mainActivity.hideLoadingBar();
                                Snackbar.make(view, "An error occurred whilst trying to unlock the device.", Snackbar.LENGTH_LONG).show();
                            }
                        })
                );
                break;
            }
        }
    }

    public void setLocked(boolean locked) {
        if(locked) {
            lockImageView.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        } else {
            lockImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
        }
    }
}