package com.example.scanandgo_pwsa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.welcome.FirstRun;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class SplashScreen extends AppCompatActivity {
    private DatabaseHandler databaseHandler;
    SessionManager session;
    private FirebaseFirestore db;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Thread splashTread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        databaseHandler = new DatabaseHandler(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        session = new SessionManager(getApplicationContext());
        session.setFirstRun(false);
        session.setShopSelect("SKLEP#2");

        SplashTask task = new SplashTask();
        task.execute();
        //StartAnimations();
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
//        anim.reset();
//        ConstraintLayout l = findViewById(R.id.lin_lay);
//        l.clearAnimation();
//        l.startAnimation(anim);

        ConstraintLayout constraintLayout = findViewById(R.id.lin_lay);
//        constraintLayout.clearAnimation();
//        constraintLayout.startAnimation(anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = findViewById(R.id.splashImage);
        iv.clearAnimation();
        iv.startAnimation(anim);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 4000) {
                        sleep(100);
                        waited += 100;
                    }

                    if(session.isFirstRun()){
                        Intent intent = new Intent(SplashScreen.this,
                                FirstRun.class);
                        session.setShop(-1);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(SplashScreen.this,
                                MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                    SplashScreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    SplashScreen.this.finish();
                }

            }
        };
        splashTread.start();
    }


    class SplashTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {StartAnimations();
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

            db.collection("categories")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {

                                return;
                            }

                            databaseHandler.resetCategory();
                            StringBuilder sb = new StringBuilder();
                            for (QueryDocumentSnapshot document : value) {
                                //Log.e("TAG",document.getId());
                                List<String> group = (List<String>) document.get("specific");

                                sb.append(document.getId() + "{");

                                for (int i=0; i<group.size();i++)
                                {
                                    sb.append(group.get(i));
                                    if(i+1<group.size())
                                        sb.append(",");
                                    else
                                        sb.append("}");

                                }
                                sb.append("/");
                                group.clear();


                                //Log.e("TAG",sb.toString());

                            }
                            databaseHandler.addCategory("Category", sb.toString());
                        }
                    });


            if(session.isShopSet()>-1) {
                db.collection("shops")
                        .document(session.isShopSelect())
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
                                        //Log.e("TAG",document.getId() + " "+ document.getData());
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
                                        //Log.e("CATEGORY1: ",category1);
                                        //Log.e("CATEGORY2: ",category2);
                                        databaseHandler.addProduct(barcode, name, price,promoEnd,promoStart,discount,quantity, category1, category2);
                                    }
                                }
                                session.setProduct(true);
                            }
                        });
                }
                return null;
            }
        }
}