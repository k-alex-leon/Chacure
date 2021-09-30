package com.example.chacure.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chacure.R;
import com.example.chacure.models.ParseItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ParseAdapter extends RecyclerView.Adapter<ParseAdapter.ViewHolder> {

    private ArrayList<ParseItem> parseItems;
    private Context context;

    public ParseAdapter(ArrayList<ParseItem> parseItems, Context context){
        this.parseItems = parseItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ParseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_parse_image,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParseAdapter.ViewHolder holder, int position) {
        ParseItem parseItem = parseItems.get(position);
        holder.txtParseTitle.setText(parseItem.getTitle());
        Picasso.with(context).load(parseItem.getImgUrl()).into(holder.imgParse);
    }

    @Override
    public int getItemCount() {
        return parseItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgParse;
        TextView txtParseTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgParse = itemView.findViewById(R.id.imgViewParse);
            txtParseTitle = itemView.findViewById(R.id.txtTitleImgParse);
        }
    }
}
