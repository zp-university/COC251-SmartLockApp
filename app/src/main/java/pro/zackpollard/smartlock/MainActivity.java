package pro.zackpollard.smartlock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import io.reactivex.disposables.CompositeDisposable;
import pro.zackpollard.smartlock.fragments.devicescan.DeviceSetupFragment;
import pro.zackpollard.smartlock.fragments.login.LoginFragment;
import pro.zackpollard.smartlock.fragments.loginsignup.LoginSignupFragment;
import pro.zackpollard.smartlock.fragments.signup.SignupFragment;
import pro.zackpollard.smartlock.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private CompositeDisposable subscriptions = new CompositeDisposable();

    private BottomNavigationView navigationView;
    private ProgressBar spinner;

    private boolean loginSignupEnabled;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_NAV", navigationView.getSelectedItemId());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return navigateToFragment(item.getItemId());
        }
    };

    public boolean navigateToFragment(int navigationId) {
        Fragment selectedFragment = null;
        int navigationVisiblity = View.VISIBLE;
        switch (navigationId) {
            case R.id.navigation_home:
                selectedFragment = SignupFragment.newInstance();
                break;
            case R.id.navigation_dashboard:
                selectedFragment = LoginFragment.newInstance();
                break;
            case R.id.navigation_people:
                selectedFragment = DeviceSetupFragment.newInstance();
                break;
            case R.id.navigation_dropups:
                selectedFragment = LoginSignupFragment.newInstance();
                break;
            case R.layout.fragment_login_signup:
                selectedFragment = LoginSignupFragment.newInstance();
                navigationVisiblity = View.GONE;
                break;
            case R.layout.fragment_device_setup:
                selectedFragment = DeviceSetupFragment.newInstance();
                navigationVisiblity = View.GONE;
        }

        if(selectedFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_frame, selectedFragment);
            transaction.commit();
            navigationView.setVisibility(navigationVisiblity);
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int navId = R.id.navigation_home;

        if(savedInstanceState != null) {
            navId = savedInstanceState.getInt("CURRENT_NAV", R.id.navigation_home);
        }

        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigateToFragment(navId);

        spinner = findViewById(R.id.spinner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userToken = SharedPreferencesUtil.getStringValue(this, SharedPreferencesUtil.USER_TOKEN);
        if(userToken == null || userToken.trim().equals("")) {
            navigateToFragment(R.layout.fragment_login_signup);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    public void showLoadingBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.VISIBLE);
    }

    public void hideLoadingBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinner.setVisibility(View.GONE);
    }

    public void toggleLoginSignupScreen(boolean enable) {
        if(loginSignupEnabled != enable) {
            if(enable) {
                navigationView.setVisibility(View.GONE);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_frame, LoginSignupFragment.newInstance());
                transaction.commit();
            } else {
                navigationView.setVisibility(View.VISIBLE);
                navigateToFragment(R.id.navigation_home);
            }
        }
    }
}