package de.androidcrypto.firebaseplayground;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.Message2Model;

public class ChatDatabaseRecyclerViewPaginated4Activity extends AppCompatActivity {

    // https://dpkpradhan649.medium.com/firebase-realtime-database-pagination-in-recyclerview-c3f9a4a7856f
    // funktioniert hat aber keine Live-Updates

    // this one should show the last 10 entries and scroll up to previous ones
    // https://stackoverflow.com/questions/50671653/firebase-database-pagination

    // part 31 of tutorial
    // https://www.youtube.com/watch?v=_oWADTT8zHQ

    // todo reverse view, show LAST 10 entries

    private static final String TAG = "ChatDbRecyclViewPag4";
    // my database is not in default/US location so you need to fill in the reference here
    private static final String DATABASE_REFERENCE = "https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app";
    private static final String NODE_DATABASE_USER = "users";
    private static final String NODE_DATABASE_MESSAGES = "messages2";

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    RecyclerView recyclerView;
    SwipeRefreshLayout mRefreshLayout;
    LinearLayoutManager manager;    //for linear layout
    NewAdapter adapter;
    MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_LOAD = 10;
    private int mCurrentPage = 1;
    List<Message2Model> messagesList = new ArrayList<>();

    ProgressBar progressBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference; // general database reference
    private DatabaseReference mMessagesRef; // messages in messages/roomId
    private LinearLayoutManager mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database_recycler_view_paginated4);

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
        mRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progressBar1);

        messagesList = new ArrayList<>();

        mAdapter = new MessageAdapter(messagesList);
        mLinearLayout = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLinearLayout);
        recyclerView.setAdapter(mAdapter);

        //adapter = new NewAdapter(getApplicationContext());
        //recyclerView.setAdapter(adapter);

        //manager = new LinearLayoutManager(getApplicationContext());


        //recyclerView.setLayoutManager(manager);
        // will add a line between the entries
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mLinearLayout.getOrientation());
        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), manager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);




        loadMessages(true);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                messagesList.clear();
                loadMessages(false);
            }
        });
    }

    private void loadMessages(boolean scrollToLast) {
        Log.i(TAG, "loadMessages, scrollToLast: " + scrollToLast);
        Query query;
        query = mMessagesRef
                .orderByKey()
                .limitToLast(mCurrentPage * TOTAL_ITEMS_LOAD);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i(TAG, "onChildAdded " + snapshot.getValue(Message2Model.class).getMessage());
                Message2Model message2Model = snapshot.getValue(Message2Model.class);
                messagesList.add(message2Model);
                mAdapter.notifyDataSetChanged();
                // scrolling depending on scrollToLast, set to FALSE when scrolling down
                if (scrollToLast) {
                    recyclerView.scrollToPosition(messagesList.size() - 1);
                } else {
                    recyclerView.scrollToPosition(0);
                }
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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