package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText InputFeedback;
    Button SubmitfeedBack;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mToolbar=findViewById(R.id.appBarLayout_of_feedback);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Feedback");

        mAuth= FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        currentUserID=mAuth.getCurrentUser().getUid();
        InputFeedback=findViewById(R.id.feedback_input);
        SubmitfeedBack=findViewById(R.id.feedback_submit_btn);








        SubmitfeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedback=InputFeedback.getText().toString();
                if (TextUtils.isEmpty(feedback))
                {
                    Toast.makeText(getApplicationContext(), "Please Type Something ", Toast.LENGTH_SHORT).show();
                }
                else {


                    RootRef.child("Feedback").child(currentUserID).setValue(feedback)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(FeedbackActivity.this, "Thanks for Feedback", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });
    }
}
