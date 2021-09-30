package com.example.chacure.providers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.chacure.utils.CompressorBitmapImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;

public class ImageProvider {

    StorageReference mStorage;

    public ImageProvider(){
        // hacemos referencia de donde se guarda la img
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    // almacenar img
    public UploadTask saveImage(Context context, @NonNull File file){
        byte[] imageByte = CompressorBitmapImage.getImage(context,file.getPath(),500,500);
        StorageReference storageReference = mStorage.child(new Date() + ".jpg");
        mStorage = storageReference;
        UploadTask task = storageReference.putBytes(imageByte);
        return task;
    }

    public StorageReference getmStorage(){
        return mStorage;
    }
}
