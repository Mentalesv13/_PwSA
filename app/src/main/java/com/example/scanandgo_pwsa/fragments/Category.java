package com.example.scanandgo_pwsa.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.CategoryAdapter;
import com.example.scanandgo_pwsa.adapters.RecyclerAdapter;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.model.Product;
import com.google.api.Distribution;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Category extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private CategoryAdapter recyclerAdapter;
    private boolean isLoading = false;
    private List<String> productsList;
    private List<String> rowsArrayList;
    private ConstraintLayout noProduct;
    private HorizontalScrollView categorySecScrollView, categoryMainScrollView;
    private ConstraintLayout cl;
    private HashMap<String, Product> products;
    private HashMap<String, String> category;
    private String[] categorie;
    private LoadingDialog loadingDialog;
    private String alreadySelected;
    private DatabaseHandler databaseHandler;
    private Fragment fragment;
    private boolean start = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment = this;
        view = inflater.inflate(R.layout.fragment_category, container, false);
        products = new HashMap<>();
        category = new HashMap<>();
        noProduct = view.findViewById(R.id.noProduct);
        databaseHandler = new DatabaseHandler(getContext());
        products = databaseHandler.getProductsDetail();
        category = databaseHandler.getCategoryDetails();
        categorySecScrollView = view.findViewById(R.id.hsv_category_secondary);
        categoryMainScrollView = view.findViewById(R.id.hsv_category_main);
        cl = view.findViewById(R.id.cl);
        categorySecScrollView.setVisibility(View.GONE);
        noProduct.setVisibility(View.GONE);
        loadingDialog = new LoadingDialog(getActivity());

        recyclerView = view.findViewById(R.id.recyclerView);

        rowsArrayList = new ArrayList<>();
        productsList = new ArrayList<>();
        showCustomLoadingDialogLong();
        for (Map.Entry mapElement : products.entrySet()) {

            Product temp = (Product) mapElement.getValue();
            productsList.add(temp.getName());
        }

        populateData();
        initAdapter();
        initScrollListener();

        for (Map.Entry mapElement : category.entrySet()) {
            //Log.e("TAG", (String) mapElement.getValue());
            final LinearLayout llmain = new LinearLayout(getActivity().getBaseContext());
            final ToggleButton showAll = new ToggleButton(getActivity().getBaseContext());
            showAll.setText("Show all");
            showAll.setTextOn("Show all");
            showAll.setTextOff("Show all");
            showAll.setAllCaps(false);
            showAll.setChecked(true);

            showAll.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    if (showAll.isChecked()) {
                        categorySecScrollView.setVisibility(View.GONE);
                        categorySecScrollView.removeAllViews();
                        products.clear();
                        rowsArrayList.clear();
                        productsList.clear();

                        products = databaseHandler.getProductsDetails();

                        for (Map.Entry mapElement : products.entrySet()) {

                            Product temp = (Product) mapElement.getValue();
                            productsList.add(temp.getName());
                        }

                        if (products.size() > 0) {
                            showCustomLoadingDialog();
                            noProduct.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            populateData2();
//                                                    initAdapter();
//                                                    initScrollListener();
                            recyclerAdapter.notifyDataSetChanged();
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            noProduct.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < llmain.getChildCount(); i++) {
                            ToggleButton child = (ToggleButton) llmain.getChildAt(i);
                            child.setChecked(false);
                        }
                        showAll.setChecked(true);

                    }
                    else
                    {
                        Toast.makeText(getActivity().getBaseContext(),"This category is already selected",Toast.LENGTH_LONG).show();
                        showAll.setChecked(true);
                    }
                }
            });
            llmain.addView(showAll);

            categorie = ((String) mapElement.getValue()).split("/");
            for (int i = 0; i < categorie.length; i++) {
                String[] temporary = categorie[i].split("\\{");
                final ToggleButton temp = new ToggleButton(getActivity().getBaseContext());
                temp.setId(i);
                temp.setText(temporary[0]);
                temp.setTextOff(temporary[0]);
                temp.setTextOn(temporary[0]);
                temp.setAllCaps(false);
                llmain.addView(temp);
                temp.setChecked(false);

                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (temp.isChecked()) {

                            products.clear();
                            rowsArrayList.clear();
                            productsList.clear();

                            products = databaseHandler.getProductsDetailsWithCategory1(temp.getText().toString());

                            for (Map.Entry mapElement : products.entrySet()) {

                                Product temp = (Product) mapElement.getValue();
                                productsList.add(temp.getName());
                            }

                            if (products.size() > 0) {
                                showCustomLoadingDialog();
                                noProduct.setVisibility(View.GONE);
                                categorySecScrollView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                populateData2();
//                                                    initAdapter();
//                                                    initScrollListener();
                                recyclerAdapter.notifyDataSetChanged();
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                noProduct.setVisibility(View.VISIBLE);
                                categorySecScrollView.setVisibility(View.GONE);
                            }

                            alreadySelected = temp.getText().toString();

                            for (int j = 0; j < categorie.length; j++) {



                                for (int i = 0; i < llmain.getChildCount(); i++) {
                                    ToggleButton child = (ToggleButton) llmain.getChildAt(i);
                                    child.setChecked(false);
                                }
                                temp.setChecked(true);

                                String categorySec = categorie[temp.getId()].substring(categorie[temp.getId()].indexOf("{") + 1, categorie[temp.getId()].indexOf("}"));
                                String[] categoriesSec = categorySec.split(",");

                                categorySecScrollView.removeAllViews();
                                final LinearLayout llSec = new LinearLayout(getActivity().getBaseContext());

                                final ToggleButton tempSecAll = new ToggleButton(getActivity().getBaseContext());
                                tempSecAll.setText("all");
                                tempSecAll.setAllCaps(false);
                                tempSecAll.setTextOff("all");
                                tempSecAll.setTextOn("all");
                                tempSecAll.setChecked(true);
                                tempSecAll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT));
                                tempSecAll.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (tempSecAll.isChecked()) {

                                            products.clear();
                                            rowsArrayList.clear();
                                            productsList.clear();
                                            products = databaseHandler.getProductsDetailsWithCategory1(alreadySelected);

                                            for (Map.Entry mapElement : products.entrySet()) {

                                                Product temp = (Product) mapElement.getValue();
                                                productsList.add(temp.getName());
                                            }

                                            if (products.size() > 0) {
                                                showCustomLoadingDialog();
                                                noProduct.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                                populateData2();
//                                                    initAdapter();
//                                                    initScrollListener();
                                                recyclerAdapter.notifyDataSetChanged();
                                            } else {
                                                recyclerView.setVisibility(View.GONE);
                                                noProduct.setVisibility(View.VISIBLE);
                                            }
                                            for (int i = 0; i < llSec.getChildCount(); i++) {
                                                ToggleButton child = (ToggleButton) llSec.getChildAt(i);
                                                child.setChecked(false);
                                            }
                                            tempSecAll.setChecked(true);
                                        } else {
                                            Toast.makeText(getActivity().getBaseContext(), "This category is already selected", Toast.LENGTH_LONG).show();
                                            tempSecAll.setChecked(true);
                                        }
                                    }
                                });
                                llSec.addView(tempSecAll);

                                for (String s : categoriesSec) {
                                    final ToggleButton tempSec = new ToggleButton(getActivity().getBaseContext());
                                    tempSec.setText(s);
                                    tempSec.setAllCaps(false);
                                    tempSec.setTextOff(s);
                                    tempSec.setTextOn(s);
                                    tempSec.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT));
                                    tempSec.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (tempSec.isChecked()) {

                                                products.clear();
                                                rowsArrayList.clear();
                                                productsList.clear();
                                                products = databaseHandler.getProductsDetailsWithCategory2(tempSec.getText().toString());

                                                for (Map.Entry mapElement : products.entrySet()) {

                                                    Product temp = (Product) mapElement.getValue();
                                                    productsList.add(temp.getName());
                                                }

                                                if (products.size() > 0) {
                                                    showCustomLoadingDialog();
                                                    noProduct.setVisibility(View.GONE);
                                                    recyclerView.setVisibility(View.VISIBLE);
                                                    populateData2();
//                                                        initAdapter();
//                                                        initScrollListener();
                                                    recyclerAdapter.notifyDataSetChanged();
                                                } else {
                                                    recyclerView.setVisibility(View.GONE);
                                                    noProduct.setVisibility(View.VISIBLE);
                                                }

                                                for (int i = 0; i < llSec.getChildCount(); i++) {
                                                    ToggleButton child = (ToggleButton) llSec.getChildAt(i);
                                                    child.setChecked(false);
                                                }
                                                tempSec.setChecked(true);

                                            }
                                            else {
                                                Toast.makeText(getActivity().getBaseContext(), "This category is already selected", Toast.LENGTH_LONG).show();
                                                tempSec.setChecked(true);
                                            }
                                        }
                                    });
                                    llSec.addView(tempSec);
                                }
                                categorySecScrollView.addView(llSec);
                            }
                        }
                        else {
                            Toast.makeText(getActivity().getBaseContext(), "This category is already selected", Toast.LENGTH_LONG).show();
                            temp.setChecked(true);
                        }
                    }

                });
            }
            categoryMainScrollView.addView(llmain);
        }

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    private void initAdapter() {
        recyclerAdapter = new CategoryAdapter(rowsArrayList,productsList, getContext(), getActivity(), fragment);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
        start = false;
    }

    private void populateData() {
        int i = 0;
        rowsArrayList.clear();
        while (i < 4 && i<productsList.size()) {
            rowsArrayList.add(productsList.get(i));
            i++;
        }
    }

    private void populateData2() {
        int i = 0;
        rowsArrayList.clear();
        while (i<productsList.size()) {
            rowsArrayList.add(productsList.get(i));
            i++;
        }
    }

    private void loadMore() {
        if (!start)
        if (rowsArrayList.size() != productsList.size()) {
            rowsArrayList.add(null);
            recyclerAdapter.notifyItemInserted(rowsArrayList.size() - 1);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.e("TAG", "TEST2");
                    rowsArrayList.remove(rowsArrayList.size() - 1);
                    int scrollPosition = rowsArrayList.size();
                    recyclerAdapter.notifyItemRemoved(scrollPosition);
                    int currentSize = scrollPosition;
                    int nextLimit = currentSize + 5;
                    if (nextLimit >= productsList.size())
                        nextLimit = productsList.size() - 1;
                    while (currentSize - 1 < nextLimit) {
                        rowsArrayList.add(productsList.get(currentSize));
                        currentSize++;
                    }
                    recyclerAdapter.notifyDataSetChanged();
                    isLoading =  true;
                }
            }, 1000);
        }
    }

    private void showCustomLoadingDialog() {
        recyclerView.setAlpha(0.10f);
        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                recyclerView.setAlpha(1f);
            }
        }, 750);
    }

    private void showCustomLoadingDialogLong() {
        recyclerView.setAlpha(0.10f);
        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                recyclerView.setAlpha(1f);
            }
        }, 1000);
    }


}
