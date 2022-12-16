package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignUpEmailPasswordActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText connectionStatus, signedInUser, eMail, password;
    static final String TAG = "SignUpEmailPassword";
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_email_password);

        progressBar = findViewById(R.id.pbSignUpEmailPassword);

        connectionStatus = findViewById(R.id.etSignUpEmailPasswordStatus);
        signedInUser = findViewById(R.id.etSignUpEmailPasswordSignedInUser);
        eMail = findViewById(R.id.etSignUpEmailPasswordEmail);
        password = findViewById(R.id.etSignUpEmailPasswordPassword);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // todo remove fixed data
        eMail.setText("q@q.com");
        password.setText("123456");

        Button signUp = findViewById(R.id.btnSignUpEmailPasswordSignUp);
        Button signIn = findViewById(R.id.btnSignUpEmailPasswordSignIn);
        Button signOut = findViewById(R.id.btnSignUpEmailPasswordSignOut);
        Button backToMain = findViewById(R.id.btnSignUpEmailPasswordToMain);

        // create a new user in the database
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo validate input to avoid empty fields
                String emailData = Objects.requireNonNull(eMail.getText()).toString();
                String passwordData = Objects.requireNonNull(password.getText()).toString();
                String logString = String.format("SignUp with Email %s and password %s", emailData, passwordData);
                Log.i(TAG, logString);

                showProgressBar();

                mAuth.createUserWithEmailAndPassword(emailData, passwordData)
                        .addOnCompleteListener(SignUpEmailPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    // todo show fail message on the ui
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(view.getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                hideProgressBar();
                            }
                        });
            }
        });


        // if the user has an account go to the sign in activity
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpEmailPasswordActivity.this, SignInEmailPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                mAuth.signOut();
                updateUI(null);
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpEmailPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // todo email validation
        // email validation
        String email = "a@a.de";
        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        } else {
            connectionStatus.setText("no user is signed in");
            signedInUser.setText(null);
        }
    }

    private void reload() {
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
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
        hideProgressBar();
        if (user != null) {
            String status = String.format("User UID: %s", user.getUid()) +
                    " is verified: " + user.isEmailVerified();
            connectionStatus.setText(status);
            String userData = String.format("Email: %s", user.getEmail());
            signedInUser.setText(userData);
            /*
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.emailPasswordButtons.setVisibility(View.GONE);
            mBinding.emailPasswordFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);


             */
            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            connectionStatus.setText("signed out");
            signedInUser.setText(null);
 /*
            mBinding.detail.setText(null);

            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);

  */
        }
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}