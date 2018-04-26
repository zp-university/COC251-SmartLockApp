package pro.zackpollard.smartlock.retrofit.apis;


import io.reactivex.Observable;
import pro.zackpollard.smartlock.retrofit.models.DeviceUuid;
import pro.zackpollard.smartlock.retrofit.models.SetupDetails;
import pro.zackpollard.smartlock.retrofit.models.SetupStatus;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SetupAPI {

    @GET("/api/v1/setup/status")
    Observable<SetupStatus> getSetupStatus();

    @POST("/api/v1/setup/details")
    Observable<DeviceUuid> sendSetupDetails(@Body SetupDetails body);
}