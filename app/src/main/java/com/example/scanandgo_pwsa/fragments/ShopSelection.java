package com.example.scanandgo_pwsa.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.Shop;
import com.example.scanandgo_pwsa.model.ShoppingList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ShopSelection extends Fragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private DatabaseHandler databaseHandler;
    private List<String> shopCodes;
    private HashMap<String, Shop> shopHashMap;
    private LoadingDialog loadingDialog;
    private FirebaseFirestore db;
    private SessionManager sessionManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_selection, container, false);
        shopHashMap = new HashMap<>();
        db = FirebaseFirestore.getInstance();
        mScannerView = view.findViewById(R.id.zxscan);
        databaseHandler = new DatabaseHandler(getContext());
        sessionManager = new SessionManager(getContext());
        shopCodes = new ArrayList<String>();
        loadingDialog = new LoadingDialog(getActivity());

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

                                shopCodes.add(shopCode);
                                //moviesList.add(name);
                            }
                        }
                        else
                        {
                            Log.d("SearchProduct",
                                    "Error getting documents: ", task.getException());
                        }
                    }
                });

        if ( ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, 1);
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        ((MainActivity)getActivity()).setFragment(new ScanAndGo(false));
                        return true;
                    }
                }
                return false;
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(final Result rawResult) {
        if(shopCodes.contains(rawResult.getText().trim())) {
            showCustomLoadingDialog(rawResult.getText());
            mScannerView.stopCameraPreview();

        }
        else {
            Toast.makeText(getActivity(), "No shop with this code ( or the code was read incorrectly )!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Try Again...!", Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScannerView.resumeCameraPreview(ShopSelection.this);
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    private void showCustomLoadingDialog(final String rawResult) {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Shop temp = shopHashMap.get(rawResult);
                sessionManager.setScanShopSelect(temp.getDocumentID());
                databaseHandler.resetShop();
                databaseHandler.addShop(temp.getName(),temp.getLatitude() + "," + temp.getLongitude(),temp.getAddress(),temp.getShopCode(),temp.getDocumentID());
                loadingDialog.hideDialog();
                ((MainActivity)getActivity()).setFragment(new ScanAndGo(true));
            }
        }, 500);
    }
    private void showCustomLoadingDialog() {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                mScannerView.resumeCameraPreview(ShopSelection.this);
            }
        }, 1000);
    }



}
