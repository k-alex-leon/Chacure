package com.example.chacure.providers;

import com.example.chacure.models.FCMBody;
import com.example.chacure.models.FCMResponse;
import com.example.chacure.retrofit.IFCMApi;
import com.example.chacure.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider(){}

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClient(url).create(IFCMApi.class).send(body);
    }
}
