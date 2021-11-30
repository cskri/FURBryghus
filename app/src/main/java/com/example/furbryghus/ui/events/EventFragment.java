package com.example.furbryghus.ui.events;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furbryghus.R;
import com.example.furbryghus.databinding.FragmentEventsBinding;
import com.example.furbryghus.ui.beers.BeerDetails;
import com.example.furbryghus.ui.home.HomeFragment;
import com.example.furbryghus.ui.home.HomeModel;
import com.example.furbryghus.ui.home.NewsIntent;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventFragment extends Fragment {

    private EventViewModel eventViewModel;
    private FragmentEventsBinding binding;
    private RecyclerView eventsFirestoreList;
    private FirebaseFirestore db;
    FirestoreRecyclerAdapter adapter;
    private String TAG = "EventFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        eventViewModel =
                new ViewModelProvider(this).get(EventViewModel.class);

        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        eventsFirestoreList = root.findViewById(R.id.event_list);

        //Query
        Query query = db.collection("events").orderBy("date");
        //RecyclerOptions
        FirestoreRecyclerOptions<EventModel> options = new FirestoreRecyclerOptions.Builder<EventModel>().setQuery(query, EventModel.class).build();

        adapter = new FirestoreRecyclerAdapter<EventModel, EventFragment.EventViewHolder>(options) {
            @NonNull
            @Override
            public EventFragment.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single_event, parent, false);
                return new EventFragment.EventViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull EventFragment.EventViewHolder holder, int position, @NonNull EventModel model) {
                holder.title_event.setText(model.getName());
                holder.description_event.setText(model.getDescription());
                holder.date_event.setText("" + model.getDate());
                holder.price_event.setText("" + model.getPrice());
                new EventFragment.DownloadImageTask(holder.image_event)
                        .execute(model.getImageLink());
                holder.button_event.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Guest user");
                            builder.setMessage("You are in here as a guest. Only registered users can sign up. Please login or create a user");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirm Signup");
                            builder.setMessage("Confirm Your signup to the event");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DocumentReference docRef = db.collection("eventUsers").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    List<String> events = document.toObject(EventDocument.class).eventId;
                                                    events.add(model.getId());
                                                    document.getReference().update("eventId", events);
                                                    // Add user id to existing array
                                                    addParticipantToEvent(model.getId(),FirebaseAuth.getInstance().getUid());
                                                } else {
                                                    //Create new document for the user
                                                    Map<String, Object> eventUsers = new HashMap<>();
                                                    eventUsers.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                    List<String> events = new ArrayList<String>();
                                                    events.add(model.getId());
                                                    eventUsers.put("eventId", events);
                                                    db.collection("eventUsers").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .set(eventUsers)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getActivity(), "You successfully signed up for the event", Toast.LENGTH_SHORT).show();
                                                                    addParticipantToEvent(model.getId(),FirebaseAuth.getInstance().getUid());
                                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error adding document", e);
                                                                }
                                                            });
                                                }
                                            }
                                            else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.d("Firestore Error", e.getMessage());
            }


        };
        eventsFirestoreList.setHasFixedSize(true);
        eventsFirestoreList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        eventsFirestoreList.setAdapter(adapter);

        return root;
    }
    private void addParticipantToEvent(String eId, String uId){
        DocumentReference docRef = db.collection("events").document(eId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> participants = document.toObject(ParticipantDocument.class).participants;
                        participants.add(uId);
                        document.getReference().update("participants", participants);
                        // Add user id to existing array

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private class EventViewHolder extends RecyclerView.ViewHolder {

        private TextView title_event;
        private TextView description_event;
        private TextView date_event;
        private TextView price_event;
        private ImageView image_event;
        private Button button_event;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title_event = itemView.findViewById(R.id.title_event);
            description_event = itemView.findViewById(R.id.description_event);
            date_event = itemView.findViewById(R.id.date_event);
            price_event = itemView.findViewById(R.id.price_event);
            image_event = itemView.findViewById(R.id.image_event);
            button_event = itemView.findViewById(R.id.button_event);
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
}