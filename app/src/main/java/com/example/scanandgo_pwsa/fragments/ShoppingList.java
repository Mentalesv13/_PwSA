package com.example.scanandgo_pwsa.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.ShoppingListAdapter;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ShoppingList extends Fragment {

    private ConstraintLayout noProduct;
    private DatabaseHandler databaseHandler;
    private RecyclerView recyclerView;
    private List<String> productsList;
    private TextView discount, totalProducts, totalPrice;
    private HashMap<String, Product> products;
    private HashMap<String, com.example.scanandgo_pwsa.model.ShoppingList> shoppingList;
    private Double discountValue=0.0, totalProductsValue=0.0;
    private ImageView clearList;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setToolbar(false);
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        databaseHandler = new DatabaseHandler(getContext());
        discount = view.findViewById(R.id.discount);
        products = new HashMap<>();
        products = databaseHandler.getProductsDetails();
        totalPrice = view.findViewById(R.id.totalPrice);
        totalProducts = view.findViewById(R.id.totalProducts);
        clearList = view.findViewById(R.id.clearList);
        FloatingActionButton addProduct = view.findViewById(R.id.addProduct);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).setFragment(new SearchProduct(true));
            }
        });

        clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setMessage("Are you sure want to clear your shopping list?")
                        .setCancelable(false)
                        .setTitle("** Delete confirmation **")
                        .setPositiveButton("Clear",
                                new DialogInterface.OnClickListener() {
                                    @SuppressLint("SetTextI18n")
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        databaseHandler.resetList();
                                        dialog.cancel();
                                        updateValues();
                                        totalPrice.setText("0.00");
                                        discount.setText("0.00");
                                        totalProducts.setText("0.00");
                                        recyclerView.setVisibility(View.GONE);
                                        Toast.makeText(getContext(),"Products deleted from list",
                                                Toast.LENGTH_SHORT).show();
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
        });

        productsList = new ArrayList<>();
        shoppingList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        recyclerView = view.findViewById(R.id.recyclerView);

        noProduct = view.findViewById(R.id.noProduct);


        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
        decf.setRoundingMode(RoundingMode.HALF_UP);
        for (Map.Entry mapElement : shoppingList.entrySet()) {

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



        if (productsList.size()>0)
        {
         noProduct.setVisibility(View.GONE);
         clearList.setVisibility(View.VISIBLE);
        }
        else {
            noProduct.setVisibility(View.VISIBLE);
            clearList.setVisibility(View.GONE);
        }

        initAdapter();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getActivity())
                .getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }
    private void initAdapter() {

        ShoppingListAdapter recyclerAdapter = new ShoppingListAdapter(productsList, getContext(), getActivity(),
                this);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void updateValues() {
        discountValue = 0.0;
        totalProductsValue = 0.0;
        shoppingList = databaseHandler.getShoppingList();
        products = databaseHandler.getProductsDetails();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decf = new DecimalFormat("#####0.00", symbols);
        decf.setRoundingMode(RoundingMode.HALF_UP);
        if (shoppingList.size() == 0) {
            noProduct.setVisibility(View.VISIBLE);
            clearList.setVisibility(View.GONE);
            totalPrice.setText(decf.format(Double.parseDouble(decf.format(totalProductsValue)) -
                    Double.parseDouble(decf.format(discountValue))));
            discount.setText("- " + decf.format(discountValue));
            totalProducts.setText(decf.format(totalProductsValue));

        } else {
            noProduct.setVisibility(View.GONE);
            clearList.setVisibility(View.VISIBLE);
            for (Map.Entry mapElement : shoppingList.entrySet()) {

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
}