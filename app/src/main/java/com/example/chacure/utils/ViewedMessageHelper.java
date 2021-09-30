package com.example.chacure.utils;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.UsersProvider;

import java.util.List;
 /**
 * Consulta si el usuario tiene la app funcionando, para ejecutar
 * el metodo de la actualizacion de estado (online - offline)
 * del chatActivity
 * **/

public class ViewedMessageHelper {

    public static void updateOnline(boolean status, final Context context){
        UsersProvider usersProvider = new UsersProvider();
        AuthProvider authProvider = new AuthProvider();

        if(authProvider.getUid() != null){
            // revisa que la app este en modo background
            if (isApplicationSentToBackground(context)){
                usersProvider.updateOnline(authProvider.getUid(), false);
            }
            else if(status){
                usersProvider.updateOnline(authProvider.getUid(), true);
            }
        }
    }

    public static boolean isApplicationSentToBackground(final Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()){
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())){
                return true;
            }
        }
        return false;
    }
}
