package com.example.scanandgo_pwsa.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.PromoAdapter;
import com.example.scanandgo_pwsa.adapters.RecyclerAdapter;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.model.Product;

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

public class Promotions extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private PromoAdapter recyclerAdapter;
    private boolean isLoading = false;
    private List<String> productsList;
    private List<String> rowsArrayList;
    private ConstraintLayout noProduct;
    private HashMap<String, Product> products;

    private DatabaseHandler databaseHandler;
    private Fragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setToolbar(false);
        fragment = this;
        view = inflater.inflate(R.layout.fragment_promo, container, false);
        products = new HashMap<>();
        databaseHandler = new DatabaseHandler(getContext());
        products = databaseHandler.getProductsDetail();

        productsList = new ArrayList<>();
        noProduct = view.findViewById(R.id.noProduct);
        noProduct.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.recyclerView);

        rowsArrayList = new ArrayList<>();

        for (Map.Entry mapElement : products.entrySet()) {
            // Add some bonus marks
            // to all the students and print it
            Product temp = (Product) mapElement.getValue();
            if (Double.parseDouble(temp.getDiscount().toString())>0)
            {
                DateFormat df = new SimpleDateFormat("yyyy.MM.dd' 'HH:mm:ss");
                SimpleDateFormat dfOld = new SimpleDateFormat("yyyy.MM.dd");
                SimpleDateFormat dfNew = new SimpleDateFormat("dd MMM yyyy");
                String date = df.format(Calendar.getInstance().getTime());
                try {
                    Date d1 = df.parse(date);

                    Date d2 = df.parse(temp.getPromoStart());

                    Date d3 = df.parse(temp.getPromoEnd());
                    DecimalFormat decf = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.US));

                    if ((d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {

                        productsList.add(temp.getName());
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

        }

        populateData();
        initAdapter();
        initScrollListener();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    private void initAdapter() {
        recyclerAdapter = new PromoAdapter(rowsArrayList,productsList, getContext(), getActivity(), fragment);
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
    }

    private void populateData() {
        int i = 0;
        rowsArrayList.clear();
        while (i < 10 && i<productsList.size()) {
            rowsArrayList.add(productsList.get(i));
            i++;
        }
    }

    private void loadMore() {
        if (rowsArrayList.size() != productsList.size()) {
            rowsArrayList.add(null);
            recyclerAdapter.notifyItemInserted(rowsArrayList.size() - 1);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("TAG", "TEST2");
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


}
