package pro.zackpollard.smartlock.retrofit;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import pro.zackpollard.smartlock.BuildConfig;
import pro.zackpollard.smartlock.retrofit.apis.LoginAPI;
import pro.zackpollard.smartlock.retrofit.apis.SetupAPI;
import pro.zackpollard.smartlock.retrofit.apis.UserAPI;
import pro.zackpollard.smartlock.utils.SharedPreferencesUtil;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private Retrofit setupRetrofit;
    private Retrofit mainRetrofit;

    private SetupAPI setupAPI;
    private LoginAPI loginAPI;
    private UserAPI userAPI;

    public RetrofitInstance(final Context context) {

        initSetupRetrofit();
        initMainRetrofit(context);
    }

    private void initSetupRetrofit() {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        OkHttpClient okHttpClient = okHttpClientBuilder.readTimeout(60, TimeUnit.SECONDS).build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.SETUP_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        setupRetrofit = builder.build();
    }

    private void initMainRetrofit(Context context) {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        okHttpClientBuilder.addInterceptor(new HeaderInterceptor(context));

        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("smartlockapp.zackpollard.pro", "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
                .build();

        okHttpClientBuilder.certificatePinner(certificatePinner);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClientBuilder.addInterceptor(interceptor);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.MAIN_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        mainRetrofit = builder.build();
    }

    public SetupAPI setupAPI() {
        if (setupAPI == null) {
            setupAPI = setupRetrofit.create(SetupAPI.class);
        }
        return setupAPI;
    }

    public LoginAPI loginAPI() {
        if (loginAPI == null) {
            loginAPI = mainRetrofit.create(LoginAPI.class);
        }
        return loginAPI;
    }

    public UserAPI userAPI() {
        if (userAPI == null) {
            userAPI = mainRetrofit.create(UserAPI.class);
        }
        return userAPI;
    }

    private class HeaderInterceptor implements Interceptor {

        private Context context;

        HeaderInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public Response intercept(final Chain chain) throws IOException {

            return chain.proceed(chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + SharedPreferencesUtil.getStringValue(context, SharedPreferencesUtil.USER_TOKEN))
                    .build()
            );
        }
    }
}