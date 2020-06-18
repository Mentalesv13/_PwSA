package com.example.scanandgo_pwsa.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Objects;


public class Menu extends Fragment {

    private TextView hello, helloDesc;
    private CardView cvSelect, cvLogout, cvProfile, cvCategory, cvPromotion,cvShopping, cvScan, cvReader;
    private FloatingActionButton btnSelect, btnLogout, btnProfile, btnCategory, btnPromotion,btnShopping, btnScan, btnReader;
    private View view;
    private DatabaseHandler databaseHandler;
    private SessionManager sessionManager;
    private HashMap<String,String> userDetails;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setToolbar(false);
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        databaseHandler = new DatabaseHandler(getContext());
        sessionManager = new SessionManager(getContext());
        cvSelect = view.findViewById(R.id.tShop);
        cvReader= view.findViewById(R.id.tReader);
        cvScan = view.findViewById(R.id.tScanGo);
        cvShopping = view.findViewById(R.id.tList);
        cvCategory = view.findViewById(R.id.tCategory);
        cvPromotion = view.findViewById(R.id.tPromo);
        cvLogout= view.findViewById(R.id.tLogout);
        cvProfile = view.findViewById(R.id.tProfile);

        btnSelect = view.findViewById(R.id.btnShop);
        btnReader= view.findViewById(R.id.btnReader);
        btnScan = view.findViewById(R.id.btnScanGo);
        btnShopping = view.findViewById(R.id.btnShoppingList);
        btnCategory = view.findViewById(R.id.btnCategory);
        btnPromotion = view.findViewById(R.id.btnPromo);
        btnLogout= view.findViewById(R.id.btnLogout);
        btnProfile = view.findViewById(R.id.btnProfile);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        cvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isScanAndGoStarted()) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new SelectShop());
                }
                else
                {
                    Toast.makeText(getContext(),"Your Scan&Go transcation in progress...",Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(),"End it or Cancel to use 'Shop selection'!",Toast.LENGTH_LONG).show();
                }
            }
        });

        cvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ScanAndGo(false));
            }
        });
        cvReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new PriceReader());
            }
        });
        cvShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ShoppingList());
            }
        });

        cvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Category());
            }
        });

        cvPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Promotions());
            }
        });

        cvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Profile());
            }
        });

        cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isScanAndGoStarted()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setMessage("Are you sure want to logout?")
                            .setCancelable(false)
                            .setTitle("** Logout confirmation **")
                            .setPositiveButton("LOGOUT",
                                    new DialogInterface.OnClickListener() {
                                        @SuppressLint("SetTextI18n")
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            sessionManager.setLogin(false);
                                            databaseHandler.resetLogin();
                                            mGoogleSignInClient.signOut();
                                            hello.setText("HELLO");
                                            helloDesc.setText("Login to use all functionality of our service");
                                            cvLogout.setVisibility(View.GONE);
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

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isScanAndGoStarted()) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new SelectShop());
                }
                else
                {
                    Toast.makeText(getContext(),"Your Scan&Go transcation in progress...",Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(),"End it or Cancel to use 'Shop selection'!",Toast.LENGTH_LONG).show();
                }
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ScanAndGo(false));
            }
        });
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new PriceReader());
            }
        });
        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ShoppingList());
            }
        });

        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Category());
            }
        });

        btnPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Promotions());
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new Profile());
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isScanAndGoStarted()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setMessage("Are you sure want to logout?")
                            .setCancelable(false)
                            .setTitle("** Logout confirmation **")
                            .setPositiveButton("LOGOUT",
                                    new DialogInterface.OnClickListener() {
                                        @SuppressLint("SetTextI18n")
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            sessionManager.setLogin(false);
                                            databaseHandler.resetLogin();
                                            mGoogleSignInClient.signOut();
                                            hello.setText("HELLO");
                                            cvLogout.setVisibility(View.GONE);
                                            helloDesc.setText("Login to use all functionality of our service");
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
        

        hello = view.findViewById(R.id.hello);
        helloDesc = view.findViewById(R.id.helloDesc);
        userDetails = new HashMap<>();

        if(sessionManager.isLoggedIn())
        {
            userDetails = databaseHandler.getUserDetails();

            hello.setText("Hello " + userDetails.get("fname"));
            helloDesc.setText("YOUR PROFILE");
            cvLogout.setVisibility(View.VISIBLE);
        }
        else
        {
            hello.setText("HELLO");
            helloDesc.setText("Login to use all functionality of our service");
            cvLogout.setVisibility(View.GONE);

        }

        return view;
    }

}
