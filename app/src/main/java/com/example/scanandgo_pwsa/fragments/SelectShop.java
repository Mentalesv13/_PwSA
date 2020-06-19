package com.example.scanandgo_pwsa.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.maps.MapsActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.ShopAdapter;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.ExampleItem;
import com.example.scanandgo_pwsa.model.Shop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SelectShop extends Fragment implements View.OnClickListener {

    private boolean flag, localizationSet;
    ArrayList<ExampleItem> exampleList;
    FirebaseFirestore db;
    LoadingDialog loadingDialog;
    HashMap<Integer, Shop> shopHashMap;
    HashMap<Integer, Double> shopDistance;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private ShopAdapter mAdapter;
    private Button btnSelect, btnGetLocation, btnMap;
    private static final String TAG = "SS_LOG";

    Context context;
    private SessionManager sessionManager;
    private DatabaseHandler databaseHandler;
    private TextView name, address;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.activity_select_shop, container, false);

        btnMap = view.findViewById(R.id.mapButton);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ContextCompat.checkSelfPermission(context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                {

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                else {
                    Bundle c = new Bundle();
                    c.putBoolean("select", false);
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtras(c);
                    startActivity(intent);
                    ((MainActivity)getActivity()).finish();
                }
            }
        });


        name = view.findViewById(R.id.name);
        address = view.findViewById(R.id.address);
        localizationSet = false;
        loadingDialog = new LoadingDialog(getActivity());
        shopDistance = new HashMap<>();
        context = getContext();
        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        databaseHandler = new DatabaseHandler(context);
        sessionManager = new SessionManager(context);

        db = FirebaseFirestore.getInstance();
        shopHashMap = new HashMap<>();

        loadingDialog.showDialog();

        btnSelect = view.findViewById(R.id.btnSelect);

        HashMap<String,String> temp = databaseHandler.getShopDetails();
        address.setText(temp.get("address"));
        name.setText(temp.get("shopname"));

        exampleList = new ArrayList<>();

        db.collection("shops")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int temp = 0;
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
                                shopHashMap.put(temp, new Shop(address,
                                        shopCode,
                                        longitude,
                                        latitude,
                                        name,
                                        0,
                                        document.getId()));
                                exampleList.add(new ExampleItem(R.drawable.ic_home,
                                        name, address,"", document.getId()));
                                temp++;
                                //moviesList.add(name);
                            }
                            mAdapter.notifyDataSetChanged();
                            loadingDialog.hideDialog();
                        }
                        else
                        {
                            Log.d("SearchProduct",
                                    "Error getting documents: ", task.getException());
                        }
                    }
                });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mAdapter = new ShopAdapter(exampleList,context);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(mLayoutManager);

        flag = false;

        btnGetLocation = (Button) view.findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, exampleList.get(sessionManager.isShopSet())
//                        .getDocumentID(),Toast.LENGTH_LONG).show();
                if (sessionManager.isShopSet()>-1) {
                    sessionManager.setShopSelect(exampleList.get(
                            sessionManager.isShopSet()).getDocumentID());
                    databaseHandler.resetShop();
                    databaseHandler.addShop(exampleList.get(sessionManager.isShopSet()).getText1(),
                            "0,0",exampleList.get(sessionManager.isShopSet()).getText2(),
                            "0",exampleList.get(sessionManager.isShopSet()).getDocumentID());

                    //HashMap<String,String> temp = databaseHandler.getShopDetails();
                    address.setText(exampleList.get(sessionManager.isShopSet()).getText2());
                    name.setText(exampleList.get(sessionManager.isShopSet()).getText1());

                    SplashTask task = new SplashTask();
                    task.execute();
                }
                else
                    Toast.makeText(context,"Please select your shop.", Toast.LENGTH_LONG).show();
            }
        });
        
        return view;
    }


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


                locationListener = new SelectShop.MyLocationListener();

                locationManager.requestLocationUpdates(LocationManager
                        .GPS_PROVIDER, 5000, 10, locationListener);

            } else {
                alertbox("Gps Status!!", "Your GPS is: OFF");
                locationListener = null;
            }
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

    protected void alertbox(String title, String mymessage) {
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
    public void showCustomLoadingDialog() {

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
                showCustomLoadingDialog();

                double longitude = loc.getLongitude();

                double latitude = loc.getLatitude();

                shopDistance.clear();
                ////Log.e(TAG, "size: " + shopHashMap.size());
                for (int i = 0; i < shopHashMap.size(); i++) {
                    Shop temp = shopHashMap.get(i);

                    Location loc1 = new Location("");
                    loc1.setLatitude(temp.getLatitude());
                    loc1.setLongitude(temp.getLongitude());

                    Location loc2 = new Location("");
                    loc2.setLatitude(latitude);
                    loc2.setLongitude(longitude);

                    double distanceInMeters = loc1.distanceTo(loc2);
                    ////Log.e(TAG, i + " " + distanceInMeters);
                    shopDistance.put(i, distanceInMeters);
                }

                Map<Integer, Double> sortedDistance = sortByValue(shopDistance);
                exampleList.clear();

                for (Map.Entry<Integer, Double> en : sortedDistance.entrySet()) {
                    ////Log.e(TAG, String.valueOf(en.getKey()));
                    Shop temp = shopHashMap.get(en.getKey());
                    double value = en.getValue()/1000.0;
                    exampleList.add(new ExampleItem(R.drawable.ic_home, temp.getName(),
                            temp.getAddress(),
                            String.format(Locale.ENGLISH,
                                    "%.1f",(value))+ "km",temp.getDocumentID()));
                }

                mAdapter = new ShopAdapter(exampleList,context);
                recyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();


                /*----------to get City-Name from coordinates ------------- */
                String cityName = null;
                Geocoder gcd = new Geocoder(context,
                        Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(loc.getLatitude(), loc
                            .getLongitude(), 1);
                    if (addresses.size() > 0)
                        cityName = addresses.get(0).getLocality();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //String s = longitude + "\n" + latitude + "\n\nMy Currrent City is: " + cityName;

                //Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                //loadingDialog.hideDialog();
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

    public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm)
    {
        List<Map.Entry<Integer, Double> > list =
                new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


    class SplashTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {showCustomLoadingDialog();
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
                    .document(sessionManager.isShopSelect())
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
                                    ////Log.e("TAG",document.getId() + " "+ document.getData());
                                    String name = (String) document.getData().get("name");
                                    String price = String.valueOf(document.getData().get("price"));
                                    String barcode = (String) document.getData().get("barcode");
                                    String promoEnd = (String) document.getData().get("promoEnd");
                                    String promoStart = (String) document.getData().get("promoStart");
                                    String discount = (String) String.valueOf(document.getData().get("discount"));
                                    String quantity = (String) String.valueOf(document.getData().get("quantity"));
                                    List<String> categories = (List<String>) document.get("category");
                                    String category1 = String.valueOf(categories.get(0));
                                    String category2 = String.valueOf(categories.get(1));
                                    ////Log.e("CATEGORY1: ",category1);
                                    ////Log.e("CATEGORY2: ",category2);
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

}
