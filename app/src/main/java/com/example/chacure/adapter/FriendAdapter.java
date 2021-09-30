package com.example.chacure.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chacure.R;
import com.example.chacure.models.Chat;
import com.example.chacure.models.FCMBody;
import com.example.chacure.models.FCMResponse;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.ChatsProvider;
import com.example.chacure.providers.NotificationProvider;
import com.example.chacure.providers.TokenProvider;
import com.example.chacure.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendAdapter extends FirestoreRecyclerAdapter<User, FriendAdapter.ViewHolder> {

    Context context;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ChatsProvider mChatProvider;
    TokenProvider mTokenProvider;
    NotificationProvider mNotificationProvider;
    String idUserProfile, idFriend;

    Dialog alertDialogAddPerson;

    public FriendAdapter(FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }

    // Establece el contenido que se desea mostrar
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User user) {

        idUserProfile = mAuthProvider.getUid();
        idFriend = user.getId();
        holder.txtFriendName.setText(user.getUserName());
        holder.txtFriendEmail.setText(user.getEmail());

        if (user.getImageProfile() != null){
            if (!user.getImageProfile().isEmpty()){
                Picasso.with(context).load(user.getImageProfile()).into(holder.imageViewUserFriend);
            }
        }
        
        holder.friendCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // volvemos a llamar al id para saber de cual usuario se muestra la info en el alertDialog
                final String idFriendAlert = user.getId();
                showAlert(idFriendAlert);
            }
        });

        if (idUserProfile.contentEquals(idFriend)){
            holder.friendCardView.setVisibility(View.GONE);
            holder.friendCardView.setEnabled(false);
        }

    }
    // muestra la info del usuario seleccionado por medio de un dialog que creamos
    private void showAlert(String idFriendAlert) {
        alertDialogAddPerson.setContentView(R.layout.add_user_layout_dialog);
        alertDialogAddPerson.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView circleAlertImgProfile = alertDialogAddPerson.findViewById(R.id.dialogCircleImageProfile);
        ImageView closeAlert = alertDialogAddPerson.findViewById(R.id.imgCloseAlert);
        TextView txtUserNameAlert = alertDialogAddPerson.findViewById(R.id.txtUserNameAlert);
        Button btnCancelAdd = alertDialogAddPerson.findViewById(R.id.btnCancelAdd);
        Button btnAdd = alertDialogAddPerson.findViewById(R.id.btnAddPerson);

        mUsersProvider.getUser(idFriendAlert).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("userName")){
                        String userName = documentSnapshot.getString("userName");
                        txtUserNameAlert.setText(userName);
                    }
                    if (documentSnapshot.contains("imageProfile")){
                        String imgProfile = documentSnapshot.getString("imageProfile");
                        if (imgProfile != null){
                            if (!imgProfile.isEmpty()){
                                Picasso.with(context).load(imgProfile).into(circleAlertImgProfile);
                            }
                        }
                    }
                }
            }
        });

        closeAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    alertDialogAddPerson.dismiss();
            }
        });

        btnCancelAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogAddPerson.dismiss();
            }
        });
        
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfChatExist(idFriendAlert);
                mUsersProvider.getUser(idUserProfile).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.contains("userName")){
                                String bodyNoti = documentSnapshot.getString("userName");
                                sendNotification(idFriendAlert, bodyNoti);
                                alertDialogAddPerson.dismiss();
                            }
                        }
                    }
                });
            }
        });

        alertDialogAddPerson.show();
    }

    // metodo para enviar una noti al usuario que se agrega
    private void sendNotification(String idFriendAlert, String bodyNoti) {
        if (idFriendAlert == null){
            return;
        }
        mTokenProvider.getToken(idFriendAlert).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        Map<String, String> data = new HashMap<>();
                        data.put("title", "New friend!");
                        data.put("body", bodyNoti);
                        FCMBody body = new FCMBody(token, "high", "4500s", data);
                        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                if (response.body() != null){
                                    if (response.body().getSuccess() == 1){
                                        Toast.makeText(context, "Notification send!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(context, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(context, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                Toast.makeText(context, "Notification sending error! ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else{
                    Toast.makeText(context, "Token doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // verifica que el chat existe en la bd
    private void checkIfChatExist(String idFriendAlert){
        mChatProvider.getChatByUsers(idUserProfile, idFriendAlert).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                if (size == 0){
                    createChat(idFriendAlert);
                }else{
                    Toast.makeText(context, "This person is on your chat list!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // creamos un nuevo chat en la bd
    private void createChat(String idFriendAlert) {
        Chat chat = new Chat();
        chat.setIdUser1(idUserProfile);
        chat.setIdUser2(idFriendAlert);
        chat.setWriting(false);
        chat.setTimestamp(new Date().getTime());
        // generamos un id random para el campo de noti del document chats
        Random random = new Random();
        int n = random.nextInt(1000000);
        chat.setIdNotification(n);
        chat.setIdChat(idUserProfile+idFriendAlert);

        ArrayList<String> ids = new ArrayList<>();
        ids.add(idUserProfile);
        ids.add(idFriendAlert);
        chat.setIds(ids);
        mChatProvider.create(chat);
    }

    // se asigna la vista que desea mostrar
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user,parent,false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtFriendName, txtFriendEmail;
        ImageView imageViewUserFriend;
        CardView friendCardView;

        public ViewHolder(View view) {
            super(view);

            mAuthProvider = new AuthProvider();
            mUsersProvider = new UsersProvider();
            mChatProvider = new ChatsProvider();
            mTokenProvider = new TokenProvider();
            mNotificationProvider = new NotificationProvider();
            alertDialogAddPerson = new Dialog(view.getContext());

            txtFriendName = view.findViewById(R.id.textViewFriendName);
            txtFriendEmail = view.findViewById(R.id.textViewEmailFriend);
            imageViewUserFriend = view.findViewById(R.id.imageFriendProfile);
            friendCardView = view.findViewById(R.id.friendCardView);

        }
    }
}
