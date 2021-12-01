package com.example.furbryghus.ui.beers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.furbryghus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BeerDetails extends AppCompatActivity {
    TextView mName;
    TextView mType;
    TextView mProof;
    TextView mEmission;
    TextView mSize;
    TextView mDescription;
    ImageView mImage;
    String id;
    String TAG = "BeerDetails";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_details);

        mName = findViewById(R.id.beerName);
        mType = findViewById(R.id.beerType);
        mProof = findViewById(R.id.beerProof);
        mEmission = findViewById(R.id.beerEmission);
        mSize = findViewById(R.id.beerSize);
        mDescription = findViewById(R.id.beerDescription);
        mImage = findViewById(R.id.beerImage);

        Intent i = getIntent();
        id = i.getStringExtra("ID");

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("beers").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        mName.setText(document.get("name").toString());
                        mDescription.setText(document.get("description").toString());
                        mType.setText(document.get("type").toString());
                        String proof = document.get("proof").toString() + "%";
                        mProof.setText(proof);
                        String emission = document.get("carbonEmission").toString() + "g";
                        mEmission.setText(emission);
                        String size = document.get("size").toString() + "cl";
                        mSize.setText(size);
                        new BeerFragment.DownloadImageTask(mImage)
                                .execute(document.get("imageLink").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    public void backButton(View view) {
        onBackPressed();
    }
}