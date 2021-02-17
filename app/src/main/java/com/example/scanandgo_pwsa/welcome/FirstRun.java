package com.example.scanandgo_pwsa.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.Adapter;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FirstRun extends AppCompatActivity {

    ViewPager viewPager;
    Adapter adapter;
    List<Model> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);

        start= findViewById(R.id.btnStart);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstRun.this, ShopSelectSignIn.class);
                startActivity(intent);
                FirstRun.this.finish();
            }
        });

        models = new ArrayList<>();
        if(Locale.getDefault().getDisplayLanguage().equals("polski"))
            models.add(new Model(R.drawable.trackspendingpl, getString(R.string.Trackyourspending), getString(R.string.Usetheappandknow)));
        else
        models.add(new Model(R.drawable.trackspending, getString(R.string.Trackyourspending), getString(R.string.Usetheappandknow)));
        models.add(new Model(R.drawable.scanpaygop, "Scan&Go", getString(R.string.Saygoodbye)));
        models.add(new Model(R.drawable.planyourlist, getString(R.string.Planyourshopping), getString(R.string.Usetheshopping)));
        models.add(new Model(R.drawable.discount, getString(R.string.DiscountL), getString(R.string.Buydiscountedproducts)));

        adapter = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(75, 0, 75, 0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4)
        };

        colors = colors_temp;

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(

                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }

                else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
