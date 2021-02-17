package com.example.scanandgo_pwsa.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class Profile extends Fragment {

    private TextView name, email;
    private View view;
    private DatabaseHandler databaseHandler;
    private SessionManager sessionManager;
    private HashMap<String,String> userDetails;
    private ConstraintLayout ON, OFF;
    private int RC_SIGN_IN = 1;

    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "Profile";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setToolbar(false);
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        databaseHandler = new DatabaseHandler(getContext());
        sessionManager = new SessionManager(getContext());
        ON = view.findViewById(R.id.ON);
        OFF = view.findViewById(R.id.welcome);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        userDetails = new HashMap<>();
        db = FirebaseFirestore.getInstance();

//        btnDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!sessionManager.isScanAndGoStarted()) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
//                    builder.setMessage("Are you sure want to delete your account?")
//                            .setCancelable(false)
//                            .setTitle("** Delete confirmation **")
//                            .setPositiveButton("DELETE",
//                                    new DialogInterface.OnClickListener() {
//                                        @SuppressLint("SetTextI18n")
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            dialog.cancel();
//                                            showCustomLoadingDialog();
//                                            db.collection("users")
//                                                    .whereEqualTo("email", databaseHandler.getUserDetails().get("email")).get()
//                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                            if (task.isSuccessful()) {
//                                                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                                                    document.getReference().delete();
//                                                                }
//                                                            }
//                                                        }
//                                                    });
//
//                                            sessionManager.setLogin(false);
//                                            databaseHandler.resetLogin();
//                                            mGoogleSignInClient.signOut();
//                                            ON.setVisibility(View.GONE);
//                                            OFF.setVisibility(View.VISIBLE);
//                                            Toast.makeText(getContext(), "You are successfully logout.",
//                                                    Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                            .setNegativeButton("Cancel",
//                                    new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            // cancel the dialog box
//                                            dialog.cancel();
//                                        }
//                                    });
//                    AlertDialog alert = builder.create();
//                    alert.show();
//                }
//                else
//                {
//                    Toast.makeText(getContext(),"Your Scan&Go transcation in progress...",Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(),"End it or Cancel to use 'Logout'!",Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//

        ON.setVisibility(View.GONE);
        OFF.setVisibility(View.GONE);

        if(sessionManager.isLoggedIn())
        {
            ON.setVisibility(View.VISIBLE);
            userDetails = databaseHandler.getUserDetails();
            name.setText(userDetails.get("fname") + " " + userDetails.get("lname"));
            email.setText(userDetails.get("email"));
        }
        else
        {
            OFF.setVisibility(View.VISIBLE);
        }

        SignInButton signInButton = view.findViewById(R.id.sign_in_button);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        ConstraintLayout clHistory = view.findViewById(R.id.cl_history);
        ConstraintLayout clChart = view.findViewById(R.id.cl_chart);
        FloatingActionButton btnHistory = view.findViewById(R.id.btnHistory);
        FloatingActionButton btnChart = view.findViewById(R.id.btnChart);

        clHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new PurchaseHistory());
            }
        });

        clChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new ExpensesChart());
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new PurchaseHistory());
            }
        });

        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new ExpensesChart());
            }
        });

//        btnRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Map<String, Object> collectionData = new HashMap<String, Object>() {{
//                    put("firstname", firstName.getText().toString().trim());
//                    put("lastname", lastName.getText().toString().trim());
//                    put("email", etEmail.getText().toString().trim());
//                    put("phone", phone.getText().toString().trim());
//                    put("uID", mAuth.getCurrentUser().getUid().trim());
//                }};
//
//                db.collection("users").document(mAuth.getCurrentUser().getUid().trim()).set(collectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        sessionManager.setLogin(true);
//                        databaseHandler.resetLogin();
//                        databaseHandler.addUser(
//                                firstName.getText().toString().trim(),
//                                lastName.getText().toString().trim(),
//                                etEmail.getText().toString().trim(),
//                                phone.getText().toString().trim(),
//                                mAuth.getCurrentUser().getUid().trim());
//
//                        firstName.getText().clear();
//                        lastName.getText().clear();
//                        etEmail.getText().clear();
//                        phone.getText().clear();
//                        register.setVisibility(View.GONE);
//                        //start.setVisibility(View.VISIBLE);
//                        ON.setVisibility(View.VISIBLE);
//
//                        userDetails = databaseHandler.getUserDetails();
//                        name.setText(userDetails.get("fname") + " " + userDetails.get("lname"));
//                        email.setText(userDetails.get("email"));
//
//                        Toast.makeText(getContext(),"ADDED", Toast.LENGTH_LONG).show();
//                    }
//                });


//                JSONObject jsonObject = new JSONObject();
//                JSONArray jsonArray = new JSONArray();
//
//                try {
//                    jsonObject.put("bill",jsonArray);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Map<String, Object> jsonMap = new Gson().fromJson(jsonObject.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
//                db.collection("bills").document(mAuth.getCurrentUser().getUid().trim()).set(jsonMap);
//            }
//        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        return view;
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(getContext(), R.string.SignInSucces, Toast.LENGTH_SHORT).show();
            OFF.setVisibility(View.GONE);
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Toast.makeText(getContext(), R.string.SignInFail, Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        //check if the account is null
        if (acct != null) {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(authCredential).addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.Successful, Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        OFF.setVisibility(View.GONE);
                        updateUI(user);
                    } else {
                        Toast.makeText(getContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
        } else {
            //Toast.makeText(getContext(), "acc failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(FirebaseUser fUser) {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getActivity()).getApplicationContext());
        if (account != null) {
            databaseHandler.addUser(
                    (account.getDisplayName()).split(" ")[0],
                    (account.getDisplayName()).split(" ")[1],
                    account.getEmail(),
                    mAuth.getUid());

            sessionManager.setLogin(true);
            OFF.setVisibility(View.GONE);
            ON.setVisibility(View.VISIBLE);
            userDetails = databaseHandler.getUserDetails();
            name.setText(userDetails.get("fname") + " " + userDetails.get("lname"));
            email.setText(userDetails.get("email"));

        }
}


}
