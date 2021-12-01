package com.example.furbryghus.ui.beers;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furbryghus.R;
import com.example.furbryghus.databinding.FragmentBeersBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.InputStream;

public class BeerFragment extends Fragment {

    private static final String TAG = "StartBeerDetailIntent";
    private BeerViewModel beerViewModel;
    private FragmentBeersBinding binding;
    private RecyclerView mFirestoreList;
    private FirebaseFirestore db;
    private TextView musicLabel;
    FirestoreRecyclerAdapter adapter;
    Button scanButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        beerViewModel =
                new ViewModelProvider(this).get(BeerViewModel.class);
        binding = FragmentBeersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        scanButton = root.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                intentIntegrator.setPrompt("For flash use volume up key");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setCaptureActivity(Capture.class);
                intentIntegrator.forSupportFragment(BeerFragment.this).initiateScan();
            }
        });

        db = FirebaseFirestore.getInstance();
        mFirestoreList = root.findViewById(R.id.event_list);

        //Query
        Query query = db.collection("beers").orderBy("name");
        //RecyclerOptions
        FirestoreRecyclerOptions<BeerModel> options = new FirestoreRecyclerOptions.Builder<BeerModel>().setQuery(query,BeerModel.class).build();
        adapter = new FirestoreRecyclerAdapter<BeerModel, BeerViewHolder>(options) {
            @NonNull
            @Override
            public BeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single_beer,parent,false);
                return new BeerViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull BeerViewHolder holder, int position, @NonNull BeerModel model)
            {
                holder.list_name.setText(model.getName());
                holder.list_type.setText(model.getType());
                holder.list_proof.setText("" + model.getProof() + "%");
                new DownloadImageTask(holder.beer_image)
                        .execute(model.getImageLink());
                holder.list_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent i = new Intent(v.getContext(), BeerDetails.class);
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
        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        mFirestoreList.setAdapter(adapter);

        return root;



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (intentResult.getContents() != null){
            DocumentReference docRef = db.collection("beers").document(intentResult.getContents());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Intent i = new Intent(getContext(), BeerDetails.class);
                            i.putExtra("ID",intentResult.getContents());
                            getContext().startActivity(i);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("No Beer Found");
                            builder.setMessage("The scanned item is not a beer from FUR Bryghus");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                    else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        else{
            Toast.makeText(getContext(), "you did not scan anything",Toast.LENGTH_SHORT).show();
        }
    }




    private class BeerViewHolder extends RecyclerView.ViewHolder{

        private TextView list_name;
        private TextView list_type;
        private TextView list_proof;
        private ImageView beer_image;
        private Button list_button;

        public BeerViewHolder(@NonNull View itemView) {
            super(itemView);
            list_name = itemView.findViewById(R.id.list_name);
            list_type = itemView.findViewById(R.id.list_type);
            list_proof = itemView.findViewById(R.id.list_proof);
            beer_image = itemView.findViewById(R.id.beer_image);
            list_button = itemView.findViewById(R.id.list_button);

        }
    }
    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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