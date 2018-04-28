package pro.zackpollard.smartlock.fragments.signup;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import pro.zackpollard.smartlock.MainActivity;
import pro.zackpollard.smartlock.R;
import pro.zackpollard.smartlock.retrofit.RetrofitInstance;
import pro.zackpollard.smartlock.retrofit.models.ErrorResponse;
import pro.zackpollard.smartlock.retrofit.models.Token;
import pro.zackpollard.smartlock.retrofit.models.User;
import pro.zackpollard.smartlock.utils.MinimalDisposableObserver;
import pro.zackpollard.smartlock.utils.SharedPreferencesUtil;
import retrofit2.HttpException;

/**
 * Created by Zack on 11/9/2017.
 */

public class SignupFragment extends Fragment implements View.OnClickListener {

    private CompositeDisposable subscriptions = new CompositeDisposable();
    private RetrofitInstance retrofitInstance;

    private MainActivity mainActivity;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText emailEditText;
    private EditText nameEditText;

    private Button signupButton;

    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        mainActivity = (MainActivity) getActivity();
        retrofitInstance = new RetrofitInstance(view.getContext());
        initViews(view);
        registerListeners();
        return view;
    }

    public void initViews(View view) {
        usernameEditText = view.findViewById(R.id.lock_ssid_text);
        passwordEditText = view.findViewById(R.id.password_text);
        confirmPasswordEditText = view.findViewById(R.id.password_confirm_text);
        emailEditText = view.findViewById(R.id.email_text);
        nameEditText = view.findViewById(R.id.name_text);

        signupButton = view.findViewById(R.id.signup_button);
    }

    public void registerListeners() {
        signupButton.setOnClickListener(this);
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
                } else if (passwordEditText.getText().toString().isEmpty() || confirmPasswordEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Password must be entered to signup!", Snackbar.LENGTH_LONG).show();
                } else if (emailEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Email must be entered to signup!", Snackbar.LENGTH_LONG).show();
                } else if (nameEditText.getText().toString().isEmpty()) {
                    Snackbar.make(view, "Name must be entered to signup!", Snackbar.LENGTH_LONG).show();
                } else if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()) && passwordEditText.getText().toString().length() < 8) {
                    Snackbar.make(view, "Entered passwords must match and be longer than 8 characters to signup!", Snackbar.LENGTH_LONG).show();
                } else if (nameEditText.getText().toString().indexOf(' ') == -1) {
                    Snackbar.make(view, "You must enter your full name to signup!", Snackbar.LENGTH_LONG).show();
                } else {
                    User user = new User();
                    user.setUsername(usernameEditText.getText().toString());
                    user.setPassword(passwordEditText.getText().toString());
                    user.setEmail(emailEditText.getText().toString());
                    String name = nameEditText.getText().toString();
                    user.setFirstName(name.substring(0, name.indexOf(' ')));
                    user.setLastName(name.substring(name.indexOf(' '), name.length()));

                    ((MainActivity) view.getContext()).showLoadingBar();

                    retrofitInstance.loginAPI().signup(user)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new MinimalDisposableObserver<Token>() {
                                @Override
                                public void onNext(Token token) {
                                    Snackbar.make(view, token.getToken(), Snackbar.LENGTH_LONG).show();
                                    mainActivity.hideLoadingBar();
                                    SharedPreferencesUtil.addKeyValue(view.getContext(), SharedPreferencesUtil.USER_TOKEN, token.getToken());
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