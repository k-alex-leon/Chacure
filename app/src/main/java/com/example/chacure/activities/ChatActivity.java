package com.example.chacure.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chacure.R;
import com.example.chacure.adapter.MessagesAdapter;
import com.example.chacure.models.FCMBody;
import com.example.chacure.models.FCMResponse;
import com.example.chacure.models.Message;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.MessagesProvider;
import com.example.chacure.providers.NotificationProvider;
import com.example.chacure.providers.TokenProvider;
import com.example.chacure.providers.UsersProvider;
import com.example.chacure.utils.ViewedMessageHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    ImageView imageSendMessage, imgArrowGoBack;
    EditText editTextMessage;
    TextView txtUserNameChat, txtUserConditionChat;
    CircleImageView imgCircleImageChat;
    RecyclerView recyclerViewMessages;
    MessagesAdapter mMessagesAdapter;

    String extraIdUser1, extraIdUser2, extraChatId;
    // variables globales para las noti
    String imgReceiver = "",
            imgSender = "";
    String mMyUserName, mUserNameChat;

    long mIdNotificationChat;

    View mActionBarView;
    MessagesProvider mMessagesProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;

    LinearLayoutManager linearLayoutManager;
    ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // se requiere inst antes del metodo showCustomToolbar para evitar fallos
        mMessagesProvider = new MessagesProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();

        extraIdUser1 = getIntent().getStringExtra("idUser1");
        extraIdUser2 = getIntent().getStringExtra("idUser2");
        extraChatId = getIntent().getStringExtra("chatId");
        mIdNotificationChat = getIntent().getIntExtra("idNotification",0);

        imageSendMessage = findViewById(R.id.imgSendMessage);
        editTextMessage = findViewById(R.id.editTextMessage);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
         linearLayoutManager = new LinearLayoutManager(this);

         // mueve la pantalla hasta el ultimo mensaje enviado
         linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        showCustomToolbar(R.layout.custom_toolbar_chat);

        imageSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // metodo para cambiar el estado de los mensajes a visto
        updateViwed();
        getMyInfoUser();
    }
    // cuando el user entra a la actv se actualiza el estado de los mensajes
    private void updateViwed() {
        String idSender = "";
        if (mAuthProvider.getUid().equals(extraIdUser1)){
            idSender = extraIdUser2;
        }else{
            idSender = extraIdUser1;
        }
        mMessagesProvider.getMessageByChatandSender(extraChatId, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // recorre cada uno de los doc en el chat
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    // usamos el metodo update para cambiar el estado de los mnsj cuando el usuario entra a la actividad
                    mMessagesProvider.updateViewed(documentSnapshot.getId(), true);
                }
            }
        });
    }

    private void sendMessage() {
        String textMessage = editTextMessage.getText().toString();
        if (!textMessage.isEmpty()){
            Message message = new Message();
            message.setIdChat(extraChatId);
            if (mAuthProvider.getUid().equals(extraIdUser1)){
                message.setIdSender(extraIdUser1);
                message.setIdReceiver(extraIdUser2);
            }else{
                message.setIdSender(extraIdUser2);
                message.setIdReceiver(extraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setMessage(textMessage);

            mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        editTextMessage.setText("");
                        getToken(message);
                        Toast.makeText(ChatActivity.this, "Message send", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getUserInfo() {
        // comprobando que info mostrar dependiendo de que usuario abra el chat
        String idUserInfo = "";
        if (mAuthProvider.getUid().equals(extraIdUser1)){
            idUserInfo = extraIdUser2;
        }else{
            idUserInfo = extraIdUser1;
        }
        listenerRegistration = mUsersProvider.getUserRealTime(idUserInfo).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.exists()){
                    if(documentSnapshot.contains("userName")){
                        mUserNameChat = documentSnapshot.getString("userName");
                        txtUserNameChat.setText(mUserNameChat);
                    }
                    if(documentSnapshot.contains("online")){
                        boolean online = documentSnapshot.getBoolean("online");
                        if (online){
                            txtUserConditionChat.setText("Online");
                        }
                        else if(documentSnapshot.contains("lastConect")){
                            // tomamos la fecha en una var
                            long lastConect = documentSnapshot.getLong("lastConect");
                            // damos formato para mostrar el dia y la hora de la ultima conx
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM hh:mm", Locale.getDefault());
                            String lastConectDate = simpleDateFormat.format(lastConect);
                            txtUserConditionChat.setText(lastConectDate);
                        }
                    }
                    if (documentSnapshot.contains("imageProfile")){
                        imgReceiver = documentSnapshot.getString("imageProfile");
                        if (!imgReceiver.isEmpty()){
                            Picasso.with(ChatActivity.this).load(imgReceiver).into(imgCircleImageChat);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onStart() {
        super.onStart();
        // enviar el estado del usuario
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);

        Query query = mMessagesProvider.getMessageByChat(extraChatId);
        FirestoreRecyclerOptions<Message> options =
                new FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(query,Message.class)
                        .build();
        mMessagesAdapter = new MessagesAdapter(options, this);
        mMessagesAdapter.notifyDataSetChanged();
        recyclerViewMessages.setAdapter(mMessagesAdapter);
        // escucha los cambios de la bd
        mMessagesAdapter.startListening();

        // esto para que se actualize siempre con el ultimo mensaje enviado
        mMessagesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // metodo para la escucha de los cambios
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                // metodo para cambiar el estado de los mensajes a visto
                updateViwed();

                int numberMessage = mMessagesAdapter.getItemCount();
                int lastMessagePosition = linearLayoutManager.findLastVisibleItemPosition();
                // validamos la posicion de los mnsj para determinar que hacer
                if (lastMessagePosition == -1 || (positionStart >=(numberMessage -1) && lastMessagePosition == (positionStart -1))){
                    recyclerViewMessages.scrollToPosition(positionStart);
                }
            }
        });
    }

    // metodo para enviar una noti al usuario que se agrega
    private void getToken(Message message) {
        String idUser = "";
        if (mAuthProvider.getUid().equals(extraIdUser1)){
            idUser = extraIdUser2;
        }else{
            idUser = extraIdUser1;
        }

        if (extraChatId == null){
            return;
        }
        mTokenProvider.getToken(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        getLastThreeMessages(message, token);

                    }
                }else{
                    Toast.makeText(ChatActivity.this, "Token doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLastThreeMessages(Message message, final String token) {

        mMessagesProvider.getThreeMessageByChatandSender(extraChatId, mAuthProvider.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Gson recibe un arraylist de mensjs
                ArrayList<Message> messageArrayList = new ArrayList<>();

                // este for recorre todos los datos que llegaron de la consulta
                for (DocumentSnapshot d: queryDocumentSnapshots.getDocuments()){
                    // validamos que el doc exista
                    if (d.exists()){
                        Message message = d.toObject(Message.class);
                        messageArrayList.add(message);
                    }
                }

                // validamos que el array no venga vacio
                if (messageArrayList.size() == 0){
                    // si viene vacio le agregamos el mnsj
                    messageArrayList.add(message);
                }

                Collections.reverse(messageArrayList);

                //permite crear nuestro objeto message a un array gson
                Gson gson = new Gson();
                String messages = gson.toJson(messageArrayList);

                sendNotification(token, messages, message);
            }
        });
    }


    private void sendNotification(final String token, String messages, Message message){

        final Map<String, String> data = new HashMap<>();
        data.put("title", "New message!");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mIdNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", mMyUserName.toUpperCase(Locale.ROOT));
        data.put("usernameReceiver", mUserNameChat.toUpperCase(Locale.ROOT));
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());

        if(imgSender.equals("")){
            imgSender = "invalid image!";
        }
        if (imgReceiver.equals("")){
            imgReceiver = "invalid image!";
        }
        data.put("imageSender", imgSender);
        data.put("imageReceiver", imgReceiver);

        String idSender = "";
        if (mAuthProvider.getUid().equals(extraIdUser1)){
            idSender = extraIdUser2;
        }else{
            idSender = extraIdUser1;
        }
        mMessagesProvider.getLastMessageSender(extraChatId,idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                String lastMessage = "";

                if (size > 0){
                    lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                    data.put("lastMessage", lastMessage);
                }

                FCMBody body = new FCMBody(token, "high", "4500s", data);
                mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body() != null){
                            if (response.body().getSuccess() == 1){

                            }else{
                                Toast.makeText(ChatActivity.this, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(ChatActivity.this, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getMyInfoUser(){
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("userName")){
                        mMyUserName = documentSnapshot.getString("userName");
                    }
                    if (documentSnapshot.contains("imageProfile")){
                        // img del usuario que envia el mnjs
                        imgSender = documentSnapshot.getString("imageProfile");
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessagesAdapter.stopListening();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
    }

    // para agregar el toolbar personalizado
    private void showCustomToolbar(int resource) {
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource,null);
        actionBar.setCustomView(mActionBarView);

        imgCircleImageChat = mActionBarView.findViewById(R.id.circleImageProfileChat);
        txtUserNameChat = mActionBarView.findViewById(R.id.txtUserNameChat);
        txtUserConditionChat = mActionBarView.findViewById(R.id.txtUserCondition);
        imgArrowGoBack = mActionBarView.findViewById(R.id.arrowGoBackChat);

        imgArrowGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // llamando la info para mostrar en el toolbar del chat
        getUserInfo();
    }
}