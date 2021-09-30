package com.example.chacure.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chacure.R;
import com.example.chacure.models.Message;
import com.example.chacure.providers.AuthProvider;
import com.example.chacure.providers.MessagesProvider;
import com.example.chacure.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessagesAdapter extends FirestoreRecyclerAdapter<Message, MessagesAdapter.ViewHolder> {

    Context context;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    MessagesProvider mMessageProvider;

    public MessagesAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position, @NonNull Message message) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        final String messageId = documentSnapshot.getId();
        holder.txtMessage.setText(message.getMessage());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String time = simpleDateFormat.format(message.getTimestamp());
        holder.txtDateMessage.setText(time);

        if (message.getIdSender().equals(mAuthProvider.getUid())){
            // modificando caracteristicas del layout
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(150,0,0,0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30,0,25,5);
            holder.contentMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout));
            holder.imgDoubleCheck.setVisibility(View.VISIBLE);

            // validando si el mensaje fue visto para cambiar el icono de color
            if (message.isViewed()){
                holder.imgDoubleCheck.setImageResource(R.drawable.ic_double_check_blue);
            }else{
                //caso contrario envia predeterminado
                holder.imgDoubleCheck.setImageResource(R.drawable.ic_double_check_gray);
            }

        }else{

            // modificando caracteristicas del layout
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0,0,150,0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30,0,25,5);
            holder.contentMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout_gray));
            holder.imgDoubleCheck.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_message,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View viewHolder;

        TextView txtMessage, txtDateMessage;
        ImageView imgDoubleCheck;
        LinearLayout linearLayoutMessage, contentMessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mAuthProvider = new AuthProvider();
            mUserProvider = new UsersProvider();
            mMessageProvider = new MessagesProvider();

            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtDateMessage = itemView.findViewById(R.id.txtDateMessage);
            imgDoubleCheck = itemView.findViewById(R.id.imgDoubleCheck);
            linearLayoutMessage = itemView.findViewById(R.id.linearLayoutMessage);
            contentMessage = itemView.findViewById(R.id.contentMessage);

                    viewHolder = itemView;
        }
    }
}
