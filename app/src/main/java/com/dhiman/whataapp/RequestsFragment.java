package com.dhiman.whataapp;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestLists;
    private DatabaseReference ChatRequestsRef,UsersRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String CurrentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         RequestsFragmentView=inflater.inflate(R.layout.fragment_requests, container, false);

         ChatRequestsRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
         UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
         ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
         mAuth=FirebaseAuth.getInstance();
         CurrentUserID=mAuth.getCurrentUser().getUid();

         myRequestLists=RequestsFragmentView.findViewById(R.id.chat_requests_list);
         myRequestLists.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(CurrentUserID),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {

                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(i).getKey();

                DatabaseReference getTypeRef=getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            String type=dataSnapshot.getValue().toString();
                            if (type.equals("received"))
                            {
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image"))
                                        {

                                            final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);
                                        }


                                            final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                            final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                            requestViewHolder.userName.setText(requestUserName);
                                            requestViewHolder.userStatus.setText("WANTS TO CONNECT WITH YOU");


                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "decline"
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName+"  Chat Requests");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if (i==0)
                                                        {
                                                            ContactsRef.child(CurrentUserID).child(list_user_id).child("Contact")
                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        ContactsRef.child(list_user_id).child(CurrentUserID).child("Contact")
                                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    ChatRequestsRef.child(CurrentUserID).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful())
                                                                                                    {
                                                                                                        ChatRequestsRef.child(list_user_id).child(CurrentUserID)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                        if (i==1)
                                                        {
                                                            ChatRequestsRef.child(CurrentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ChatRequestsRef.child(list_user_id).child(CurrentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "Request Declned", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (type.equals("sent"))
                            {
                                Button request_sent_btn=requestViewHolder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req. Sent");
                                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image"))
                                        {

                                            final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);
                                        }


                                        final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                        requestViewHolder.userName.setText(requestUserName);
                                        requestViewHolder.userStatus.setText("You have Sent request to "+requestUserName);


                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Cancel Chat Request "
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(" Sent Chat Requests");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if (i==0)
                                                        {
                                                            ChatRequestsRef.child(CurrentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ChatRequestsRef.child(list_user_id).child(CurrentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), " Sent Request  Deleted ", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestViewHolder viewHolder=new RequestViewHolder(view);
                return viewHolder;
            }
        };

        myRequestLists.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        Button AcceptButton,DeclineDutton;
        CircleImageView profileImage;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            DeclineDutton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
