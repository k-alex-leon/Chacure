package com.example.chacure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chacure.R;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.UsersProvider;
import com.example.chacure.utils.LoadingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    Button btnSignIn;
    TextInputEditText inputEmailUser, inputPassword;
    TextView txtViewRegisterUser;
    AuthProvider mAuthProvider;
    LinearLayout layoutBtn, layoutEmailPassContent,layoutBtnLogin;
    ImageButton imgBtnLogin;
    SignInButton btnGoogleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    private final int REQUEST_CODE_GOOGLE = 1;


    UsersProvider mUsersProvider;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignInEmailAndPassword);
        inputEmailUser = findViewById(R.id.inputEmailUser);
        inputPassword = findViewById(R.id.inputPassword);
        layoutBtn = findViewById(R.id.layoutBtn);
        layoutEmailPassContent = findViewById(R.id.layoutEmailPassContent);
        layoutBtnLogin = findViewById(R.id.layoutBtnLogin);
        imgBtnLogin = findViewById(R.id.imgBtnLogin);
        btnGoogleSignIn = findViewById(R.id.btnSignInGoogle);

        txtViewRegisterUser = findViewById(R.id.txtRegisterUser);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        // Para el inicio de sesion con google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // cuadro de carga
        loadingDialog = new LoadingDialog(this);

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        layoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutEmailPassContent.setVisibility(View.VISIBLE);
                layoutBtn.setVisibility(View.GONE);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutEmailPassContent.setVisibility(View.VISIBLE);
                layoutBtn.setVisibility(View.GONE);
            }
        });

        txtViewRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        layoutBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        imgBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthProvider.getUserSession() != null){
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("ERROR", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        loadingDialog.startLoadingAnimation();
        mAuthProvider.googleLogin(acct)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //Solicitando el id del user
                            String id = mAuthProvider.getUid();
                            // verificando si el usuario existe
                            ckeckUserExist(id);
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            loadingDialog.dissmissDialog();
                            // If sign in fails, display a message to the user.
                            Log.w("ERROR", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Login error", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void ckeckUserExist(String id) {

        // consultando en la bd si el usuario con el id ya se registro
        //addOnSuccessListener = verifica si la traida de datos fue exitosa
        mUsersProvider.getUser(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            //documentSnapshot = es donde se contiene los datos del user en la bd
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //si existe el usuario ya se habia registrado
                if (documentSnapshot.exists()){
                    loadingDialog.dissmissDialog();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }else{
                    String email = mAuthProvider.getEmail();
                    Map<String, Object> map = new HashMap<>();
                    map.put("email", email);
                    User user = new User();
                    user.setEmail(email);
                    user.setId(id);
                    mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingDialog.dissmissDialog();
                            if (task.isSuccessful()){
                                Intent intent = new Intent(MainActivity.this, CompleteProfileActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(MainActivity.this, "Could not save to database", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });
    }

    private void login() {
        String email = inputEmailUser.getText().toString();
        String password = inputPassword.getText().toString();

        if (!email.equals("") && !password.equals("")){
            loadingDialog.startLoadingAnimation();
            mAuthProvider.login(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    loadingDialog.dissmissDialog();
                    if(task.isSuccessful()) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        // limpia historial de ventanas al iniciar sesion
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Wrong email or password", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            Toast.makeText(MainActivity.this, "Oops it seems that something is missing!", Toast.LENGTH_SHORT).show();
        }
    }


}