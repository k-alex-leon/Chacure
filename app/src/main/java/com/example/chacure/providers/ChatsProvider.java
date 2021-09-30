package com.example.chacure.providers;

import com.example.chacure.models.Chat;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ChatsProvider {

    CollectionReference mCollection;

    public ChatsProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Chats");
    }

    public void create(Chat chat){
        mCollection.document(chat.getIdUser1() + chat.getIdUser2()).set(chat);
    }

    public Query getAllChats(String idUser){
        return mCollection.whereArrayContains("ids", idUser);
    }

    // consultando si el documento ya existe para no duplicar datos en la bd
    public Query getChatByUsers(String idUser1, String idUser2){
        ArrayList<String> ids = new ArrayList<>();
        ids.add(idUser1+idUser2);
        ids.add(idUser2+idUser1);
        return mCollection.whereIn("idChat",ids);
    }
}
