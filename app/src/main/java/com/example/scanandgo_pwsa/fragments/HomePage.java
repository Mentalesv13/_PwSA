package com.example.scanandgo_pwsa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        Button btnPriceReader = view.findViewById(R.id.btnScanCode);

        btnPriceReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragmentNoAnim(new PriceReader());
            }
        });

        return view;
    }
}
