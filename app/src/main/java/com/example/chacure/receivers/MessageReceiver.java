package com.example.chacure.receivers;


import static com.example.chacure.services.MyFirebaseMessagingClient.NOTIFICATION_REPLY;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

import com.example.chacure.models.FCMBody;
import com.example.chacure.models.FCMResponse;
import com.example.chacure.models.Message;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.MessagesProvider;
import com.example.chacure.providers.NotificationProvider;
import com.example.chacure.providers.TokenProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageReceiver extends BroadcastReceiver {

    String mExtraIdSender, mExtraIdReceiver,
            mExtraIdChat, mExtraUsernameSender,
            mExtraUsernameReceiver;

    String mExtraImageSender, mExtraImageReceiver;

    int mExtraIdNotification;

    TokenProvider mTokenProvider;
    MessagesProvider mMessagesProvider;
    NotificationProvider mNotificationProvider;
    @Override
    public void onReceive(Context context, Intent intent) {

        mExtraIdSender = intent.getExtras().getString("idSender");
        mExtraIdReceiver = intent.getExtras().getString("idReceiver");
        mExtraIdChat = intent.getExtras().getString("idChat");
        mExtraIdNotification = intent.getExtras().getInt("idNotification");
        mExtraUsernameSender = intent.getExtras().getString("usernameSender");
        mExtraUsernameReceiver = intent.getExtras().getString("usernameReceiver");
        mExtraImageSender = intent.getExtras().getString("imageSender");
        mExtraImageReceiver = intent.getExtras().getString("imageReceiver");

        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mExtraIdNotification);

        String message = getMessageText(intent).toString();
        sendMessage(message);

    }

    private void sendMessage(String textMessage) {
        Message message = new Message();
        message.setIdChat(mExtraIdChat);
        message.setIdSender(mExtraIdReceiver);
        message.setIdReceiver(mExtraIdSender);
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setMessage(textMessage);

        mMessagesProvider = new MessagesProvider();
        mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    getToken(message);
                }
            }
        });
    }

    private void getToken(Message message){
        mTokenProvider.getToken(mExtraIdSender).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        //permite crear nuestro objeto message a un array gson
                        Gson gson = new Gson();
                        ArrayList<Message> messageArrayList = new ArrayList<>();
                        messageArrayList.add(message);
                        String messages = gson.toJson(messageArrayList);
                        sendNotification(token, messages, message);
                    }
                }
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message){

        final Map<String, String> data = new HashMap<>();
        data.put("title", "New message!");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mExtraIdNotification));
        data.put("messages", messages);
        data.put("usernameSender", mExtraUsernameReceiver.toUpperCase(Locale.ROOT));
        data.put("usernameReceiver", mExtraUsernameSender.toUpperCase(Locale.ROOT));
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());

        data.put("imageSender", mExtraImageReceiver);
        data.put("imageReceiver", mExtraImageSender);

        FCMBody body = new FCMBody(token, "high", "4500s", data);
        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.d("ERROR", "The error are in:" + t.getMessage());
            }
        });
    }

    private CharSequence getMessageText(Intent intent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null){
            return remoteInput.getCharSequence(NOTIFICATION_REPLY);
        }
        return null;
    }
}
