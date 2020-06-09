package com.example.scanandgo_pwsa.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.se.omapi.Session;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.Product;
import com.example.scanandgo_pwsa.model.ShoppingList;
import com.example.scanandgo_pwsa.welcome.ShopSelectSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Profile extends Fragment {

    private TextView name, email;
    private View view;
    private DatabaseHandler databaseHandler;
    private SessionManager sessionManager;
    private HashMap<String,String> userDetails;
    private Button btnCancel, btnRegister, btnDelete, btnEdit;
    private EditText firstName, lastName, etEmail, phone;
    private ConstraintLayout ON, OFF, register;
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
        register = view.findViewById(R.id.register);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnEdit = view.findViewById(R.id.btnEdit);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        userDetails = new HashMap<>();
        db = FirebaseFirestore.getInstance();

        firstName = view.findViewById(R.id.etFirstName);
        lastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        phone = view.findViewById(R.id.etPhone);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isScanAndGoStarted()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setMessage("Are you sure want to delete your account?")
                            .setCancelable(false)
                            .setTitle("** Delete confirmation **")
                            .setPositiveButton("DELETE",
                                    new DialogInterface.OnClickListener() {
                                        @SuppressLint("SetTextI18n")
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            showCustomLoadingDialog();
                                            db.collection("users")
                                                    .whereEqualTo("email", databaseHandler.getUserDetails().get("email")).get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    document.getReference().delete();
                                                                }
                                                            }
                                                        }
                                                    });

                                            sessionManager.setLogin(false);
                                            databaseHandler.resetLogin();
                                            mGoogleSignInClient.signOut();
                                            ON.setVisibility(View.GONE);
                                            OFF.setVisibility(View.VISIBLE);
                                            Toast.makeText(getContext(), "You are successfully logout.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // cancel the dialog box
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                {
                    Toast.makeText(getContext(),"Your Scan&Go transcation in progress...",Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(),"End it or Cancel to use 'Logout'!",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            updateLayout();
            }
        });

        ON.setVisibility(View.GONE);
        OFF.setVisibility(View.GONE);
        register.setVisibility(View.GONE);

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

        //Logout
        //sessionManager.setLogin(false);
        //mGoogleSignInClient.signOut();

//        if (user != null) {
//            // User is signed in
//            start.setVisibility(View.VISIBLE);
//            scanProduct.setVisibility(View.VISIBLE);
//            Toast.makeText(getContext(),"USER LOGIN", Toast.LENGTH_SHORT).show();
//        } else {
//            welcome.setVisibility(View.VISIBLE);
//            scanProduct.setVisibility(View.GONE);
//            Toast.makeText(getContext(),"USER LOGOUT", Toast.LENGTH_SHORT).show();
//            // No user is signed in
//        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String, Object> collectionData = new HashMap<String, Object>() {{
                    put("firstname", firstName.getText().toString().trim());
                    put("lastname", lastName.getText().toString().trim());
                    put("email", email.getText().toString().trim());
                    put("phone", phone.getText().toString().trim());
                }};

                db.collection("users").document().set(collectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.setLogin(true);
                        databaseHandler.resetLogin();
                        databaseHandler.addUser(
                                firstName.getText().toString().trim(),
                                lastName.getText().toString().trim(),
                                etEmail.getText().toString().trim(),
                                phone.getText().toString().trim());
                        firstName.getText().clear();
                        lastName.getText().clear();
                        etEmail.getText().clear();
                        phone.getText().clear();
                        register.setVisibility(View.GONE);
                        //start.setVisibility(View.VISIBLE);
                        ON.setVisibility(View.VISIBLE);

                        userDetails = databaseHandler.getUserDetails();
                        name.setText(userDetails.get("fname") + " " + userDetails.get("lname"));
                        email.setText(userDetails.get("email"));

                        Toast.makeText(getContext(),"ADDED", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OFF.setVisibility(View.VISIBLE);
                register.setVisibility(View.GONE);
            }
        });

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
            Toast.makeText(getContext(), "Signed In Successfully", Toast.LENGTH_SHORT).show();
            OFF.setVisibility(View.GONE);
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Sign In Failed", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        OFF.setVisibility(View.GONE);
                        updateUI(user);
                    } else {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "acc failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(FirebaseUser fUser) {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getActivity()).getApplicationContext());
        if (account != null) {

            db.collection("users")
                    .whereEqualTo("email", account.getEmail()).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                databaseHandler.resetLogin();
                                boolean createdAccount = false;
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    createdAccount = true;
                                    databaseHandler.addUser(
                                            (String)document.get("firstname"),
                                            (String)document.get("lastname"),
                                            (String)document.get("email"),
                                            (String)document.get("phone"));
                                }

                                if (createdAccount)
                                {
                                    sessionManager.setLogin(true);
                                    register.setVisibility(View.GONE);
                                    OFF.setVisibility(View.GONE);
                                    //start.setVisibility(View.VISIBLE);
                                    ON.setVisibility(View.VISIBLE);
                                    userDetails = databaseHandler.getUserDetails();
                                    name.setText(userDetails.get("fname") + " " + userDetails.get("lname"));
                                    email.setText(userDetails.get("email"));
                                }
                                else {
                                    OFF.setVisibility(View.GONE);
                                    String[] names = Objects.requireNonNull(account.getDisplayName()).split(" ");
                                    firstName.setText( names[0]);
                                    lastName.setText( names[1]);
                                    etEmail.setText(account.getEmail());
                                    register.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
}

    private void updateLayout() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.edituser_dialog, null);

        final EditText firstName = dialogView.findViewById(R.id.etFirstName);
        final EditText lastName = dialogView.findViewById(R.id.etLastName);
        final EditText email = dialogView.findViewById(R.id.etEmail);
        final EditText phone = dialogView.findViewById(R.id.etPhone);

        firstName.setText(databaseHandler.getUserDetails().get("fname"));
        lastName.setText(databaseHandler.getUserDetails().get("lname"));
        email.setText(databaseHandler.getUserDetails().get("email"));
        phone.setText(databaseHandler.getUserDetails().get("phone"));

        final Button btnUpdate = dialogView.findViewById(R.id.btnEdit);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String, Object> collectionData = new HashMap<String, Object>() {{
                    put("firstname", firstName.getText().toString().trim());
                    put("lastname", lastName.getText().toString().trim());
                    put("email", email.getText().toString().trim());
                    put("phone", phone.getText().toString().trim());
                }};

                db.collection("users")
                        .whereEqualTo("email", databaseHandler.getUserDetails().get("email")).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        document.getReference().set(collectionData);
                                    }
                                }
                            }
                        });
            }
        });

        dialogBuilder.setView(dialogView);
        //dialogBuilder.setTitle("Select date");
        dialogBuilder.setCancelable(true);


        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                final Button btnClose = dialogView.findViewById(R.id.btnCancel);

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    private void showCustomLoadingDialog() {
        final LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.showDialog();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
            }
        }, 500);
    }

}
