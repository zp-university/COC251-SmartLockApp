package pro.zackpollard.smartlock.fragments.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import pro.zackpollard.smartlock.MainActivity;
import pro.zackpollard.smartlock.R;
import pro.zackpollard.smartlock.retrofit.RetrofitInstance;
import pro.zackpollard.smartlock.retrofit.models.Authentication;
import pro.zackpollard.smartlock.retrofit.models.ErrorResponse;
import pro.zackpollard.smartlock.retrofit.models.Token;
import pro.zackpollard.smartlock.utils.MinimalDisposableObserver;
import pro.zackpollard.smartlock.utils.SharedPreferencesUtil;

/**
 * Created by Zack on 11/9/2017.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {

    private CompositeDisposable subscriptions = new CompositeDisposable();
    private RetrofitInstance retrofitInstance;

    private MainActivity mainActivity;

    private EditText usernameEditText;
    private EditText passwordEditText;

    private Button loginButton;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mainActivity = (MainActivity) getActivity();
        retrofitInstance = new RetrofitInstance(view.getContext());
        initViews(view);
        registerListeners();
        return view;
    }

    public void initViews(View view) {
        usernameEditText = view.findViewById(R.id.username_text);
        passwordEditText = view.findViewById(R.id.password_text);

        loginButton = view.findViewById(R.id.signup_button);
    }

    public void registerListeners() {
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.signup_button: {
                if (usernameEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Username must be entered to signup!", Snackbar.LENGTH_LONG).show();
                } else if (passwordEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Password must be entered to signup!", Snackbar.LENGTH_LONG).show();
                } else {
                    Authentication auth = new Authentication();
                    auth.setUsername(usernameEditText.getText().toString());
                    auth.setPassword(passwordEditText.getText().toString());

                    ((MainActivity) view.getContext()).showLoadingBar();

                    retrofitInstance.loginAPI().login(auth)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new MinimalDisposableObserver<Token>() {
                                @Override
                                public void onNext(Token token) {
                                    Snackbar.make(view, token.getToken(), Snackbar.LENGTH_LONG).show();
                                    mainActivity.hideLoadingBar();
                                    SharedPreferencesUtil.addKeyValue(view.getContext(), SharedPreferencesUtil.USER_TOKEN, token.getToken());
                                    mainActivity.navigateToFragment(R.layout.fragment_device_setup);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    mainActivity.hideLoadingBar();
                                    Log.e("ERROR", "onError: ", e);
                                    String errorString = e.getLocalizedMessage();
                                    if(e instanceof HttpException) {
                                        ResponseBody responseBody = ((HttpException) e).response().errorBody();
                                        if(responseBody != null) {
                                            ErrorResponse error = new Gson().fromJson(responseBody.charStream(), ErrorResponse.class);
                                            errorString = error.getMessage();
                                        }
                                    }

                                    Snackbar.make(view, errorString, Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }
        }
    }
}