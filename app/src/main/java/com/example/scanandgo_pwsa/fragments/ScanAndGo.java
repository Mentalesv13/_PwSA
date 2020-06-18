package com.example.scanandgo_pwsa.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.ScanAndGoAdapter;
import com.example.scanandgo_pwsa.helper.BarcodeScanner;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.Product;
import com.example.scanandgo_pwsa.model.Shop;
import com.example.scanandgo_pwsa.payments.PayPalConfig;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ScanAndGo extends Fragment {
    private ConstraintLayout welcome, start, scanProduct, register, shopSelection, currentShop, productL;
    private Button btnScanCode, btnGPS, btnStart;
    private EditText firstName, lastName, email, phone;
    private boolean flag, localizationSet;

    private HashMap<String, Double> shopDistance;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    private LoadingDialog loadingDialog;

    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "ScanAndGo";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int RC_SIGN_IN = 1;
    private boolean selected;
    private HashMap<String, String> shop;
    private HashMap<String, Shop> shopHashMap;
    private TextView name;
    private TextView address;
    private CardView cardViewStart;
    private ImageView cartCancel, cartEnd;

    private RecyclerView recyclerView;
    private List<String> productsList;
    private TextView discount, totalProducts, totalPrice;
    private HashMap<String, Product> products;
    private HashMap<String, com.example.scanandgo_pwsa.model.ShoppingList> sagList;
    private HashMap<String, com.example.scanandgo_pwsa.model.ShoppingList> shoppingList;
    private Double discountValue=0.0, totalProductsValue=0.0;

    //Session, localStorage

    private SessionManager sessionManager;
    private DatabaseHandler databaseHandler;
    private FloatingActionButton addProduct;

    public ScanAndGo(boolean b) {
        selected = b;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        //View
        ((MainActivity)getActivity()).setToolbar(false);
        View view = inflater.inflate(R.layout.fragment_scan_and_go, container, false);
        databaseHandler = new DatabaseHandler(Objects.requireNonNull(getActivity()).getApplicationContext());
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        localizationSet = false;
        loadingDialog = new LoadingDialog(getActivity());
        shopDistance = new HashMap<>();
        shopHashMap = new HashMap<>();
        context = getContext();

        cartCancel = view.findViewById(R.id.cartCancel);
        cartEnd = view.findViewById(R.id.cartEnd);

        cartEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sagList = databaseHandler.getScanAndGoShoppingList();
                if (sagList.size()>0)
                {
                    confirmList();
                }
                else
                {
                    Toast.makeText(getActivity(),"It seems that your cart was empty!",Toast.LENGTH_LONG).show();
                    Toast.makeText(getActivity(),"Add some products to continue! ",Toast.LENGTH_LONG).show();
                    cartEnd.setEnabled(false);
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0.1f);
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    cartEnd.setColorFilter(filter);
                }

            }
        });

        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);

        addProduct = view.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new BarcodeScanner());

            }
        });

        db = FirebaseFirestore.getInstance();
        welcome = view.findViewById(R.id.welcome);
        start = view.findViewById(R.id.start);
        scanProduct = view.findViewById(R.id.scanProduct);
        productL = view.findViewById(R.id.productLayout);
        shopSelection = view.findViewById(R.id.shopSelection);
        register = view.findViewById(R.id.register);
        currentShop = view.findViewById(R.id.currentShop);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnScanCode = view.findViewById(R.id.btnScanCode);
        cardViewStart = view.findViewById(R.id.cardViewStart);



        products = new HashMap<>();
        products = databaseHandler.getProductsDetails();
        discount = view.findViewById(R.id.discount);
        totalPrice = view.findViewById(R.id.totalPrice);
        totalProducts = view.findViewById(R.id.totalProducts);
        productsList = new ArrayList<>();
        sagList = new HashMap<>();
        sagList = databaseHandler.getScanAndGoShoppingList();
        shoppingList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        recyclerView = view.findViewById(R.id.recyclerView);

        DecimalFormat decf = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.US));
        for (Map.Entry mapElement : sagList.entrySet()) {

            com.example.scanandgo_pwsa.model.ShoppingList temp =
                    (com.example.scanandgo_pwsa.model.ShoppingList) mapElement.getValue();
            productsList.add(temp.getBarcode());


            if(products.get(temp.getBarcode())!=null) {
                Product temporary = products.get(temp.getBarcode());

                if (temporary != null) {
                    totalProductsValue = totalProductsValue + Double.parseDouble(decf.format(
                            Double.parseDouble(temporary.getPrice().toString()) *
                                    temp.getAmount()));

                    if (Double.parseDouble(temporary.getDiscount().toString()) > 0) {
                        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(
                                "yyyy.MM.dd' 'HH:mm:ss");
                        String date = df.format(Calendar.getInstance().getTime());
                        try {
                            Date d1 = df.parse(date);

                            Date d2 = df.parse(temporary.getPromoStart());

                            Date d3 = df.parse(temporary.getPromoEnd());
                            if (d1 != null && (d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                                discountValue = discountValue + ((Double.parseDouble(decf.format(
                                        Double.parseDouble(temporary.getPrice().toString()) *
                                                Double.parseDouble(temporary.getDiscount().toString()))) *
                                        temp.getAmount()));
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        totalPrice.setText(decf.format(Double.parseDouble(decf.format(totalProductsValue))-
                Double.parseDouble(decf.format(discountValue))));
        discount.setText("- " + decf.format(discountValue));
        totalProducts.setText(decf.format(totalProductsValue));


        initAdapter();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getActivity())
                .getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        welcome.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        scanProduct.setVisibility(View.GONE);
        shopSelection.setVisibility(View.GONE);
        register.setVisibility(View.GONE);
        productL.setVisibility(View.GONE);
        cartCancel.setVisibility(View.GONE);
        cartEnd.setVisibility(View.GONE);

        btnStart = view.findViewById(R.id.btnContinue);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopSelection.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                cartCancel.setVisibility(View.VISIBLE);
                cartEnd.setVisibility(View.VISIBLE);
                scanProduct.setVisibility(View.VISIBLE);
                sessionManager.setScanAndGoStarted(true);
            }
        });

        if (sessionManager.isScanAndGoStarted()) {
            scanProduct.setVisibility(View.VISIBLE);
            cartCancel.setVisibility(View.VISIBLE);
            cartEnd.setVisibility(View.VISIBLE);


            cartCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setMessage("Are you sure want to cancel Scan&Go transaction?")
                            .setCancelable(false)
                            .setTitle("** Transaction cancel confirmation **")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        @SuppressLint("SetTextI18n")
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                            databaseHandler.resetShopAndGoList();
                                            dialog.cancel();
                                            updateValues();
                                            totalPrice.setText("0.00");
                                            discount.setText("0.00");
                                            totalProducts.setText("0.00");

                                            shopSelection.setVisibility(View.VISIBLE);
                                            start.setVisibility(View.GONE);
                                            productL.setVisibility(View.GONE);
                                            scanProduct.setVisibility(View.GONE);
                                            currentShop.setVisibility(View.GONE);
                                            cardViewStart.setVisibility(View.GONE);
                                            cartCancel.setVisibility(View.GONE);
                                            cartEnd.setVisibility(View.GONE);
                                            sessionManager.setScanAndGoStarted(false);
                                            Toast.makeText(getContext(),"Transaction canceled.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // cancel the dialog box
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });



            if (sagList.size() > 0) {
                start.setVisibility(View.GONE);
                productL.setVisibility(View.VISIBLE);
            } else {
                start.setVisibility(View.VISIBLE);
                productL.setVisibility(View.GONE);

            }
        }
        else {
            btnGPS = view.findViewById(R.id.btnGPS);

            db.collection("shops")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("ShopList", document.getId() + " => " +
                                            document.getData().get("name") + " " +
                                            document.getData().get("address") + " " +
                                            document.getData().get("localization") + " ");

                                    String name = (String) document.getData().get("name");
                                    String address = (String) document.getData().get("address");
                                    String localization = String.valueOf(document.getData()
                                            .get("localization"));
                                    String shopCode = (String) document.getData().get("shopCode");
                                    String[] result = localization.split(",");
                                    Double longitude = Double.valueOf(result[1]);
                                    Double latitude = Double.valueOf(result[0]);
                                    shopHashMap.put(shopCode, new Shop(address,
                                            shopCode,
                                            longitude,
                                            latitude,
                                            name,
                                            0,
                                            document.getId()));
                                }
                            }
                            else
                            {
                                Log.d("SearchProduct",
                                        "Error getting documents: ", task.getException());
                            }
                        }
                    });


            firstName = view.findViewById(R.id.etFirstName);
            lastName = view.findViewById(R.id.etLastName);
            email = view.findViewById(R.id.etEmail);
            phone = view.findViewById(R.id.etPhone);

            btnScanCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ShopSelection());
                }
            });
            btnGPS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    localizationSet = false;
                    if ( ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                    {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    else {
                        flag = displayGpsStatus();
                        if (flag) {
                            Log.v(TAG, "onClick");


                            locationListener = new ScanAndGo.MyLocationListener();

                            locationManager.requestLocationUpdates(LocationManager
                                    .GPS_PROVIDER, 5000, 10, locationListener);

                        } else {
                            alertbox("Gps Status!!", "Your GPS is: OFF");
                            locationListener = null;
                        }
                    }
                }
            });
            name = view.findViewById(R.id.name);
            address = view.findViewById(R.id.address);

            if (sessionManager.isLoggedIn()) {
                shopSelection.setVisibility(View.VISIBLE);
                cardViewStart.setVisibility(View.GONE);
            } else {
                welcome.setVisibility(View.VISIBLE);
                scanProduct.setVisibility(View.GONE);
                SignInButton signInButton = view.findViewById(R.id.sign_in_button);
                mAuth = FirebaseAuth.getInstance();

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Map<String, Object> collectionData = new HashMap<String, Object>() {{
                            put("firstname", firstName.getText().toString().trim());
                            put("lastname", lastName.getText().toString().trim());
                            put("email", email.getText().toString().trim());
                            put("phone", phone.getText().toString().trim());
                            put("uID", mAuth.getCurrentUser().getUid().trim());
                        }};

                        db.collection("users").document(mAuth.getCurrentUser().getUid().trim()).set(collectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sessionManager.setLogin(true);
                                databaseHandler.resetLogin();
                                databaseHandler.addUser(
                                        firstName.getText().toString().trim(),
                                        lastName.getText().toString().trim(),
                                        email.getText().toString().trim(),
                                        phone.getText().toString().trim(),
                                        mAuth.getCurrentUser().getUid().trim());
                                firstName.getText().clear();
                                lastName.getText().clear();
                                email.getText().clear();
                                phone.getText().clear();
                                register.setVisibility(View.GONE);
                                //start.setVisibility(View.VISIBLE);
                                shopSelection.setVisibility(View.VISIBLE);
                                cardViewStart.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "ADDED", Toast.LENGTH_LONG).show();
                            }
                        });

                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = new JSONArray();

                        try {
                            jsonObject.put("bill",jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Map<String, Object> jsonMap = new Gson().fromJson(jsonObject.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
                        db.collection("bills").document(mAuth.getCurrentUser().getUid().trim()).set(jsonMap);


                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        welcome.setVisibility(View.VISIBLE);
                        register.setVisibility(View.GONE);
                    }
                });

                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signIn();
                    }
                });
            }
        }

        if (selected) {
            shop = databaseHandler.getShopDetails();
            currentShop.setVisibility(View.VISIBLE);

            name.setText(shop.get("shopname"));
            address.setText(shop.get("address"));
            SplashTask task = new SplashTask();
            task.execute();
            btnStart.setVisibility(View.VISIBLE);
            cardViewStart.setVisibility(View.VISIBLE);
        } else {
            currentShop.setVisibility(View.GONE);
        }

        return view;
    }

    private void confirmList() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.productonlist, null);


        HorizontalScrollView scrollView = dialogView.findViewById(R.id.scrollView);

        scrollView.removeAllViews();

        LinearLayout listView = new LinearLayout(getActivity().getBaseContext());
        listView.setOrientation(LinearLayout.VERTICAL);

        sagList = databaseHandler.getScanAndGoShoppingList();
        shoppingList = databaseHandler.getShoppingList();
        boolean isListComplete = false;
        for (Map.Entry product : shoppingList.entrySet())
        {
            String barcode = (String) product.getKey();
            //Log.e("TAG",barcode);

            if(sagList.get(barcode) == null)
            {
                if (products.get(barcode) != null ) {
                    isListComplete = true;
                    //Log.e("TAG", "Test");
                    com.example.scanandgo_pwsa.model.ShoppingList temp =
                            (com.example.scanandgo_pwsa.model.ShoppingList) product.getValue();
                    //Log.e("TAG", temp.getProductName());
                    final TextView textView = new TextView(getActivity().getBaseContext());
                    textView.setText(temp.getProductName() + " " + temp.getAmount() + "pcs.");
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                    listView.addView(textView);
                }
            }
        }

        if(isListComplete) {

            scrollView.addView(listView);

            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);


            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                final Button btnClose = dialogView.findViewById(R.id.btnClose);
                final Button btnContinue = dialogView.findViewById(R.id.btnContinue);


                btnContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentLayout();
                    }
                });

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
        else
        {
            paymentLayout();
        }
    }

    private void paymentLayout() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
        decf.setRoundingMode(RoundingMode.HALF_UP);




        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.paymentlayout, null);
        updateValues();

        ImageView btnPay = dialogView.findViewById(R.id.btnPayPal);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPayment();
            }
        });

        TextView discount = dialogView.findViewById(R.id.discount);
        TextView totalPrice = dialogView.findViewById(R.id.totalPrice);
        TextView totalProducts = dialogView.findViewById(R.id.totalProducts);



        sagList = databaseHandler.getScanAndGoShoppingList();
        shoppingList = databaseHandler.getShoppingList();
        for (Map.Entry product : shoppingList.entrySet())
        {
            String barcode = (String) product.getKey();
            //Log.e("TAG",barcode);

        }
        totalPrice.setText(decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                Double.parseDouble(decf.format(discountValue))));
        discount.setText("- " + decf.format(discountValue));
        totalProducts.setText(decf.format(totalProductsValue));
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);


            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {

                }
            });
            alertDialog.show();
    }

    private void getPayment() {
        //Getting the amount from editText

        //Creating a paypalpayment
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
        decf.setRoundingMode(RoundingMode.HALF_UP);

        PayPalPayment payment = new PayPalPayment(new BigDecimal(decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                Double.parseDouble(decf.format(discountValue)))), "PLN", "Scan&Go payment",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(getActivity(), PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);


        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }


    private void initAdapter() {
        ScanAndGoAdapter recyclerAdapter = new ScanAndGoAdapter(productsList, getContext(), getActivity(),
                this);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void updateValues() {
        discountValue = 0.0;
        totalProductsValue = 0.0;
        sagList = databaseHandler.getScanAndGoShoppingList();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
        decf.setRoundingMode(RoundingMode.HALF_UP);
        if (sagList.size() == 0) {
            cartEnd.setEnabled(true);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1f);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            cartEnd.setColorFilter(filter);
            start.setVisibility(View.VISIBLE);
            productL.setVisibility(View.GONE);
        } else {
            start.setVisibility(View.GONE);
            productL.setVisibility(View.VISIBLE);
            for (Map.Entry mapElement : sagList.entrySet()) {

                com.example.scanandgo_pwsa.model.ShoppingList temp =
                        (com.example.scanandgo_pwsa.model.ShoppingList) mapElement.getValue();
                productsList.add(temp.getBarcode());
                if (products.get(temp.getBarcode()) != null) {
                    Product temporary = products.get(temp.getBarcode());

                    if (temporary != null)
                    {
                        totalProductsValue = totalProductsValue + Double.parseDouble(decf.format(
                                Double.parseDouble(temporary.getPrice().toString()) *
                                        temp.getAmount()));

                        if (Double.parseDouble(temporary.getDiscount().toString()) > 0) {
                            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(
                                    "yyyy.MM.dd' 'HH:mm:ss");
                            String date = df.format(Calendar.getInstance().getTime());
                            try {
                                Date d1 = df.parse(date);

                                Date d2 = df.parse(temporary.getPromoStart());

                                Date d3 = df.parse(temporary.getPromoEnd());
                                if (d1 != null && (d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                                    discountValue = discountValue + ((Double.parseDouble(decf.format(
                                            Double.parseDouble(temporary.getPrice().toString()) *
                                                    Double.parseDouble(temporary.getDiscount().toString()))) *
                                            temp.getAmount()));
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            totalPrice.setText(decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                    Double.parseDouble(decf.format(discountValue))));
            discount.setText("- " + decf.format(discountValue));
            totalProducts.setText(decf.format(totalProductsValue));
        }
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

        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation

                databaseHandler.resetShopAndGoList();
                sessionManager.setScanAndGoStarted(false);
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
                decf.setRoundingMode(RoundingMode.HALF_UP);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);




                        //Starting a new activity for the payment details and also putting the payment details with intent
                        startActivity(new Intent(getActivity(), MainActivity.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("confirm", true)
                                .putExtra("select", false)
                                .putExtra("PaymentAmount", decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                                        Double.parseDouble(decf.format(discountValue)))));


                        JSONObject jsonDetails = new JSONObject(paymentDetails);

                        JSONObject json = jsonDetails.getJSONObject("response");

                        String pID = json.getString("id");

                        DateFormat df = new SimpleDateFormat(
                                "yyyy.MM.dd' 'HH:mm:ss");
                        String date = df.format(Calendar.getInstance().getTime());
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("shopCode", databaseHandler.getShopDetails().get("shopID"));
                            jsonObject.put("shopName", databaseHandler.getShopDetails().get("shopname"));
                            jsonObject.put("shopAddress", databaseHandler.getShopDetails().get("address"));
                            jsonObject.put("date", date);
                            jsonObject.put("total", Double.parseDouble(decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                                    Double.parseDouble(decf.format(discountValue)))));
                            jsonObject.put("paymentID", pID);

                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonProduct;
                            for (Map.Entry mapElement : sagList.entrySet()) {
                                jsonProduct = new JSONObject();
                                com.example.scanandgo_pwsa.model.ShoppingList temp =
                                        (com.example.scanandgo_pwsa.model.ShoppingList) mapElement.getValue();

                                if(products.get(temp.getBarcode())!=null) {
                                    Product temporary = products.get(temp.getBarcode());

                                    if (temporary != null) {
                                        jsonProduct.put("amount",temp.getAmount());
                                        jsonProduct.put("name",temporary.getName());

                                        if (Double.parseDouble(temporary.getDiscount().toString()) > 0) {
                                            try {
                                                Date d1 = df.parse(date);

                                                Date d2 = df.parse(temporary.getPromoStart());

                                                Date d3 = df.parse(temporary.getPromoEnd());
                                                if (d1 != null && (d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                                                    double tempValue = ((Double.parseDouble(decf.format(
                                                            Double.parseDouble(temporary.getPrice().toString()) *
                                                                    (1.0-Double.parseDouble(temporary.getDiscount().toString()))))));
                                                    jsonProduct.put("price",tempValue);
                                                    jsonProduct.put("total",((Double.parseDouble(decf.format(
                                                            (Double.parseDouble(temporary.getPrice().toString()) *
                                                                    (1.0-Double.parseDouble(temporary.getDiscount().toString()))) * temp.getAmount())))));
                                                }
                                                else
                                                {

                                                    jsonProduct.put("price",temporary.getPrice());

                                                    jsonProduct.put("total",Double.parseDouble(decf.format(
                                                            Double.parseDouble(temporary.getPrice().toString()) *
                                                                    temp.getAmount())));}

                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            jsonProduct.put("price",temporary.getPrice());
                                            jsonProduct.put("total",Double.parseDouble(decf.format(
                                                    Double.parseDouble(temporary.getPrice().toString()) *
                                                            temp.getAmount())));
                                        }
                                    }
                                }

                                jsonArray.put(jsonProduct);

                            }

                            jsonObject.put("products",jsonArray);

                            DocumentReference washingtonRef = db.collection("bills").document(databaseHandler.getUserDetails().get("uid"));

                            Map<String, Object> jsonMap = new Gson().fromJson(jsonObject.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());

                            washingtonRef.update("bill", FieldValue.arrayUnion(jsonMap));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } catch (JSONException e) {
                        //Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(getContext(), "Signed In Successfully", Toast.LENGTH_SHORT).show();
            welcome.setVisibility(View.GONE);
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
                        welcome.setVisibility(View.GONE);
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
                                            (String)document.get("phone"),
                                            mAuth.getCurrentUser().getUid().trim());
                                }

                                if (createdAccount)
                                {
                                    sessionManager.setLogin(true);
                                    register.setVisibility(View.GONE);
                                    welcome.setVisibility(View.GONE);
                                    //start.setVisibility(View.VISIBLE);
                                    shopSelection.setVisibility(View.VISIBLE);
                                    cardViewStart.setVisibility(View.GONE);
                                    //scanProduct.setVisibility(View.VISIBLE);

                                }
                                else {
                                    welcome.setVisibility(View.GONE);
                                    String[] names = Objects.requireNonNull(account.getDisplayName()).split(" ");
                                    firstName.setText( names[0]);
                                    lastName.setText( names[1]);
                                    email.setText(account.getEmail());
                                    register.setVisibility(View.VISIBLE);
                                }


                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }

    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = context
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);

        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    private void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
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
    private void showCustomLoadingDialog() {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
            }
        }, 2000);
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            if (!localizationSet) {
                //loadingDialog.showDialog();
                //showCustomLoadingDialog();

                double longitude = loc.getLongitude();

                double latitude = loc.getLatitude();

                shopDistance.clear();
                for(Map.Entry<String,Shop> sHM : shopHashMap.entrySet())
                {
                    Shop temp = sHM.getValue();

                    Location loc1 = new Location("");
                    loc1.setLatitude(temp.getLatitude());
                    loc1.setLongitude(temp.getLongitude());

                    Location loc2 = new Location("");
                    loc2.setLatitude(latitude);
                    loc2.setLongitude(longitude);

                    double distanceInMeters = loc1.distanceTo(loc2);
                    shopDistance.put(temp.getShopCode(), distanceInMeters);
                }

                Map<String, Double> sortedDistance = sortByValue(shopDistance);

                Iterator<String> iterator = sortedDistance.keySet().iterator();
                String key = null;
                if(iterator.hasNext()){
                    key = iterator.next();
                }
                        Shop temp = shopHashMap.get(key);
                        //Log.e("TAG",temp.getDocumentID());
                        sessionManager.setScanShopSelect(temp.getDocumentID());
                        databaseHandler.resetShop();
                        //Log.e("TAG",temp.getName() + " " +temp.getLatitude()+ "," + temp.getLongitude() + " " +temp.getAddress() + " " +temp.getShopCode() + " " +temp.getDocumentID());
                        databaseHandler.addShop(temp.getName(),temp.getLatitude()+ "," + temp.getLongitude(),temp.getAddress(),temp.getShopCode(),temp.getDocumentID());
                        selected = true;
                        currentShop.setVisibility(View.VISIBLE);
                        cardViewStart.setVisibility(View.VISIBLE);
                        name.setText(temp.getName());
                        address.setText(temp.getAddress());
                        SplashTask task = new SplashTask();
                        task.execute();
                localizationSet = true;
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private static HashMap<String, Double> sortByValue(HashMap<String, Double> hm)
    {
        List<Map.Entry<String, Double> > list =
                new LinkedList<Map.Entry<String, Double> >(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


    class SplashTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {//showCustomLoadingDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @SuppressLint("UseSparseArrays")
        @Override
        protected Void doInBackground(Void... voids) {
            Query query = db.collection("shops");
            ListenerRegistration registration = query.addSnapshotListener(
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        }
                        // ...
                    });

            registration.remove();

            db.collection("shops")
                    .document(sessionManager.isScanShopSelect())
                    .collection("products")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {

                                return;
                            }
                            databaseHandler.resetProducts();
                            for (QueryDocumentSnapshot document : value) {
                                if (document.get("name") != null) {
                                    String name = (String) document.getData().get("name");
                                    String price = String.valueOf(document.getData().get("price"));
                                    String barcode = (String) document.getData().get("barcode");
                                    String promoEnd = (String) document.getData().get("promoEnd");
                                    String promoStart = (String) document.getData().get("promoStart");
                                    String discount = (String) String.valueOf(document.getData().get("discount"));
                                    String quantity = (String) String.valueOf(document.getData().get("quantity"));
                                    String category1 = (String) String.valueOf(document.getData().get("category[0]"));
                                    String category2 = (String) String.valueOf(document.getData().get("category[1]"));
                                    databaseHandler.addProduct(barcode, name, price,promoEnd,promoStart,discount,quantity, category1, category2);
                                }
                            }
                            sessionManager.setProduct(true);
                        }
                    });
            //loadingDialog.hideDialog();
            return null;

        }
    }

    public static final int PAYPAL_REQUEST_CODE = 123;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(
                    PayPalConfig.PAYPAL_CLIENT_ID);

}
