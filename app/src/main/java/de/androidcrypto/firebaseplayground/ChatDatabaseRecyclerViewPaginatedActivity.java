package de.androidcrypto.firebaseplayground;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference; // general database reference
    private DatabaseReference mMessagesRef; // messages in messages/roomId
    private Query mMessagesQuery; // query on e.g. last 10 messages
    int ITEM_LOAD_COUNT= 10;

    List<MessageModel > messageList;
    DatabaseMessageAdapter mDatabaseMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database_recycler_view_paginated);

        headerTextView = findViewById(R.id.tvChatDatabaseHeader);
        recyclerView = findViewById(R.id.rvChatDatabase);
        edtMessageLayout = findViewById(R.id.etChatDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etChatDatabaseMessage);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance(DATABASE_REFERENCE).getReference();
        // the mMessagesRef is set when the own user is signed in and a chat partner is given
        // this is done with the method setMMessagesRef(authUserId, receiveUserId);
        // here called from setDatabaseForRoomId

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
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                System.out.println("lastCompletelyVisibleItemPosition: " + lastCompletelyVisibleItemPosition);
                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == messageList.size() - 1) {
                    // bottom of list!
                    System.out.println("** scroll down, loading more");
                    //loadMore();
                    //isLoading = true;
                }

            }
        });

    }

    private void setMMessagesRef(String chatOwnUid, String chatPartnerUid) {
        Log.i(TAG, "setDatabaseForRoomId");
        roomId = getRoomId(chatOwnUid, chatPartnerUid);
        Log.i(TAG, "room Id is " + roomId);
        mMessagesRef = mDatabaseReference.child(NODE_DATABASE_MESSAGES).child(roomId);
        recentPostsQuery(ITEM_LOAD_COUNT);
    }

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
/*
                mDatabaseMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        //mBinding.messagesList.smoothScrollToPosition(adapter.getItemCount());
                        recyclerView.smoothScrollToPosition(mDatabaseMessageAdapter.getItemCount());
                        //recyclerView.scrollToPosition(mDatabaseMessageAdapter.getItemCount());
                    }
                });
*/


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