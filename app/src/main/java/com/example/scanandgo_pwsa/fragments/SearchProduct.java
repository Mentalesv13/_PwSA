package com.example.scanandgo_pwsa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.RecyclerAdapter;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchProduct extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private boolean isLoading = false;
    private List<String> productsList;
    private List<String> rowsArrayList;
    private List<String> searchArrayList;
    private ConstraintLayout noProduct;
    private short searchActive = 0;
    private HashMap<String, Product> products;

    private DatabaseHandler databaseHandler;
    private Fragment fragment;

    private boolean searchOn;

    public SearchProduct( boolean searchOn) {
        this.searchOn = searchOn;
    }

    public boolean isSearchOn() {
        return searchOn;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setToolbar(false);
        fragment = this;
        view = inflater.inflate(R.layout.searchproduct, container, false);
        products = new HashMap<>();
        databaseHandler = new DatabaseHandler(getContext());
        products = databaseHandler.getProductsDetail();

        productsList = new ArrayList<>();
        noProduct = view.findViewById(R.id.noProduct);
        noProduct.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.recyclerView);

        rowsArrayList = new ArrayList<>();
        searchArrayList = new ArrayList<>();

        for (Map.Entry mapElement : products.entrySet()) {
            String key = (String)mapElement.getKey();

            // Add some bonus marks
            // to all the students and print it
            Product temp = (Product) mapElement.getValue();
            productsList.add(temp.getName());
        }

        //populateData();
        initAdapter();
        initScrollListener();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        final SearchView searchView = view.findViewById(R.id.productSearch);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>0) {
                    searchArrayList.clear();
                    for (String movie: productsList) {
                        if (movie.toLowerCase().contains(query.toLowerCase())) {
                            searchArrayList.add(movie);
                        }
                    }
                    searchActive = 1;
                    recyclerAdapter = new RecyclerAdapter(searchArrayList,productsList, getContext(), getActivity(), fragment);
                    recyclerView.setAdapter(recyclerAdapter);
                    if (searchArrayList.size()>0)
                        noProduct.setVisibility(View.GONE);
                    else
                        noProduct.setVisibility(View.VISIBLE);
                }
                else{
                    searchActive = 2;
                    }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        SearchView.OnCloseListener onCloseListener = new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                searchActive = 0;
                rowsArrayList.clear();
                return false;
            }
        };
        searchView.setOnCloseListener(onCloseListener);

        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.requestFocusFromTouch();
        searchView.setIconified(false);
        searchView.setQueryHint("Search for products");

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);

        // Set on click listener
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Find EditText view
                EditText et = view.findViewById(R.id.search_src_text);

                //Clear the text from EditText view
                et.setText("");

                //Clear query
                searchView.setQuery("", false);
                //Collapse the action view
                searchView.onActionViewCollapsed();
                rowsArrayList.clear();
                //Collapse the search widget
            }
        });

        return view;
    }

    private void initAdapter() {
        recyclerAdapter = new RecyclerAdapter(rowsArrayList,productsList, getContext(), getActivity(), fragment);
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

    private void loadMore() {
        //Log.e("TAG", "TEST1");
        if (searchActive==0) {
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
                        int nextLimit = currentSize + 20;
                        if (nextLimit >= productsList.size())
                            nextLimit = productsList.size() - 1;
                        while (currentSize - 1 < nextLimit) {
                            rowsArrayList.add(productsList.get(currentSize));
                            currentSize++;
                        }

                        recyclerAdapter.notifyDataSetChanged();
                        isLoading = false;
                    }
                }, 1000);

            }
        }
        else if (searchActive==1)
        {
            if (rowsArrayList.size() != searchArrayList.size()) {
                rowsArrayList.add(null);
                recyclerAdapter.notifyItemInserted(rowsArrayList.size() - 1);


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (rowsArrayList.size()>0)
                        rowsArrayList.remove(rowsArrayList.size() - 1);
                        int scrollPosition = rowsArrayList.size();
                        recyclerAdapter.notifyItemRemoved(scrollPosition);
                        int currentSize = scrollPosition;
                        int nextLimit = currentSize + 20;
                        if (nextLimit >= searchArrayList.size())
                            nextLimit = searchArrayList.size() - 1;
                        while (currentSize - 1 < nextLimit) {
                            rowsArrayList.add(searchArrayList.get(currentSize));
                            currentSize++;
                        }

                        recyclerAdapter.notifyDataSetChanged();
                        isLoading = false;
                    }
                }, 1000);

            }
        }

    }

}
