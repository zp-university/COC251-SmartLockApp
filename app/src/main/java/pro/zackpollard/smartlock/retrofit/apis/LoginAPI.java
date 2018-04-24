package pro.zackpollard.smartlock.retrofit.apis;


import io.reactivex.Observable;
import pro.zackpollard.smartlock.retrofit.models.Authentication;
import pro.zackpollard.smartlock.retrofit.models.Token;
import pro.zackpollard.smartlock.retrofit.models.User;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginAPI {

    @POST("/api/v1/auth/login")
    Observable<Token> login(@Body Authentication body);

    @POST("/api/v1/auth/signup")
    Observable<Token> signup(@Body User body);
}