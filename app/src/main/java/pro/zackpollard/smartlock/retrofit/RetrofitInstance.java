package pro.zackpollard.smartlock.retrofit;

import android.content.Context;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import pro.zackpollard.smartlock.retrofit.apis.LoginAPI;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import pro.zackpollard.smartlock.BuildConfig;
import pro.zackpollard.smartlock.retrofit.apis.SetupAPI;
import pro.zackpollard.smartlock.utils.SharedPreferencesUtil;

public class RetrofitInstance {

    private Retrofit setupRetrofit;
    private Retrofit mainRetrofit;

    private SetupAPI setupAPI;
    private LoginAPI loginAPI;

    public RetrofitInstance(final Context context) {

        initSetupRetrofit();
        initMainRetrofit(context);
    }

    private void initSetupRetrofit() {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

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

    private class HeaderInterceptor implements Interceptor {

        private Context context;

        HeaderInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public Response intercept(final Chain chain) throws IOException {

            return chain.proceed(chain.request()
                    .newBuilder()
                    .addHeader("Bearer", SharedPreferencesUtil.getStringValue(context, SharedPreferencesUtil.USER_TOKEN))
                    .build()
            );
        }
    }
}