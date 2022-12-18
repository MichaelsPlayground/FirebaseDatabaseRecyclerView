package de.androidcrypto.firebaseplayground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    com.google.android.material.textfield.TextInputEditText signedInUser;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signedInUser = findViewById(R.id.etMainSignedInUser);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /**
         * authentication sign-in/out section
         */

        Button signUpWithEmailAndPassword = findViewById(R.id.btnMainSignUpEmailPassword);
        signUpWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign up a user with email and password");
                Intent intent = new Intent(MainActivity.this, SignUpEmailPasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button signInWithEmailAndPassword = findViewById(R.id.btnMainSignInEmailPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign in a user with email and password");
                Intent intent = new Intent(MainActivity.this, SignInEmailPasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button signOut = findViewById(R.id.btnMainSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign out the current user");
                // set user onlineStatus in Firestore users to false
                mAuth.signOut();
                signedInUser.setText(null);
            }
        });

        /**
         * database section
         */

        Button chatDatabaseRecyclerView = findViewById(R.id.btnMainDatabaseChatRecyclerView);
        chatDatabaseRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "chat in database with RecyclerView");
                Intent intent = new Intent(MainActivity.this, ChatDatabaseRecyclerViewActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button chatDatabaseRecyclerViewPaginated = findViewById(R.id.btnMainDatabaseChatRecyclerViewPaginated);
        chatDatabaseRecyclerViewPaginated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "chat in database with Paginated RecyclerView");
                Intent intent = new Intent(MainActivity.this, ChatDatabaseRecyclerViewPaginatedActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button chatDatabaseRecyclerViewPaginated2 = findViewById(R.id.btnMainDatabaseChatRecyclerViewPaginated2);
        chatDatabaseRecyclerViewPaginated2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "chat in database with Paginated 2 RecyclerView");
                Intent intent = new Intent(MainActivity.this, ChatDatabaseRecyclerViewPaginated2Activity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button chatDatabaseRecyclerViewPaginated3 = findViewById(R.id.btnMainDatabaseChatRecyclerViewPaginated3);
        chatDatabaseRecyclerViewPaginated3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "chat in database with Paginated 3 RecyclerView");
                Intent intent = new Intent(MainActivity.this, ChatDatabaseRecyclerViewPaginated3Activity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button chatDatabaseRecyclerViewPaginated4 = findViewById(R.id.btnMainDatabaseChatRecyclerViewPaginated4);
        chatDatabaseRecyclerViewPaginated4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "chat in database with Paginated 4 RecyclerView");
                Intent intent = new Intent(MainActivity.this, ChatDatabaseRecyclerViewPaginated4Activity.class);
                startActivity(intent);
                //finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        } else {
            signedInUser.setText(null);
        }
    }

    private void reload() {
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
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
            String userData = String.format("Email: %s", user.getEmail())
                    + String.format("\nemail address is verified: %s", user.isEmailVerified());
            if (user.getDisplayName() != null) {
                userData += String.format("\ndisplay name: %s", Objects.requireNonNull(user.getDisplayName()).toString());
            } else {
                userData += "\nno display name available";
            }
            if (user.getPhotoUrl() != null) {
                userData += String.format("\nphoto url: %s", Objects.requireNonNull(user.getPhotoUrl()).toString());
            } else {
                userData += "\nno photo url available";
            }
            signedInUser.setText(userData);

            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            signedInUser.setText(null);
        }
    }
}