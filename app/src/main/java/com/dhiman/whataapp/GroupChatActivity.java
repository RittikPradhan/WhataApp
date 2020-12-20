package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        mAuth=FirebaseAuth.getInstance();

        currentUserID=mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef= FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        
        InitializeFields();
        getUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageInfoToDatabase();
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        userMessageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN); 
                return false;
            }
        });

        
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate=((DataSnapshot)iterator.next()).getValue().toString();
            String chatMessage=((DataSnapshot)iterator.next()).getValue().toString();
            String chatName=((DataSnapshot)iterator.next()).getValue().toString();
            String chatTime=((DataSnapshot)iterator.next()).getValue().toString();
            displayTextMessage.append(chatName+" :\n"+chatMessage+"\n"+chatTime+"  "+chatDate+"\n\n\n" );

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void saveMessageInfoToDatabase() {

        String message=userMessageInput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "write first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate= Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM,dd,yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());


            Calendar calForTime= Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm:ss:a");
            currentTime=currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef=GroupNameRef.child(messageKey);
            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void getUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void InitializeFields() {
        mToolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton=findViewById(R.id.send_message_button);
        userMessageInput=findViewById(R.id.input_group_message);
        displayTextMessage=findViewById(R.id.group_chat_text_display);
        mScrollView=findViewById(R.id.my_scroll_view);

    }
}
