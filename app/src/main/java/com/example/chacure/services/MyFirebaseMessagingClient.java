package com.example.chacure.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.example.chacure.R;
import com.example.chacure.channel.NotificationHelper;
import com.example.chacure.models.Message;
import com.example.chacure.receivers.MessageReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    public static final String NOTIFICATION_REPLY = "NotificationReply";

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // obtenemos la info que llega desde las noti
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        if(title != null){
            if (title.equals("New message!")){
                showNotificationMessage(data);
            }
            else if(title.equals("New friend!")){
                showNotification(title,body);
            }

        }
    }

    private void showNotification(String title, String body){
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotification(title, body);
        // generamos un id para la noti
        Random random = new Random();
        // esto para que se puedan enviar noti diferentes
        int n = random.nextInt(10000);
        notificationHelper.getManager().notify(n, builder.build());
    }

    private void showNotificationMessage(Map<String, String> data){

        String imageSender = data.get("imageSender");
        String imageReceiver = data.get("imageReceiver");

        getImageSender(data ,imageSender, imageReceiver);

    }

    private void getImageSender(Map<String, String> data,String imageSender, String imageReceiver) {

        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext())
                                .load(imageSender)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmapSender, Picasso.LoadedFrom from) {
                                        getImageReceiver(data, imageReceiver, bitmapSender);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        getImageReceiver(data, imageReceiver, null);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                    }
                });

    }

    private void getImageReceiver(Map<String, String> data,String imageReceiver, Bitmap bitmapSender){
        Picasso.with(getApplicationContext())
                .load(imageReceiver)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmapReceiver, Picasso.LoadedFrom from) {
                        notifyMessage(data,bitmapSender ,bitmapReceiver);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        notifyMessage(data, bitmapSender, null);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private void notifyMessage(Map<String, String> data, Bitmap bitmapSender, Bitmap bitmapReceiver){
        // pasamos el dato como entero para evitar errores
        int idNotiChat = Integer.parseInt(data.get("idNotification"));
        String usernameSender = data.get("usernameSender");
        String usernameReceiver = data.get("usernameReceiver");
        String lastMessage = data.get("lastMessage");
        String messagesJSON = data.get("messages");
        String idSender = data.get("idSender");
        String idReceiver = data.get("idReceiver");
        String idChat = data.get("idChat");

        String imageSender = data.get("imageSender");
        String imageReceiver = data.get("imageReceiver");

        Intent intent = new Intent(this, MessageReceiver.class);
        intent.putExtra("idSender", idSender);
        intent.putExtra("idReceiver", idReceiver);
        intent.putExtra("idChat", idChat);
        intent.putExtra("idNotification", idNotiChat);
        intent.putExtra("usernameSender", usernameSender);
        intent.putExtra("usernameReceiver", usernameReceiver);
        intent.putExtra("imageReceiver", imageReceiver);
        intent.putExtra("imageSender", imageSender);

        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("ur message").build();

        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.mipmap.ic_chacure_logo,
                "Answer",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();


        Gson gson = new Gson();
        Message[] messages = gson.fromJson(messagesJSON, Message[].class);


        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder =
                notificationHelper.getNotificationMessage(
                        messages,
                        usernameSender,
                        usernameReceiver,
                        lastMessage,
                        bitmapSender,
                        bitmapReceiver,
                        action);
        notificationHelper.getManager().notify(idNotiChat, builder.build());
    }
}
