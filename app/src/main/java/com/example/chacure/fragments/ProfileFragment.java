package com.example.chacure.fragments;

import static android.app.Activity.RESULT_OK;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chacure.R;
import com.example.chacure.activities.MainActivity;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.ImageProvider;
import com.example.chacure.providers.UsersProvider;
import com.example.chacure.utils.FileUtil;
import com.example.chacure.utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    View mView;
    TextView txtUserName, txtEmailUser, txtInfoUser;
    UsersProvider mUserProvider;
    AuthProvider mAuthProvider;
    ImageProvider mImageProvider;
    CircleImageView imgCircleProfile, imgCircleEditImageProfile;
    ImageView imgEditUserName, imgEditInfoUser, imgDoneUserName, imgDoneUserInfo, imgLogOut;
    TextInputEditText inputUsername, inputUserInfo;

    String imageProfile;

    File mImageFile;

    private final int GALERY_REQUEST_CODE = 1;

    LoadingDialog loadingDialog;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_profile, container, false);

        mUserProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider();


        txtUserName = mView.findViewById(R.id.txtUserNameProfile);
        txtEmailUser = mView.findViewById(R.id.txtEmailUserProfile);
        txtInfoUser = mView.findViewById(R.id.txtUserInfoProfile);

        imgCircleProfile = mView.findViewById(R.id.imgCircleUserProfile);
        imgCircleEditImageProfile = mView.findViewById(R.id.imgEditImageProfile);
        imgEditUserName = mView.findViewById(R.id.imgEditUserNameProfile);
        imgEditInfoUser = mView.findViewById(R.id.imgEditInfoProfile);
        imgDoneUserName = mView.findViewById(R.id.imgDoneUserNameProfile);
        imgDoneUserInfo = mView.findViewById(R.id.imgDoneInfoProfile);
        imgLogOut = mView.findViewById(R.id.imgLogout);

        inputUsername = mView.findViewById(R.id.inputEditUsername);
        inputUserInfo = mView.findViewById(R.id.inputEditUserInfo);

        // cuadro de carga
        loadingDialog = new LoadingDialog(getActivity());

        imgLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        imgEditUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserName();
            }
        });

        imgEditInfoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserInfo();
            }
        });

        imgCircleEditImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        getUser();

        return mView;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_REQUEST_CODE && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(getContext(),data.getData());
                imgCircleProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
                selectImgProfile();
            }catch (Exception e){
                Log.d("ERROR", "Error with onActivityResult" + e.getMessage());
                Toast.makeText(getContext(), "Error with onActivityResult", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectImgProfile() {
        loadingDialog.startLoadingAnimation();
        mImageProvider.saveImage(getContext(), mImageFile)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            mImageProvider.getmStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String urlProfile = uri.toString();
                                    User user = new User();
                                    user.setId(mAuthProvider.getUid());
                                    user.setImageProfile(urlProfile);
                                    updateImgProfile(user);
                                }
                            });
                        }else{
                            loadingDialog.dissmissDialog();
                            Toast.makeText(getContext(), "Error saving image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateImgProfile(User user) {

        mUserProvider.updateImg(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                loadingDialog.dissmissDialog();
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Image saved.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void editUserName() {
        txtUserName.setVisibility(View.GONE);
        imgEditUserName.setVisibility(View.GONE);
        inputUsername.setVisibility(View.VISIBLE);
        imgDoneUserName.setVisibility(View.VISIBLE);

        String userName = txtUserName.getText().toString();
        inputUsername.setText(userName);

        imgDoneUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String newInfoUser = txtInfoUser.getText().toString();
                    String newUserName = inputUsername.getText().toString();
                    loadingDialog.startLoadingAnimation();

                if (!newUserName.isEmpty()) {
                    if (newUserName.length() < 30) {
                        setInfoProfile(newUserName, newInfoUser);

                        inputUsername.setVisibility(View.GONE);
                        imgDoneUserName.setVisibility(View.GONE);
                        txtUserName.setVisibility(View.VISIBLE);
                        txtUserName.setText(newUserName);
                        imgEditUserName.setVisibility(View.VISIBLE);

                    }else{
                        loadingDialog.dissmissDialog();
                        Toast.makeText(getContext(), "Maximum 30 characters", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    loadingDialog.dissmissDialog();
                    Toast.makeText(getContext(), "This field cannot be blank.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void editUserInfo() {
        txtInfoUser.setVisibility(View.GONE);
        imgEditInfoUser.setVisibility(View.GONE);
        inputUserInfo.setVisibility(View.VISIBLE);
        imgDoneUserInfo.setVisibility(View.VISIBLE);

        String infoUser = txtInfoUser.getText().toString();
        inputUserInfo.setText(infoUser);

        imgDoneUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newInfoUser = inputUserInfo.getText().toString();
                String newUserName = txtUserName.getText().toString();
                loadingDialog.startLoadingAnimation();

                if(!newInfoUser.isEmpty()) {

                    if (newInfoUser.length() < 20){
                        setInfoProfile(newUserName, newInfoUser);

                        inputUserInfo.setVisibility(View.GONE);
                        imgDoneUserInfo.setVisibility(View.GONE);
                        txtInfoUser.setVisibility(View.VISIBLE);
                        txtInfoUser.setText(newInfoUser);
                        imgEditInfoUser.setVisibility(View.VISIBLE);
                    }
                    else{
                        loadingDialog.dissmissDialog();
                        Toast.makeText(getContext(), "Maximum 20 characters", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    loadingDialog.dissmissDialog();
                    Toast.makeText(getContext(), "This field cannot be blank.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setInfoProfile(String newUserName, String newInfoUser){
        User user2 = new User();
        user2.setUserName(newUserName);
        user2.setInfoUser(newInfoUser);
        user2.setId(mAuthProvider.getUid());
        updateData(user2);
    }


    private void updateData(User user2) {

        // if (mDialog.isShowing()){mDialog.show();}

        mUserProvider.updateInfo(user2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingDialog.dissmissDialog();
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Data updated.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void getUser() {
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    if (documentSnapshot.contains("userName")) {
                        String userName = documentSnapshot.getString("userName");
                        txtUserName.setText(userName);
                    }
                    if (documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        txtEmailUser.setText(email);
                    }
                    if (documentSnapshot.contains("infoUser")) {
                        String infoUser = documentSnapshot.getString("infoUser");
                        txtInfoUser.setText(infoUser);
                    }
                    // image profile
                    if (documentSnapshot.contains("imageProfile")){
                        imageProfile = documentSnapshot.getString("imageProfile");
                        if (imageProfile != null){
                            if(!imageProfile.isEmpty()){
                                Picasso.with(getContext()).load(imageProfile).into(imgCircleProfile);
                            }
                        }
                    }


                }
            }
        });
    }

    private void logout() {
        mUserProvider.updateOnline(mAuthProvider.getUid(), false);
        mAuthProvider.logout();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}