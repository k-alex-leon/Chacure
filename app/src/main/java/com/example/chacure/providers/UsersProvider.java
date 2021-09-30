package com.example.chacure.providers;


import com.example.chacure.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersProvider {

    private CollectionReference mCollection;

    public UsersProvider(){mCollection = FirebaseFirestore.getInstance().collection("Users");}

    public Query getUserByEmail(String email){
        return mCollection.orderBy("email").startAt(email).endAt(email + '\uf8ff');
    }

    public Task<DocumentSnapshot> getUser(String id){
        return mCollection.document(id).get();
    }

    public DocumentReference getUserRealTime(String id){
        return mCollection.document(id);
    }

    // Esta solicitando el modelo User de la carpeta models
    public Task<Void> create(User user){
        return mCollection.document(user.getId()).set(user);
    }

    public Task<Void> updateInfo (User user){
        // si se quiere actualizar mas valores simplemente se agrega un nuevo map.put
        Map<String,Object> map = new HashMap<>();
        map.put("userName", user.getUserName());
        map.put("timestamp", new Date().getTime());
        map.put("infoUser", user.getInfoUser());
        return mCollection.document(user.getId()).update(map);
    }

    public Task<Void> updateImg(User user){
        Map<String,Object> mapImg = new HashMap<>();
        mapImg.put("imageProfile", user.getImageProfile());
        return mCollection.document(user.getId()).update(mapImg);
    }


    public Task<Void> updateOnline(String idUser, boolean status){
        Map<String,Object> mapImg = new HashMap<>();
        // campo encargado de verificar si el usuario esta conectado o no
        mapImg.put("online", status);
        // llama la ult vez que el usuario se conecto
        mapImg.put("lastConect", new Date().getTime());
        return mCollection.document(idUser).update(mapImg);
    }
}
