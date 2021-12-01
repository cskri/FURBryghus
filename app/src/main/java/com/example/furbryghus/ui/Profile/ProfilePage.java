package com.example.furbryghus.ui.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.furbryghus.MainActivityViewModel;
import com.example.furbryghus.R;
import com.example.furbryghus.ui.events.EventDocument;
import com.example.furbryghus.ui.events.EventFragment;
import com.example.furbryghus.ui.events.EventModel;
import com.example.furbryghus.ui.events.ParticipantDocument;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilePage extends AppCompatActivity {
    private Button back;
    private Button signout;
    private MainActivityViewModel viewModel;
    private RecyclerView eventsFirestoreList;
    private FirebaseFirestore db;
    FirestoreRecyclerAdapter adapter;
    private String TAG = "ProfilePage";
    List<String> events;
    private TextView signed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        back = (Button) findViewById(R.id.BackButtonProfile);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        signout = (Button) findViewById(R.id.SignOutButton);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
        signed = findViewById(R.id.eventsSignedUpText);
        if(!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
            TextView hello;
            hello = (TextView)findViewById(R.id.helloText);
            hello.setText("Hello " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        }
        else {
            signed.setText("");
        }
        db = FirebaseFirestore.getInstance();
        eventsFirestoreList = findViewById(R.id.rvProfile);
        DocumentReference docRef = db.collection("eventUsers").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                Log.d("Events OUTPUT", "SUCCESS");
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Events OUTPUT", "DOCUMENT EXISTS");
                        events = document.toObject(EventDocument.class).eventId;
                        buildRecyclerView(document);
                    } else {
                        Log.d("Events OUTPUT", "DOCUMENT NOT EXISTS");
                        signed.setText("");
                    }
                }
                else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }
    private void removeParticipantFromEvent(String eId, String uId){
        DocumentReference docRef = db.collection("events").document(eId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> participants = document.toObject(ParticipantDocument.class).participants;
                        participants.remove(uId);
                        document.getReference().update("participants", participants);

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
                else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void buildRecyclerView(DocumentSnapshot document){
        Query query = db.collection("events").whereIn("id",events).orderBy("date");

        //RecyclerOptions
        FirestoreRecyclerOptions<EventModel> options = new FirestoreRecyclerOptions.Builder<EventModel>().setQuery(query, EventModel.class).build();

        adapter = new FirestoreRecyclerAdapter<EventModel, ProfilePage.ProfileViewHolder>(options) {
            @NonNull
            @Override
            public ProfilePage.ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single_profile, parent, false);
                return new ProfilePage.ProfileViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProfilePage.ProfileViewHolder holder, int position, @NonNull EventModel model) {
                holder.title_profile.setText(model.getName());
                holder.description_profile.setText(model.getDescription());
                holder.date_profile.setText("" + model.getDate());
                holder.price_profile.setText("" + model.getPrice());
                new ProfilePage.DownloadImageTask(holder.image_profile)
                        .execute(model.getImageLink());
                holder.button_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilePage.this);
                        builder.setTitle("Confirm Deletion");
                        builder.setMessage("Confirm that you want to unsign from event");
                        builder.setPositiveButton("UNSIGN", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                events.remove(model.getId());
                                if(events.size() == 0){
                                    document.getReference().delete();
                                }
                                document.getReference().update("eventId", events);
                                removeParticipantFromEvent(model.getId(), FirebaseAuth.getInstance().getUid());
                                finish();
                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
            }
        };
        eventsFirestoreList.setHasFixedSize(true);
        eventsFirestoreList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        eventsFirestoreList.setAdapter(adapter);
        adapter.startListening();
    }
    private class ProfileViewHolder extends RecyclerView.ViewHolder {

        private TextView title_profile;
        private TextView description_profile;
        private TextView date_profile;
        private TextView price_profile;
        private ImageView image_profile;
        private Button button_profile;


        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            title_profile = itemView.findViewById(R.id.title_profile);
            description_profile = itemView.findViewById(R.id.description_profile);
            date_profile = itemView.findViewById(R.id.date_profile);
            price_profile = itemView.findViewById(R.id.price_profile);
            image_profile = itemView.findViewById(R.id.image_profile);
            button_profile = itemView.findViewById(R.id.button_profile);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!(adapter == null)){
            adapter.stopListening();
        }

    }

}