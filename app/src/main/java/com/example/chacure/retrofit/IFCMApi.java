package com.example.chacure.retrofit;

import com.example.chacure.models.FCMBody;
import com.example.chacure.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA7-5ClM4:APA91bF2hTzs6vkhvwBIiQEjCumlDEDC8s5RKjbJhZl56NV0PCBundjoSg7mg6ESEoss0z0CMiJcLTT0-UnruPcMK92EjcbDPm3EqjIsX1a11xJYuUk6jdAJn2WyC3GxKWvrUsMnEgRm"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
