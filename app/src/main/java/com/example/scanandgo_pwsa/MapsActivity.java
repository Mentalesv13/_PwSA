package com.example.scanandgo_pwsa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.welcome.ShopSelectSignIn;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        LocationListener     {

    class Shop{
        float lat;
        float lon;
        String name;
        String id;
        String address;
    }
    private SessionManager sessionManager;
    private LocationManager locationManager;
    private DatabaseHandler databaseHandler;
    public HashMap<String,Shop> shops = new HashMap<>();
    private GoogleMap mMap;
    private Activity activity;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        activity = this;
        sessionManager = new SessionManager(getBaseContext());
        databaseHandler = new DatabaseHandler(getBaseContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocation();

        db.collection("shops")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Shop temp = new Shop();
                                temp.name = document.getString("name");
                                temp.id = document.getId();
                                String [] coord = document.get("localization").toString().split(",");
                                temp.lat = Float.parseFloat(coord[0]);
                                temp.lon = Float.parseFloat(coord[1]);
                                temp.address = document.getString("address");
                                shops.put(document.getId(),temp);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(temp.lat, temp.lon))
                                        .title(temp.name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))


                                ).setTag(temp.id);

                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style));

        mMap.setOnMarkerClickListener(this);

    }

    void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        if(marker.getTag()!=Integer.valueOf(-1)){

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            final Shop temp = shops.get(marker.getTag());
            builder.setMessage("Are you sure want to select:\n\n"+ temp.name+ "\n" + temp.address)
                    .setCancelable(false)
                    .setTitle("** Shop selection **")
                    .setPositiveButton("Select",
                            new DialogInterface.OnClickListener() {
                                @SuppressLint("SetTextI18n")
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    sessionManager.setShopSelect(temp.id);
                                    databaseHandler.resetShop();
                                    databaseHandler.addShop(temp.name,
                                            "0,0",temp.address,
                                            "0",temp.id);
                                    SplashTask task = new SplashTask();
                                    task.execute();
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

            //Toast.makeText(this, marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onMyLocationButtonClick() {

        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

        String address="";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address=addresses.get(0).getAddressLine(0);

        }catch(Exception e)
        {

        }

        LatLng MyPosition = new LatLng(location.getLatitude(),location.getLongitude());
        Marker myMarker = mMap.addMarker(new MarkerOptions().
                position(MyPosition)
                .title(address)
                .snippet("you are there")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        myMarker.setTag(-1);
        myMarker.showInfoWindow();
        float zoomLevel = 12.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyPosition, zoomLevel));

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


    class SplashTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
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
                                    Log.e("TAG",document.getId() + " "+ document.getData());
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
                                    Log.e("CATEGORY1: ",category1);
                                    Log.e("CATEGORY2: ",category2);
                                    databaseHandler.addProduct(barcode, name, price,promoEnd,promoStart,discount,quantity, category1, category2);
                                }
                            }
                            sessionManager.setProduct(true);
                        }
                    });
            //loadingDialog.hideDialog();

            Bundle b = getIntent().getExtras();
            if (b!=null) {
                if (b.getBoolean("select")) {
                    Bundle c = new Bundle();
                    c.putBoolean("select", true);
                    Intent intent = new Intent(MapsActivity.this, ShopSelectSignIn.class);
                    intent.putExtras(c);
                    startActivity(intent);
                    MapsActivity.this.finish();
                } else {
                    Bundle c = new Bundle();
                    c.putBoolean("select", true);
                    Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                    intent.putExtras(c);
                    startActivity(intent);
                    MapsActivity.this.finish();
                }
            }



            return null;

        }
    }




}

