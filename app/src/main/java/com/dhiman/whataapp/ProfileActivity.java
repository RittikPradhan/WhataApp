package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompatSideChannelService;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {
    private String receiveUserId,Current_State,senderUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,DeclineMessageRequestButton;

    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NortificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NortificationRef= FirebaseDatabase.getInstance().getReference().child("Nortifications");

        mAuth=FirebaseAuth.getInstance();

        userProfileImage=findViewById(R.id.visit_profile_image);
        userProfileName=findViewById(R.id.visit_profile_name);
        userProfileStatus=findViewById(R.id.visit_profile_status);
        SendMessageRequestButton=findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=findViewById(R.id.decline_message_request_button);
        Current_State="new";


        receiveUserId=getIntent().getExtras().get("visit_user_id").toString();
        senderUserID=mAuth.getCurrentUser().getUid();

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {
        UserRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else
                {
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequest() {
        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiveUserId))
                {
                    String request_type=dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        Current_State="request_sent";
                        SendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if (request_type.equals("received"))
                    {
                        Current_State="request_received";
                        SendMessageRequestButton.setText("Accept Chat Request");

                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiveUserId))
                            {
                                Current_State="friends";
                                SendMessageRequestButton.setText("Remove This Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (!senderUserID.equals(receiveUserId))
        {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiveUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiveUserId).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                SendMessageRequestButton.setText("Send Message");
                                                Current_State="new";

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }

                                        }
                                    });

                        }

                    }
                });
    }

    private void AcceptChatRequest() {

        ContactsRef.child(senderUserID).child(receiveUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        ContactsRef.child(receiveUserId).child(senderUserID).child("Contacts").setValue("Saved")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            ChatRequestRef.child(senderUserID).child(receiveUserId).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                ChatRequestRef.child(receiveUserId).child(senderUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    Current_State="friends";
                                                                                    SendMessageRequestButton.setText("Remove This Contact");

                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiveUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiveUserId).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                SendMessageRequestButton.setText("Send Message");
                                                Current_State="new";
                                                Toasty.normal(ProfileActivity.this,"Request Cancelled Successfully",Toast.LENGTH_SHORT,
                                                        ContextCompat.getDrawable(ProfileActivity.this,R.drawable.toasty)).show();

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }

                                        }
                                    });

                        }

                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiveUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiveUserId).child(senderUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {

                                                HashMap<String ,String > chatNortificatioMap=new HashMap<>();
                                                chatNortificatioMap.put("from",senderUserID);
                                                chatNortificatioMap.put("type","request");

                                                NortificationRef.child(receiveUserId).push()
                                                        .setValue(chatNortificatioMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State="request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
                                                                    Toasty.success(ProfileActivity.this,"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }

                    }
                });
    }
}
