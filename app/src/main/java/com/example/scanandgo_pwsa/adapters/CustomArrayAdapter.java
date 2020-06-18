package com.example.scanandgo_pwsa.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.model.ShoppingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> moviesList;
    private DatabaseHandler databaseHandler;
    private HashMap<String, ShoppingList> shoppingList;
    private HashMap<String, ShoppingList> sagList;

    public CustomArrayAdapter(@NonNull Context context, ArrayList<String> list) {
        super(context, 0 , list);
        mContext = context;
        moviesList = new ArrayList<>();
        databaseHandler = new DatabaseHandler(getContext());
        shoppingList = new HashMap<>();
        sagList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        sagList = databaseHandler.getScanAndGoShoppingList();
        for (Map.Entry product : shoppingList.entrySet())
        {
            String barcode = (String) product.getKey();
            //Log.e("TAG",barcode);

            if(sagList.get(barcode) == null)
            {
                //Log.e("TAG","Test");
                com.example.scanandgo_pwsa.model.ShoppingList temp =
                        (com.example.scanandgo_pwsa.model.ShoppingList) product.getValue();
                //Log.e("TAG",temp.getProductName());
                moviesList.add(temp.getProductName());
            }
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item_1,parent,false);

        String currentMovie = moviesList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.textView);
        name.setText(currentMovie);

        notifyDataSetChanged();

        return listItem;
    }
}