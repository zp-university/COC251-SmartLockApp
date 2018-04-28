package pro.zackpollard.smartlock.retrofit.apis;


import io.reactivex.Observable;
import pro.zackpollard.smartlock.retrofit.models.Device;
import pro.zackpollard.smartlock.retrofit.models.DeviceUuid;
import pro.zackpollard.smartlock.retrofit.models.LockStatus;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserAPI {

    @POST("/api/v1/user/device/add")
    Observable<Device> userAddDevice(@Body DeviceUuid body);

    @POST("/api/v1/user/device/lock")
    Observable<Device> userDeviceLock(@Body LockStatus body);

    @GET("/api/v1/user/device")
    Observable<Device> getUserDevice();
}