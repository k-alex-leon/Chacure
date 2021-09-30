package com.example.chacure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.auth.AuthResult;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class RegisterActivity extends AppCompatActivity {

    TextInputEditText txtInputUserName, txtInputUserEmail,
            txtInputUserPassword, txtInputUserConfirmPass;

    LinearLayout layoutBtnRegister;

    ImageView imgViewGoBack;
    ImageButton imgButtonRegister;

    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Obtenemos medidas de window y asignamos el tamaño de popup
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int heigth = metrics.heightPixels;
        getWindow().setLayout((int)(width * 0.9), (int)(heigth*0.7));

        txtInputUserName = findViewById(R.id.inputUserName);
        txtInputUserEmail = findViewById(R.id.inputEmailUser);
        txtInputUserPassword = findViewById(R.id.inputPassword);
        txtInputUserConfirmPass = findViewById(R.id.inputConfirmPassword);

        imgViewGoBack = findViewById(R.id.arrowGoBack);
        imgButtonRegister = findViewById(R.id.imgBtnRegister);
        layoutBtnRegister = findViewById(R.id.layoutBtnRegister);

        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();

        // cuadro de carga
        loadingDialog = new LoadingDialog(this);

        imgViewGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        layoutBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        imgButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String userName = txtInputUserName.getText().toString();
        String emailUser = txtInputUserEmail.getText().toString();
        String passUser = txtInputUserPassword.getText().toString();
        String confirmPassUser = txtInputUserConfirmPass.getText().toString();

        if (!userName.isEmpty() && !emailUser.isEmpty() &&
             !passUser.isEmpty() && !confirmPassUser.isEmpty()){
            if (isEmailValid(emailUser)){
                if (passUser.equals(confirmPassUser)){
                    if (passUser.length() >= 6){
                        createUser(userName, emailUser, passUser);
                    }else{
                        Toast.makeText(RegisterActivity.this, "This password is too short.", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Passwords must be the same.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(RegisterActivity.this, "Invalid email.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(RegisterActivity.this, "Oops it seems that something is missing", Toast.LENGTH_LONG).show();
        }

    }
    // metodo de creacion de ususario
    private void createUser(String userName, String emailUser, String passUser) {
        loadingDialog.startLoadingAnimation();
        mAuthProvider.register(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //Estamos adquiriendo el id para utilizarlo en el .document()
                    String id = mAuthProvider.getUid();
                    //pasamos datos desde el modelo de user al mUserProvider
                    User user = new User();
                    user.setId(id);
                    user.setUserName(userName);
                    user.setEmail(emailUser);
                    user.setTimestamp(new Date().getTime());

                    mUserProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingDialog.dissmissDialog();
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Welcome!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                //limpiando las activities para que el user no pueda volver a la de registro
                                // cierra la app si el user intenta volver a la actividad anterior
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }else{
                                Toast.makeText(RegisterActivity.this, "Error creating user", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }else{
                    loadingDialog.dissmissDialog();
                    Toast.makeText(RegisterActivity.this, "Error creating user", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    // validacion de un correo
    private boolean isEmailValid(String emailUser) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emailUser);
        return matcher.matches();
    }
}