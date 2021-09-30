package com.example.chacure.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chacure.R;
import com.example.chacure.activities.SearchUserActivity;
import com.example.chacure.adapter.ChatAdapter;
import com.example.chacure.models.Chat;
import com.example.chacure.models.User;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.ChatsProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

public class ChatsFragment extends Fragment {
    
    View mView;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    ChatsProvider mChatProvider;
    AuthProvider mAuthProvider;
    ChatAdapter mChatAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ChatsFragment(){}

    public static ChatsFragment newInstance(String param1, String param2){
        ChatsFragment fragment = new ChatsFragment();
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
    public void onStart() {
        super.onStart();
        Query query = mChatProvider.getAllChats(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();
        mChatAdapter = new ChatAdapter(options, getContext());
        recyclerView.setAdapter(mChatAdapter);
        mChatAdapter.startListening();
    }

    // cuando sale de la app deja de escuchar los cambios de la bd
    @Override
    public void onStop() {
        super.onStop();
        mChatAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatAdapter.getmListener() != null){
            mChatAdapter.getmListener().remove();
        }
        if (mChatAdapter.getmListener2() != null){
            mChatAdapter.getmListener2().remove();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_chat,container,false);

        mChatProvider = new ChatsProvider();
        mAuthProvider = new AuthProvider();

        recyclerView = mView.findViewById(R.id.recyclerViewChats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        fab = mView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchUserActivity.class);
                startActivity(intent);
            }
        });

        return mView;
    }
}
