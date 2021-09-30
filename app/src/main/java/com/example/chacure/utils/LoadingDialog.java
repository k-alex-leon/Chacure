package com.example.chacure.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.chacure.R;

public class LoadingDialog {

    private Activity activity;
    private AlertDialog alertDialog;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    //este metodo inicia la animacion de carga
    public void startLoadingAnimation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        // con esto impedimos realizar la accion de cancelar animacion
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void dissmissDialog(){
        alertDialog.dismiss();
    }
}
