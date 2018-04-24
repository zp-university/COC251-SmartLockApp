package pro.zackpollard.smartlock.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SetupDetails {

    @SerializedName("jwttoken")
    @Expose
    private String jwtToken;
    @SerializedName("wifissid")
    @Expose
    private String wifiSsid;
    @SerializedName("wifipassword")
    @Expose
    private String wifiPassword;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }
}