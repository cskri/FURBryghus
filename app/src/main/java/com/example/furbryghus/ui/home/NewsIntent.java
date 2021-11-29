package com.example.furbryghus.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furbryghus.R;
import com.google.firebase.auth.FirebaseAuth;

public class NewsIntent extends AppCompatActivity {
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_intent);
        back = (Button) findViewById(R.id.backButtonNews);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

    });
}

}
