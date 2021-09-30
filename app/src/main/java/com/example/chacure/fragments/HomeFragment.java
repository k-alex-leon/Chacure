package com.example.chacure.fragments;


import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.example.chacure.R;
import com.example.chacure.adapter.ParseAdapter;
import com.example.chacure.models.ParseItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class HomeFragment extends Fragment {


    View mView;
    RecyclerView recyclerViewParseImg;
    ParseAdapter parseAdapter;
    ArrayList<ParseItem> parseItems = new ArrayList<>();
    ProgressBar progressBarParse;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        mView = inflater.inflate(R.layout.fragment_home,container,false);

        recyclerViewParseImg = mView.findViewById(R.id.reciclerParseImage);
        progressBarParse = mView.findViewById(R.id.progressBarParse);

        recyclerViewParseImg.setHasFixedSize(true);
        recyclerViewParseImg.setLayoutManager(new LinearLayoutManager(getContext()));
        parseAdapter = new ParseAdapter(parseItems, getContext());
        recyclerViewParseImg.setAdapter(parseAdapter);

        Content content = new Content();
        content.execute();
        return mView;
    }

    private class Content extends AsyncTask<Void, Void, Void>{

        // este metodo muestra el progressbar indicando la carga de datos
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // muestra el progress
            progressBarParse.setVisibility(View.VISIBLE);
            // inicia la animacion del progressbar
            progressBarParse.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        }

        // una vez cargado se quita la anim del progress
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            // ocultamos el progress
            progressBarParse.setVisibility(View.GONE);
            // finaliza la animacion del progressbar
            progressBarParse.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            // enseñar los cambios que se realizen de la data en el adapter
            parseAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String url = "https://www.placermonticello.com/darse-un-gusto/recetas?gclid=Cj0KCQjw1ouKBhC5ARIsAHXNMI_iU4z_wugzhdY_C3j7m2DKnXwhnuEqEcJebbtC3GpynsrP7LVoIgIaAjeBEALw_wcB";
                Document doc = Jsoup.connect(url).get();

                Elements data = doc.select("div.item-pro");

                Log.d("items", "doc"+ doc);
                Log.d("items", "data"+ data);

                int size = data.size();

                for (int i = 0; i < size; i++){
                    String imgUrl = data.select("a")
                            .select("img")
                            .eq(i)
                            .attr("src");

                    String img = "https://www.placermonticello.com" + imgUrl;

                    String title = data.select("div.linea-pro")
                            .select("h2")
                            .eq(i)
                            .text();

                    parseItems.add(new ParseItem(img , title));
                    Log.d("items", "img: " + img + ". title: "+ title);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}