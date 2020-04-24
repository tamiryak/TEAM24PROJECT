package com.example.team24p;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private ListView usersListView;
    private ArrayList<String> items;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRefUsersDetails = mDatabase.getReference().child("Users");
    //private DatabaseReference mRefUsersAndPassword = mDatabase.getReference().child("UsersAndPassword");
    String userNameLoggedIn="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        items = new ArrayList<>();
        items.clear();
        userNameLoggedIn = getIntent().getStringExtra("userNameLoggedIn");

        usersListView = (ListView)findViewById(R.id.userListView);
        mRefUsersDetails.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> usersAll = (Map<String, Object>) dataSnapshot.getValue();
                for (Object key : usersAll.values()) {
                    Map<String, Object> singleUser = (Map<String, Object>) key;
                    for (Object key2 : singleUser.keySet()) {
                        if ((key2.toString().equals("username"))) {
                            String username = singleUser.get("username").toString();
                            items.add(username);
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items);
                usersListView.setAdapter(null);
                usersListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id) {
                String user = adapter.getItemAtPosition(position).toString();
                System.out.println(user); //just to check that click do something

                Intent stuserAdmin = new Intent(getApplicationContext(), userAdminActivity.class);
                stuserAdmin.putExtra("UserNameLoggedIn",userNameLoggedIn);
                stuserAdmin.putExtra("userToEdit",user);
                startActivity(stuserAdmin);


            }
        });


    }
}
