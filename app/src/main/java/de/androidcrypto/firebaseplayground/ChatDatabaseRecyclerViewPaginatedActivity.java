package de.androidcrypto.firebaseplayground;

import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.MessageModel;
import de.androidcrypto.firebaseplayground.models.UserModel;

public class ChatDatabaseRecyclerViewPaginatedActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    // see https://dpkpradhan649.medium.com/firebase-realtime-database-pagination-in-recyclerview-c3f9a4a7856f

    private static final String TAG = "ChatDbRecyclViewPaginat";
    // my database is not in default/US location so you need to fill in the reference here
    private static final String DATABASE_REFERENCE = "https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app";
    private static final String NODE_DATABASE_USER = "users";
    private static final String NODE_DATABASE_MESSAGES = "messages";

    TextView headerTextView;
    RecyclerView recyclerView;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    ProgressBar progressBar;

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference; // general database reference
    private DatabaseReference mMessagesRef; // messages in messages/roomId
    private Query mMessagesQuery; // query on e.g. last 10 messages
    int ITEM_LOAD_COUNT = 10;
    String last_key = "", last_node = "";
    boolean isMaxData = false, isScrolling = false;
    int currentItems, tottalItems, scrolledoutItems;
    LinearLayoutManager layoutManager;

    List<MessageModel> messageList;
    DatabaseMessageAdapter mDatabaseMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database_recycler_view_paginated);

        headerTextView = findViewById(R.id.tvChatDatabaseHeader);
        recyclerView = findViewById(R.id.rvChatDatabase);
        edtMessageLayout = findViewById(R.id.etChatDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etChatDatabaseMessage);
        progressBar = findViewById(R.id.pbChatDatabase);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance(DATABASE_REFERENCE).getReference();
        // the mMessagesRef is set when the own user is signed in and a chat partner is given
        // this is done with the method setMMessagesRef(authUserId, receiveUserId);
        // here called from setDatabaseForRoomId

        //getLastKeyFromFirebase();
        layoutManager = new LinearLayoutManager(getApplicationContext());

        messageList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseMessageAdapter = new DatabaseMessageAdapter(messageList);
        recyclerView.setAdapter(mDatabaseMessageAdapter);

        loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = layoutManager.getChildCount();
                tottalItems = layoutManager.getItemCount();
                scrolledoutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && currentItems + scrolledoutItems == tottalItems) {
                    //  Toast.makeText(getContext(), "fetch data", Toast.LENGTH_SHORT).show();
                    isScrolling = false;
                    //fetch data
                    progressBar.setVisibility(View.VISIBLE);
                    getMessages();
                }
            }
        });

    }

    private void getMessages() {
        if (!isMaxData) // 1st fasle
        {
            Query query;

            if (TextUtils.isEmpty(last_node))
                query = mDatabaseReference
                        .child(NODE_DATABASE_MESSAGES)
                        .child(roomId)
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);
            else
                query = mDatabaseReference
                        .child(NODE_DATABASE_MESSAGES)
                        .child(roomId)
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {

                        List<MessageModel> newMessages = new ArrayList<>();
                        List<String> newMessagesIds = new ArrayList<>(); // get the message keys
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            newMessages.add(messageSnapshot.getValue(MessageModel.class));
                            newMessagesIds.add(messageSnapshot.getKey());
                        }

                        //last_node = newMessages.get(newMessages.size() - 1).getId();    // 10  if it greater than the total items set to visible then fetch data from server
                        last_node = newMessagesIds.get(newMessages.size() - 1);    // 10  if it greater than the total items set to visible then fetch data from server

                        if (!last_node.equals(last_key))
                            newMessages.remove(newMessages.size() - 1);    // 19,19 so to remove duplicate remove one value
                        else
                            last_node = "end";

                        Toast.makeText(getApplicationContext(), "last_node " + last_node, Toast.LENGTH_SHORT).show();

                        mDatabaseMessageAdapter.addAll(newMessages);
                        mDatabaseMessageAdapter.notifyDataSetChanged();
                    } else   //reach to end no further child available to show
                    {
                        isMaxData = true;
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            progressBar.setVisibility(View.GONE); //if data end
        }
    }

    private void getLastKeyFromFirebase() {
        Log.i(TAG, "getLastKeyFromFirebase");
        Query getLastKey = mDatabaseReference
                .child(NODE_DATABASE_MESSAGES)
                .child(roomId)
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot lastkey : snapshot.getChildren())
                    last_key = lastkey.getKey();
                Log.i(TAG, "lastKey: " + last_key);
                Toast.makeText(getApplicationContext(), "last_key" + last_key, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "can not get last key", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setMMessagesRef(String chatOwnUid, String chatPartnerUid) {
        Log.i(TAG, "setDatabaseForRoomId");
        roomId = getRoomId(chatOwnUid, chatPartnerUid);
        Log.i(TAG, "room Id is " + roomId);
        mMessagesRef = mDatabaseReference.child(NODE_DATABASE_MESSAGES).child(roomId);
        //recentPostsQuery(ITEM_LOAD_COUNT);
        getLastKeyFromFirebase();
    }

    /*
    private void recentPostsQuery(int numberPosts) {
        // Last numberPosts posts, these are automatically the numberPosts most recent
        // due to sorting by push() keys
        mMessagesQuery = mMessagesRef
                .limitToLast(numberPosts);

        childEventListenerRecycler();

    }

    private void childEventListenerRecycler() {
        final Context mContext = this;
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                System.out.println("*** added: " + messageModel.getMessage() + " on " + messageModel.getMessageTime());
                messageList.add(messageModel);

                mDatabaseMessageAdapter = new DatabaseMessageAdapter(messageList);
                // Set adapter on recycler view
                recyclerView.setAdapter(mDatabaseMessageAdapter);
                recyclerView.smoothScrollToPosition(mDatabaseMessageAdapter.getItemCount() - 1);
                mDatabaseMessageAdapter.notifyDataSetChanged();
                //mDatabaseMessageAdapter.notifyItemInserted();



                // scroll down to last message
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                MessageModel newComment = dataSnapshot.getValue(MessageModel.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                MessageModel movedComment = dataSnapshot.getValue(MessageModel.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mMessagesQuery.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]
    }
     */

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + "_" + a;
        else return a + "_" + b;
    }

    /**
     * basic
     */

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {

            receiveUserId = "zzzzz"; // todo for test purposes
            if (!receiveUserId.equals("")) {
                Log.i(TAG, "onStart prepare database for chat");
                reload();
                enableUiOnSignIn(true);
                setDatabaseForRoomId(currentUser.getUid(), receiveUserId);

                //attachRecyclerViewAdapter();
            } else {
                headerTextView.setText("you need to select a receiveUser first");
                Log.i(TAG, "you need to select a receiveUser first");
            }
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
            enableUiOnSignIn(false);

        }
        // startListening begins when a user is logged in
        //childEventListenerRecycler();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    private void setDatabaseForRoomId(String ownUserUid, String receiveUserId) {
        Log.i(TAG, "setDatabaseForRoomId");
        roomId = getRoomId(ownUserUid, receiveUserId);
        Log.i(TAG, "room Id is " + roomId);
        headerTextView.setText("Paginated Chat in roomId " + roomId);
        setMMessagesRef(ownUserUid, receiveUserId);
    }


    @Override
    protected void onStop() {
        super.onStop();
        //if (firebaseRecyclerAdapter != null) {   }
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            //getAuthUserCredentials();
        } else {
            Toast.makeText(this, "you need to sign in before chatting", Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadSignedInUserData(String mAuthUserId) {
        Log.i(TAG, "loadSignedInUserData for Uid: " + mAuthUserId);
        if (!mAuthUserId.equals("")) {
            mDatabaseReference.child(NODE_DATABASE_USER)
                    .child(mAuthUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            //hideProgressBar();
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Error getting data", task.getException());
                            } else {
                                // check for a null value means no user data were saved before
                                UserModel userModel = task.getResult().getValue(UserModel.class);
                                Log.i(TAG, String.valueOf(userModel));
                                if (userModel == null) {
                                    Log.i(TAG, "userModel is null, show message");
                                } else {
                                    Log.i(TAG, "userModel email: " + userModel.getUserMail());
                                    authUserId = mAuthUserId;
                                    authUserEmail = userModel.getUserMail();
                                    authDisplayName = userModel.getUserName();
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            //hideProgressBar();
        }
    }

    private void reload() {
        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mFirebaseAuth.getCurrentUser());
                    /*
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                     */
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(getApplicationContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressBar();
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "no display name available";
            }
            String userData = String.format("Email: %s", authUserEmail);
            userData += "\nUID: " + authUserId;
            userData += "\nDisplay Name: " + authDisplayName;
            //signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            //signedInUser.setText(null);
            authUserId = "";
        }
    }

    private void enableUiOnSignIn(boolean userIsSignedIn) {
        if (!userIsSignedIn) {
            headerTextView.setText("you need to be signed in before starting a chat");
            edtMessageLayout.setEnabled(userIsSignedIn);
        } else {
            edtMessageLayout.setEnabled(userIsSignedIn);
        }
    }
}