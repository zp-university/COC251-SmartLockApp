package pro.zackpollard.smartlock.fragments.loginsignup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import pro.zackpollard.smartlock.fragments.login.LoginFragment;
import pro.zackpollard.smartlock.fragments.signup.SignupFragment;

public class LoginSignupAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public LoginSignupAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                SignupFragment signup = new SignupFragment();
                return signup;
            case 1:
                LoginFragment login = new LoginFragment();
                return login;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}