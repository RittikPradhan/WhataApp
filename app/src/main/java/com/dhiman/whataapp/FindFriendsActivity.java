package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList=findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar=findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        Alerter.create(this)
                .setTitle("Here You all users are Visible")
                .setText("You can send request to anyone availiable here")
                .setIcon(R.drawable.find_friend_alerter)
                .setBackgroundColorInt(Color.BLUE)
                .setDuration(3000)
                .enableSwipeToDismiss()
                .enableProgress(true)
                .setProgressColorInt(Color.GREEN)
                .setOnHideListener(new OnHideAlertListener() {
                    @Override
                    public void onHide() {
                        Toasty.warning(FindFriendsActivity.this,"Nice Boy!", Toast.LENGTH_LONG).show();
                    }
                })

                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UsersRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adaptor=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id=getRef(position).getKey();
                        Intent profileIntent=new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;

            }
        };

        FindFriendsRecyclerList.setAdapter(adaptor);
        adaptor.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }
}
