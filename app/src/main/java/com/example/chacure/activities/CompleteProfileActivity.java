package com.example.chacure.activities;

import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.chacure.R;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.UsersProvider;
import com.example.chacure.utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;



public class CompleteProfileActivity extends AppCompatActivity {

    TextInputEditText txtInputUsername;
    LinearLayout layoutBtnRegisterProfile;
    ImageButton imgBtnRegisterProfile;


    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        //Obtenemos medidas de window y asignamos el tamaño de popup
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int heigth = metrics.heightPixels;
        getWindow().setLayout((int)(width * 0.9), (int)(heigth*0.5));


        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        // cuadro de carga
        loadingDialog = new LoadingDialog(this);


        txtInputUsername = findViewById(R.id.txtInputUsername);
        layoutBtnRegisterProfile = findViewById(R.id.layoutBtnRegister);
        imgBtnRegisterProfile = findViewById(R.id.imgBtnRegister);

        layoutBtnRegisterProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        imgBtnRegisterProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String userName = txtInputUsername.getText().toString();

        if (!userName.isEmpty()){
            updateUser(userName);
        }else{
            Toast.makeText(this, "Oops it seems that something is missing", Toast.LENGTH_LONG).show();
        }

    }

    private void updateUser(String userName) {
        String id = mAuthProvider.getUid();

        User user = new User();
        user.setUserName(userName);
        user.setId(id);
        user.setTimestamp(new Date().getTime());
        loadingDialog.startLoadingAnimation();
        mUsersProvider.updateInfo(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingDialog.dissmissDialog();
                if (task.isSuccessful()){
                    Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(CompleteProfileActivity.this, "Error creating user.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}