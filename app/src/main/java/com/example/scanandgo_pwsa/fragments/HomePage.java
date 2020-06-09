package com.example.scanandgo_pwsa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;

import java.util.Objects;


public class HomePage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        CardView cvScanGo = view.findViewById(R.id.tScanGo);
        CardView cvPriceReader = view.findViewById(R.id.tPrice);
        CardView cvShopping = view.findViewById(R.id.tShopping);
        Button btnPriceReader = view.findViewById(R.id.btnPriceReader);
        Button btnScanAndGo = view.findViewById(R.id.btnScanCode);
        Button btnShoppingList = view.findViewById(R.id.btnShopping);

        btnPriceReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new PriceReader());
            }
        });
        btnScanAndGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ScanAndGo(false));
            }
        });

        btnShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ShoppingList());
            }
        });

        cvPriceReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new PriceReader());
            }
        });
        cvScanGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ScanAndGo(false));
            }
        });

        cvShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new ShoppingList());
            }
        });



        return view;
    }
}
