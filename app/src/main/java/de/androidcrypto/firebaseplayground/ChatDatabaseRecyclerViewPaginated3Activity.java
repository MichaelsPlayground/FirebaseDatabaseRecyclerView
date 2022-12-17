package de.androidcrypto.firebaseplayground;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.Message2Model;

public class ChatDatabaseRecyclerViewPaginated3Activity extends AppCompatActivity {

    // https://dpkpradhan649.medium.com/firebase-realtime-database-pagination-in-recyclerview-c3f9a4a7856f
    // funktioniert hat aber keine Live-Updates

    private static final String TAG = "ChatDbRecyclViewPag3";
    // my database is not in default/US location so you need to fill in the reference here
    private static final String DATABASE_REFERENCE = "https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app";
    private static final String NODE_DATABASE_USER = "users";
    private static final String NODE_DATABASE_MESSAGES = "messages2";

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    RecyclerView recyclerView;
    LinearLayoutManager manager;    //for linear layout
    NewAdapter adapter;

    String lastKey = "", lastNode = "";
    boolean isMaxData = false, isScrolling = false;
    int ITEM_LOAD_COUNT = 10;
    ProgressBar progressBar;

    int currentItems, tottalItems, scrolledoutItems;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference; // general database reference
    private DatabaseReference mMessagesRef; // messages in messages/roomId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database_recycler_view_paginated3);

        // init auth & database
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        authUserId = mFirebaseAuth.getCurrentUser().getUid();
        receiveUserId = "zzzzz"; // fixed here for demo purposes
        roomId = getRoomId(authUserId, receiveUserId);
        Log.i(TAG, "roomId: " + roomId);
        mDatabaseReference = FirebaseDatabase.getInstance(DATABASE_REFERENCE).getReference();
        mMessagesRef = mDatabaseReference.child(NODE_DATABASE_MESSAGES).child(roomId);
        // the mMessagesRef is set when the own user is signed in and a chat partner is given

        recyclerView = findViewById(R.id.recycler_view_user);
        progressBar = findViewById(R.id.progressBar1);

        getLastKeyFromFirebase();

        manager = new LinearLayoutManager(getApplicationContext());
        adapter = new NewAdapter(getApplicationContext());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        // will add a line between the entries
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                manager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        getMessages();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = manager.getChildCount();
                tottalItems = manager.getItemCount();
                scrolledoutItems = manager.findFirstVisibleItemPosition();

                if( isScrolling && currentItems + scrolledoutItems == tottalItems)
                {
                    //  Toast.makeText(getContext(), "fetch data", Toast.LENGTH_SHORT).show();
                    isScrolling = false;
                    //fetch data
                    progressBar.setVisibility(View.VISIBLE);
                    getMessages();
                }
            }
        });

        // return UserFragment;
    }

    private void getMessages()
    {
        Log.i(TAG, "getUsers with last_node: " + lastNode);
        Log.i(TAG, "getUser isMaxData: " + isMaxData);
        if(!isMaxData) // 1st fasle
        {
            Query query;
            if (TextUtils.isEmpty(lastNode))
                query = mMessagesRef
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);
            else
                query = mMessagesRef
                        .orderByKey()
                        .startAt(lastNode)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.hasChildren())
                    {
                        Log.i(TAG, "getUsers snapshot.hasChildren");
                        List<Message2Model> newMessages = new ArrayList<>();
                        Message2Model newUser;
                        for (DataSnapshot userSnapshot : snapshot.getChildren())
                        {
                            // this is to include the key
                            newUser = userSnapshot.getValue(Message2Model.class);
                            // this is to include the key
                            newUser.key = userSnapshot.getKey();
                            newMessages.add(newUser);
                            Log.i(TAG, "newUsers: " + userSnapshot.getValue(Message2Model.class).getMessage());
                        }

                        //last_node =newUsers.get(newUsers.size()-1).getMessageTimeString();    //10  if it greater than the toatal items set to visible then fetch data from server
                        lastNode = newMessages.get(newMessages.size()-1).getKey();    //10  if it greater than the toatal items set to visible then fetch data from server
                        Log.i(TAG, "getUsers last_node: " + lastNode);
                        if(!lastNode.equals(lastKey)) {
                            Log.i(TAG, "getUsers last_node. NOT equals(last_key) : " + lastKey);
                            newMessages.remove(newMessages.size() - 1);    // 19,19 so to renove duplicate removeone value
                        }
                        else {
                            lastNode = "end";
                            Log.i(TAG, "getUsers last_node. equals(last_key) last_node: " + lastNode);
                        }
                        // Toast.makeText(getContext(), "last_node"+last_node, Toast.LENGTH_SHORT).show();
                        adapter.addAll(newMessages);
                        adapter.notifyDataSetChanged();
                    }
                    else   //reach to end no further child avaialable to show
                    {
                        Log.i(TAG, "getUsers snapshot.hasChildren FALSE, isMaxData=true");
                        isMaxData = true;
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });
        }
        else
        {
            progressBar.setVisibility(View.GONE); //if data end
        }
    }

    private void getLastKeyFromFirebase()
    {
        Query getLastKey= mMessagesRef
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot lastkey : snapshot.getChildren())
                    lastKey = lastkey.getKey();
                    //last_key=lastkey.getValue(Message2Model.class).getMessageTimeString();
                    Log.i(TAG, "getLastKeyFromFirebase last_key: " + lastKey);
                //   Toast.makeText(getContext(), "last_key"+last_key, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getApplicationContext(), "can not get last key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + "_" + a;
        else return a + "_" + b;
    }
}