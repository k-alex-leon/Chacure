package com.example.chacure.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.chacure.R;
import com.example.chacure.adapter.FriendAdapter;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.UsersProvider;
import com.example.chacure.utils.ViewedMessageHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;


public class SearchUserActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView searchViewUsers;
    ImageView imageViewGoBack;
    RecyclerView recyclerViewUsersSearch;

    FriendAdapter friendAdapter;

    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        searchViewUsers = findViewById(R.id.searchViewUsers);
        searchViewUsers.setOnQueryTextListener(this);

        imageViewGoBack = findViewById(R.id.arrowGoBack);
        imageViewGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerViewUsersSearch =findViewById(R.id.recyclerViewUserSearch);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewUsersSearch.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, SearchUserActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, SearchUserActivity.this);
    }

    // metodo para realizar la consulta
    private void searByEmail(String emailSearch){
        Query query = mUsersProvider.getUserByEmail(emailSearch);
        FirestoreRecyclerOptions<User> options =
                new FirestoreRecyclerOptions.Builder<User>()
                        .setQuery(query,User.class)
                        .build();
        friendAdapter = new FriendAdapter(options, this);
        friendAdapter.notifyDataSetChanged();
        recyclerViewUsersSearch.setAdapter(friendAdapter);
        // escucha los cambios de la bd
        friendAdapter.startListening();
    }


    // este metodo se ejecuta al presionar enter en la busqueda
    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query != null){
            searByEmail(query);
        }
        return true;
    }
    // escucha los cambios que se realizan en el searchview
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText != null){
            searByEmail(newText);
        }
        return true;
    }

}