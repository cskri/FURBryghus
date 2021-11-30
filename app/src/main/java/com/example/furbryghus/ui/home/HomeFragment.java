package com.example.furbryghus.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furbryghus.R;
import com.example.furbryghus.databinding.FragmentHomeBinding;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.InputStream;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecyclerView newsFirestoreList;
    private FirebaseFirestore db;
    FirestoreRecyclerAdapter adapter;

    SliderView sliderView;
    int[] images = {R.drawable.slider_hero,
            R.drawable.slider_bygning,
            R.drawable.slider_processen,
            R.drawable.slider_sommerkoncert,
            R.drawable.slider_tapning};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        newsFirestoreList = root.findViewById(R.id.rvNews);

        //Query
        Query query = db.collection("news").orderBy("date");
        //RecyclerOptions
        FirestoreRecyclerOptions<HomeModel> options = new FirestoreRecyclerOptions.Builder<HomeModel>().setQuery(query, HomeModel.class).build();

        adapter = new FirestoreRecyclerAdapter<HomeModel, HomeViewHolder>(options) {
            @NonNull
            @Override
            public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_signle_news, parent, false);
                return new HomeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HomeViewHolder holder, int position, @NonNull HomeModel model) {
                holder.title_news.setText(model.getTitle());
                holder.description_news.setText(model.getDescription());
                holder.date_news.setText("" + model.getDate());
                new DownloadImageTask(holder.news_image)
                        .execute(model.getImageLink());
                holder.button_news.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), NewsIntent.class);
                        i.putExtra("ID",model.getId());
                        v.getContext().startActivity(i);
                    }
                });
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.d("Firestore Error", e.getMessage());
            }


        };
        newsFirestoreList.setHasFixedSize(true);
        newsFirestoreList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        newsFirestoreList.setAdapter(adapter);



        /* SliderView */
        sliderView = root.findViewById(R.id.image_slider);
        SliderAdapter sliderAdapter = new SliderAdapter(images);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();

        return root;

    }

    public void goToNews() {
        Intent intent = new Intent(HomeFragment.this.getActivity(), NewsIntent.class);
        HomeFragment.this.startActivity(intent);
    }


    private class HomeViewHolder extends RecyclerView.ViewHolder {

        private TextView title_news;
        private TextView description_news;
        private TextView date_news;
        private ImageView news_image;
        private Button button_news;


        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            title_news = itemView.findViewById(R.id.title_event);
            description_news = itemView.findViewById(R.id.description_event);
            news_image = itemView.findViewById(R.id.image_event);
            button_news = itemView.findViewById(R.id.button_event);
            date_news = itemView.findViewById(R.id.date_event);

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}