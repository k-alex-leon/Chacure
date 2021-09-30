package com.example.chacure.providers;

import com.example.chacure.models.Message;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class MessagesProvider {
    CollectionReference mCollection;

    public MessagesProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Messages");
    }

    public Task<Void> create(Message message){
        DocumentReference documentReference = mCollection.document();
        message.setIdMessage(documentReference.getId());
        return documentReference.set(message);
    }

    public Query getMessageByChat(String idChat){
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.ASCENDING);
    }

    //metodo para verificar si el usuario vio el mensaje
    //consultamos el campo de viewed (llamar a los que esten false)
    public Query getMessageByChatandSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .whereEqualTo("viewed", false);
    }

    // Metodo para llamar los ultimos 3 mensajes no leidos
    // esto para las notif
    public Query getThreeMessageByChatandSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                            .whereEqualTo("idSender", idSender)
                                .whereEqualTo("viewed", false)
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(3);
    }

    // consulta para saber cual es el ultimo mensaje
    // .limit(1) = cantidad de mensajes a llamar
    public Query getLastMessage(String idChat){
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public Query getLastMessageSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public Task<Void> updateViewed(String idDocument, boolean state){
        Map<String, Object> map = new HashMap<>();
        map.put("viewed", state);
        return mCollection.document(idDocument).update(map);
    }
}
