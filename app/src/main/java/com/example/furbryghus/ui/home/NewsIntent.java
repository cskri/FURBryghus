package com.example.furbryghus.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.furbryghus.R;
import com.example.furbryghus.ui.beers.BeerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class NewsIntent extends AppCompatActivity {
    TextView mTitle;
    TextView mTitle2;
    TextView mDate;
    TextView mContent;
    ImageView mImage;
    String id;
    String TAG = "NewsContent";
    Button back;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_intent);
        back = (Button) findViewById(R.id.backButtonNews);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitle = findViewById(R.id.newsTitle);
        mTitle2 = findViewById(R.id.newsTitle2);
        mDate = findViewById(R.id.newsDate);
        mContent = findViewById(R.id.newsContent);
        mImage = findViewById(R.id.newsImage);

        Intent i = getIntent();
        id = i.getStringExtra("ID");

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("news").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        mTitle.setText(document.get("title").toString());
                        mTitle2.setText(document.get("title").toString());
                        mDate.setText(document.getDate("date").toString());
                        mContent.setText(document.get("content").toString());
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

}
