package de.androidcrypto.firebaseplayground;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.Message2Model;
import de.androidcrypto.firebaseplayground.models.MessageModel;
import de.androidcrypto.firebaseplayground.models.UserModel;

public class ChatDatabaseRecyclerViewPaginated2Activity extends AppCompatActivity {

    // see https://github.com/Shajeel-Afzal/Firebase-Realtime-Database-Pagination-Example
    // https://blog.shajeelafzal.com/firebase-realtime-database-pagination-guide-using-recyclerview/

    private static final String TAG = "ChatDbRecyclViewPag2";
    // my database is not in default/US location so you need to fill in the reference here
    private static final String DATABASE_REFERENCE = "https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app";
    private static final String NODE_DATABASE_USER = "users";
    private static final String NODE_DATABASE_MESSAGES = "messages2";

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private RecyclerView mRV;
    private UserRVAdapter mAdapter;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading = false;
    private int mPostsPerPage = 10;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference; // general database reference
    private DatabaseReference mMessagesRef; // messages in messages/roomId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database_recycler_view_paginated2);

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

        mRV = findViewById(R.id.rv);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRV.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRV.getContext(),
                mLayoutManager.getOrientation());
        mRV.addItemDecoration(dividerItemDecoration);

        mAdapter = new UserRVAdapter();
        mRV.setAdapter(mAdapter);

        getUsers(null);
        //getUsers("1671237589127");

        mRV.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && mTotalItemCount <= (mLastVisibleItemPosition + mPostsPerPage)) {
                    getUsers(mAdapter.getLastItemId());
                    mIsLoading = true;
                    Log.i(TAG, "onScrolled isLoading");
                }
            }
        });
    }

    //private void getUsers(String nodeId) {
    private void getUsers(String nodeId) {
        Query query;
        Log.i(TAG, "getUsers nodeId: " + nodeId);
        if (nodeId == null) {
            query = mMessagesRef
                    .orderByKey()
                    .limitToFirst(mPostsPerPage);
            Log.i(TAG, "query with nodeId NULL");
        } else {
            query = mMessagesRef
                    .orderByKey()
                    .startAt(nodeId)
                    .limitToFirst(mPostsPerPage);
            Log.i(TAG, "query with startAt(nodeId): " + nodeId);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Message2Model user;
                List<Message2Model> userModels = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    user = userSnapshot.getValue(Message2Model.class);
                    // this is to include the key
                    user.key = userSnapshot.getKey();
                    userModels.add(user);
                    Log.i(TAG, "onDataChange: " + userSnapshot.getValue(Message2Model.class).getMessage());
                }
                String lastNode = userModels.get(userModels.size() - 1).getKey();
                Log.i(TAG, "nodeId: " + nodeId + " lastNode: " + lastNode);
                if (!lastNode.equals(nodeId))
                    userModels.remove(userModels.size() - 1);    // 19,19 so to remove duplicate remove one value
                else {
                    lastNode = "end";
                }

                mAdapter.addAll(userModels);
                mIsLoading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mIsLoading = false;
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