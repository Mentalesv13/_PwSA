package com.example.scanandgo_pwsa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.scanandgo_pwsa.fragments.HomePage;
import com.example.scanandgo_pwsa.fragments.Menu;
import com.example.scanandgo_pwsa.fragments.Profile;
import com.example.scanandgo_pwsa.fragments.ScanAndGo;
import com.example.scanandgo_pwsa.fragments.SearchProduct;
import com.example.scanandgo_pwsa.fragments.SelectShop;
import com.example.scanandgo_pwsa.fragments.ShoppingList;
import com.example.scanandgo_pwsa.payments.PayPalConfirm;
import com.example.scanandgo_pwsa.welcome.ShopSelectSignIn;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView searchProduct, home, shoppingList, menu;
    String lastFragment;
    private CardView toolbar;
    FloatingActionButton fabScanGo, profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lastFragment = HomePage.class.getName();
        searchProduct = findViewById(R.id.iv_search);
        home = findViewById(R.id.iv_home);
        shoppingList = findViewById(R.id.iv_list);
        menu = findViewById(R.id.iv_menu);
        toolbar = findViewById(R.id.Toolbar);
        fabScanGo = findViewById(R.id.fabScanGo);
        profile = findViewById(R.id.profile);

        searchProduct.setOnClickListener(this);
        home.setOnClickListener(this);
        shoppingList.setOnClickListener(this);
        menu.setOnClickListener(this);
        shoppingList.setOnClickListener(this);
        fabScanGo.setOnClickListener(this);
        profile.setOnClickListener(this);

        Bundle b = getIntent().getExtras();
        if (b!=null) {
            if (b.getBoolean("select")) {
                toolbar.setVisibility(View.GONE);
                setFragmentNoAnim(new SelectShop());
            }
            else if (b.getBoolean("confirm"))
            {
                toolbar.setVisibility(View.GONE);
                setFragmentNoAnim(new PayPalConfirm(b.getString("PaymentDetails"), b.getString("PaymentAmount")));
            }
        }

    }

    public void setFragment(Fragment fragment) {
        String fragmentTag = fragment.getClass().getName();
        if (!fragmentTag.equals(lastFragment)) {
            lastFragment = fragmentTag;
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction ftx = fragmentManager.beginTransaction();
            ftx.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                    R.anim.enter_from_right, R.anim.exit_to_left);
            ftx.addToBackStack(null);
//            ftx.setCustomAnimations(R.anim.slide_in_right,
//                    R.anim.slide_out_left, R.anim.slide_in_left,
//            R.anim.slide_out_right);
            ftx.replace(R.id.fragmentContainer, fragment);
            ftx.commit();
        }
    }
    public void setFragmentNoAnim(Fragment fragment) {
        String fragmentTag = fragment.getClass().getName();
        if (!fragmentTag.equals(lastFragment)) {
            lastFragment = fragmentTag;
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction ftx = fragmentManager.beginTransaction();
            ftx.addToBackStack(null);
//            ftx.setCustomAnimations(R.anim.slide_in_right,
//                    R.anim.slide_out_left, R.anim.slide_in_left,
//            R.anim.slide_out_right);
            ftx.replace(R.id.fragmentContainer, fragment);
            ftx.commit();
        }
    }

    @Override
    public void onBackPressed() {
        toolbar.setVisibility(View.VISIBLE);
        if (!lastFragment.equals(HomePage.class.getName())) {
            setFragment(new HomePage());
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.iv_home:
                setFragmentNoAnim(new HomePage());
                toolbar.setVisibility(View.VISIBLE);
                break;

            case R.id.iv_list:
                setFragmentNoAnim(new ShoppingList());
                //toolbar.setVisibility(View.GONE);
                break;
            case R.id.iv_search:
                setFragment(new SearchProduct(false));
                //toolbar.setVisibility(View.GONE);
                break;
            case R.id.iv_menu:
                setFragment(new Menu());
                //toolbar.setVisibility(View.GONE);
                break;
            case R.id.fabScanGo:
                setFragmentNoAnim(new ScanAndGo(false));
                //toolbar.setVisibility(View.GONE);
                break;
            case R.id.profile:
                setFragment(new Profile());
                //toolbar.setVisibility(View.GONE);
                break;
        }
    }

    public void setToolbar(boolean visible)
    {
        if (visible)
        {toolbar.setVisibility(View.VISIBLE);}
        else {toolbar.setVisibility(View.GONE);}
    }
}
