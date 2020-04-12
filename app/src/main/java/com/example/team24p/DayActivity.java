package com.example.team24p;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ListActivity;
import android.app.usage.UsageEvents;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DayActivity extends AppCompatActivity {
    //private static final String TAG = "FireLog";
    private ListView mMainList;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mDatabase.getReference().child("Events");

    //private EventListAdapter eventListAdapter;
    private List<Events> eventsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        mMainList = (ListView) findViewById(R.id.listEv);
        String year = getIntent().getStringExtra("year");
        String month = getIntent().getStringExtra("month");
        String dayOfMonth = getIntent().getStringExtra("dayOfMonth");
        final String groundName = getIntent().getStringExtra("markerName");
        final String date = dayOfMonth + "/" + month + "/" + year;
/*
        eventsList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(eventsList);
        mMainList.setHasFixedSize(true);
        mMainList.setLayoutManager(new LinearLayoutManager(this));
        mMainList = (RecyclerView) findViewById(R.id.event_list);
        mMainList.setAdapter(eventListAdapter);
*/

        final ArrayList<Events> eventsArrayList = new ArrayList<>();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Map<String, String>> events = (ArrayList<Map<String, String>>) dataSnapshot.getValue();
                for (Map<String, String> entry : events) {
                    Events event = new Events();
                    for (String key : entry.keySet()) {
                        for (int i = 0; i < 4; i++) {
                            String value = entry.get(key);
                            System.out.println(key + ":" + value);
                            switch (key) {
                                case "date":
                                    event.setDate(value);

                                    break;
                                case "ground":
                                    event.setGround(value);
                                    break;
                                case "hour":
                                    event.setHour(value);

                                    break;
                                case "username":
                                    event.setUsername(value);

                                    break;

                            }

                        }

                    }
                    eventsArrayList.add(event);
                }


                ArrayList<String> items = new ArrayList<>();
                for(Events ev : eventsArrayList){
                    if((ev.getDate().equals(date)) && (ev.getGround().equals(groundName))){
                        items.add(ev.getHour());
                    }
                }

                ArrayAdapter<String>adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items);
                mMainList.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
