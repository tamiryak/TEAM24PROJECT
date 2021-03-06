package com.example.team24p;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class chatActivity extends AppCompatActivity {


    private EditText editText;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private String userNameLoggedIn, userSelected;
    public MemberData data;
    public String selctedKey;
    public ArrayList<Message> messageArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //initialize variables
        mRef = mDatabase.getReference().child("messages");
        editText = (EditText) findViewById(R.id.editText);
        userNameLoggedIn = getIntent().getStringExtra("userNameLoggedIn");
        userSelected = getIntent().getStringExtra("userSelected");
        userSelected = userSelected.split("@",1)[0];
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(null);
        messagesView.setAdapter(messageAdapter);
        data = new MemberData(userSelected, getRandomColor());

        //get data from message db about the relevant user - first we check what conversation we in
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> messageTable = (HashMap<String, Object>) dataSnapshot.getValue();
                for (String key : messageTable.keySet()) {
                    Map<String, Object> value = (HashMap<String, Object>) messageTable.get(key);
                    //check if user is user 1 or user 2 and the other is user is the second one
                    if ((value.get("user1").toString().equals(userNameLoggedIn) && value.get("user2").toString().equals(userSelected)) || (value.get("user1").toString().equals(userSelected) && value.get("user2").toString().equals(userNameLoggedIn))) {
                        selctedKey = key; //return the key of the conversation
                    }
                }
                //if its the first time user initiate conversation with the user
                //create a table properties for the conversation
                if (selctedKey == null) {
                    selctedKey = mRef.push().getKey();
                    final Map<String, String> userData2 = new HashMap<String, String>();
                    userData2.put("user1", userNameLoggedIn);
                    userData2.put("user2", userSelected);
                    DatabaseReference refChildKey = mRef.child(selctedKey);
                    refChildKey.setValue(userData2);
                }
                //get the message list if they have one
                    mRef.child(selctedKey).child("messageList").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot!=null) {
                                final Map<String, Object> value = (HashMap) dataSnapshot.getValue();
                                if(value!=null) {
                                    //if i have unread message and enter the chat it set it to read
                                    for (String key : value.keySet()) {
                                        Map<String, String> value2 = (Map<String, String>) value.get(key);
                                        if (!value2.get("sender").equals(userNameLoggedIn)) {
                                            DatabaseReference refChildKey2 = mRef.child(selctedKey).child("messageList").child(key).child("read");
                                            refChildKey2.setValue("True");
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                //if new message arrived from the other user or from me
                    mRef.child(selctedKey).child("messageList").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if(dataSnapshot!=null) {
                                final Map<String, String> value = (HashMap) dataSnapshot.getValue();
                                String text = value.get("text");
                                boolean belg = userNameLoggedIn.equals(value.get("sender"));
                                onMessage(text, belg); //send it to function that set the message on the list by order
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(View view){ //if i press send button it push the information to the db
        final String msg = editText.getText().toString();
        if (msg.length() > 0) { //check if the user not enter nothing and press the button
            editText.getText().clear();

       // onMessage(msg,true);
        final Map<String, String> userData = new HashMap<String, String>();
        userData.put("read","False");
        userData.put("sender",userNameLoggedIn);
        userData.put("text",msg);
        userData.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        String key = mRef.push().getKey();

        DatabaseReference refChildKey2 = mRef.child(selctedKey).child("messageList").child(key); //put the message into the relevant conversation
        refChildKey2.setValue(userData);
        }
    }

    public void onMessage(final String msg, final boolean belg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Message message = new Message(msg,data,belg);
                messageAdapter.add(message);
                // scroll the ListView to the last added element
                messagesView.setSelection(messagesView.getCount() - 1); //set the message to the most new message - first we see
            }
        });
    }

    //set random color of the other user - just for view
    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}