package com.example.chacure.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chacure.R;
import com.example.chacure.activities.ChatActivity;
import com.example.chacure.models.Chat;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.ChatsProvider;
import com.example.chacure.providers.MessagesProvider;
import com.example.chacure.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends FirestoreRecyclerAdapter<Chat, ChatAdapter.ViewHolder> {

    Context context;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    ChatsProvider mChatsProvider;
    MessagesProvider mMessageProvider;
    // debemos crear esto para que la consultas no generen error al destruir la actividad
    ListenerRegistration mListener, mListener2;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
        mUserProvider = new UsersProvider();
        mUserProvider = new UsersProvider();
        mChatsProvider = new ChatsProvider();
        mAuthProvider = new AuthProvider();
        mMessageProvider = new MessagesProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position, @NonNull Chat chat) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        final String chatId = documentSnapshot.getId();
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            getFriendInfo(chat.getIdUser2(),holder);
        }else{
            getFriendInfo(chat.getIdUser1(),holder);
        }

        holder.viewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatActivity(chatId, chat.getIdUser1(), chat.getIdUser2(), chat.getIdNotification());
            }
        });

        getLastMessage(chatId, holder.textViewLastMessageFriend);
        String idSender = "";
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            idSender = chat.getIdUser2();
        }else{
            idSender = chat.getIdUser1();
        }
        getMessageNotRead(chatId, idSender, holder.txtMessageNotRead, holder.flMessageNotRead);
    }

    private void getMessageNotRead(String chatId, String idSender, TextView txtMessageNotRead, FrameLayout flMessageNotRead) {
        //.addSnapshotListener = para hacer la consulta en tiempo real
        mListener = mMessageProvider.getMessageByChatandSender(chatId, idSender).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    int size = value.size();
                    if (size > 0){
                        flMessageNotRead.setVisibility(View.VISIBLE);
                        // cambiamos el int por string para poder mostrarlo en el view
                        txtMessageNotRead.setText(String.valueOf(size));
                    }else{
                        flMessageNotRead.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    // para poderlo llamar desde el fragment en el que se está ejecutando
    public ListenerRegistration getmListener(){
        return mListener;
    }
    // para poderlo llamar desde el fragment en el que se está ejecutando
    public ListenerRegistration getmListener2(){
        return mListener2;
    }

    private void getLastMessage(String chatId, TextView txtMessageNotRead) {
       mListener2 = mMessageProvider.getLastMessage(chatId).addSnapshotListener(new EventListener<QuerySnapshot>() {
           @Override
           public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
               if (value != null){
                   int size = value.size();
                   if (size > 0){
                       final String lastMessage = value.getDocuments().get(0).getString("message");
                       if (!lastMessage.isEmpty()){
                           txtMessageNotRead.setVisibility(View.VISIBLE);
                           txtMessageNotRead.setText(lastMessage);
                       }
                       else{
                           txtMessageNotRead.setVisibility(View.GONE);
                       }
                   }
               }
           }
       });
    }

    private void goToChatActivity(String chatId, String idUser1, String idUser2, long idNotification) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("idUser1", idUser1);
        intent.putExtra("idUser2", idUser2);
        intent.putExtra("idNotification", idNotification);

        context.startActivity(intent);
    }

    private void getFriendInfo(String idFriend, ViewHolder holder) {
        mUserProvider.getUser(idFriend).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("userName")){
                        String userName = documentSnapshot.getString("userName");
                        holder.txtUserName.setText(userName);
                    }
                    if (documentSnapshot.contains("imageProfile")){
                        String imgProfile = documentSnapshot.getString("imageProfile");
                        if (imgProfile != null){
                            if (!imgProfile.isEmpty()){
                                Picasso.with(context).load(imgProfile).into(holder.imgProfile);
                            }
                        }
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chat, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserName, textViewLastMessageFriend, txtMessageNotRead;
        CircleImageView imgProfile;
        LinearLayout content;
        FrameLayout flMessageNotRead;
        View viewHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.textViewFriendNameChat);
            textViewLastMessageFriend = itemView.findViewById(R.id.textViewLastMessageFriend);
            txtMessageNotRead = itemView.findViewById(R.id.txtMessageNotRead);
            flMessageNotRead = itemView.findViewById(R.id.frameLayoutMessageNotRead);
            imgProfile = itemView.findViewById(R.id.imageFriendChat);
            content = itemView.findViewById(R.id.contentCardViewChat);



            viewHolder = itemView;
        }
    }
}
