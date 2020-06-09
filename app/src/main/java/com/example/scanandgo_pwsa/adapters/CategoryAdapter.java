package com.example.scanandgo_pwsa.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.fragments.SearchProduct;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.model.Product;
import com.example.scanandgo_pwsa.model.ShoppingList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private Context context;
    private List<String> moviesList;
    private List<String> moviesListAll;
    private DatabaseHandler databaseHandler;
    private HashMap<String, Product> products;
    private StorageReference storageReference;
    private HashMap<String, ShoppingList> shoppingList;
    private LoadingDialog loadingDialog;
    private Activity activity;
    private Fragment fragment;

    public CategoryAdapter(List<String> itemList, List<String> allItem, Context context, Activity activity, Fragment fragment)  {
        this.context = context;
        this.moviesList = itemList;
        moviesListAll = new ArrayList<>();
        moviesListAll.addAll(allItem);
        databaseHandler = new DatabaseHandler(context);
        products  = new HashMap<>();
        products = databaseHandler.getProductsDetail();
        storageReference = FirebaseStorage.getInstance().getReference();
        shoppingList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        loadingDialog = new LoadingDialog((FragmentActivity) activity);
        this.activity = activity;
        this.fragment = fragment;
    }

        @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_items, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {

            populateItemRows((ItemViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    @Override
    public int getItemCount() {
        return moviesList == null ? 0 : moviesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return moviesList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            List<String> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(moviesListAll);
            } else {
                for (String movie: moviesListAll) {
                    if (movie.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(movie);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            moviesList.clear();
            //moviesList.addAll((Collection<? extends String>) filterResults.values);
            ArrayList <String> searchList = new ArrayList<>();
            searchList.addAll((Collection<? extends String>) filterResults.values);
            int i = 0;
            while (i < 20) {
                if (i < searchList.size()){
                moviesList.add(searchList.get(i));
                i++;}
                else {break;}
            }
            notifyDataSetChanged();
        }
    };


    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvItem, tvItem2, tvItem3, zloty1, zloty2, promo;
        ImageView ivItem;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.name);
            tvItem2 = itemView.findViewById(R.id.price);
            tvItem3 = itemView.findViewById(R.id.oldPrice);
            ivItem = itemView.findViewById(R.id.image);
            zloty1 = itemView.findViewById(R.id.zloty1);
            zloty2 = itemView.findViewById(R.id.zloty2);
            promo = itemView.findViewById(R.id.promo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
           // Toast.makeText(view.getContext(), moviesList.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            showCustomLoadingDialog(getAdapterPosition());
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    @SuppressLint("SetTextI18n")
    private void populateItemRows(final ItemViewHolder viewHolder, int position) {
        String item = moviesList.get(position);
        viewHolder.tvItem.setText(item);
        Log.e("TEST", item);
        if (products.get(item.trim()) != null)
        {
            Product temp = products.get(item.trim());

            Log.e("TEST", temp.getName());
            Log.e("TEST", temp.getDiscount().toString());

            if (Double.parseDouble(temp.getDiscount().toString()) > 0) {
                DateFormat df = new SimpleDateFormat("yyyy.MM.dd' 'HH:mm:ss");
                SimpleDateFormat dfOld = new SimpleDateFormat("yyyy.MM.dd");
                SimpleDateFormat dfNew = new SimpleDateFormat("dd MMM yyyy");
                String date = df.format(Calendar.getInstance().getTime());
                try {
                    Date d1 = df.parse(date);

                    Date d2 = df.parse(temp.getPromoStart());

                    Date d3 = df.parse(temp.getPromoEnd());
                    DecimalFormat decf = new DecimalFormat("####0.00", new DecimalFormatSymbols(Locale.US));

                    if ((d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                        viewHolder.tvItem2.setText(decf.format(Double.parseDouble(temp.getPrice().toString()) -
                                Double.parseDouble(temp.getPrice().toString()) *
                                        Double.parseDouble(temp.getDiscount().toString())));

                        viewHolder.tvItem3.setText(decf.format(temp.getPrice()) + " zł");
                        viewHolder.zloty1.setVisibility(View.VISIBLE);
                        viewHolder.tvItem3.setVisibility(View.VISIBLE);
                        viewHolder.zloty2.setVisibility(View.GONE);
                        viewHolder.promo.setVisibility(View.VISIBLE);
                        String tempString = "Promotions Ends ";
                        Date d4 = dfOld.parse(temp.getPromoEnd().split(" ")[0]);
                        String tempDate = dfNew.format(d4);
                        Log.e("TAG", "test3");
                        viewHolder.promo.setText(tempString + tempDate);

                    } else {
                        viewHolder.tvItem2.setText(decf.format(temp.getPrice()));
                        viewHolder.tvItem3.setVisibility(View.GONE);
                        viewHolder.zloty2.setVisibility(View.GONE);
                        viewHolder.promo.setVisibility(View.GONE);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                viewHolder.tvItem2.setText(temp.getPrice().toString());
                viewHolder.tvItem3.setVisibility(View.GONE);
                viewHolder.zloty2.setVisibility(View.GONE);
                viewHolder.promo.setVisibility(View.GONE);
            }
            //Picasso.load("https://www.officeroom.pl/environment/cache/images/0_0_productGfx_22066/dfdb2777cf2bc063e9188e28245a6a03.jpg").into(viewHolder.ivItem);
            final RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder);
            Log.e(TAG, temp.getBarcode());
            storageReference.child("products/" + temp.getBarcode() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(viewHolder.ivItem.getContext())
                            .load(uri.toString())
                            .apply(requestOptions)
                            .into(viewHolder.ivItem);
                }
            });

//        StorageReference pathReference = storageReference.child("products/"+temp.getBarcode()+".jpg");
//
//        Glide.with(viewHolder.ivItem.getContext())
//                .load(pathReference)
//                .apply(requestOptions)
//                .into(viewHolder.ivItem);
        }
    }

    private void buyLayout(final String product, final int position) {
        final Product temp = products.get(product.trim());

        final ShoppingList[] tempSL = new ShoppingList[1];
        tempSL[0] = shoppingList.get(temp.getBarcode());
        final String barcode = temp.getBarcode();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.row_item, null);
        final ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
        final ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);
        final EditText editAmount = dialogView.findViewById(R.id.editText);
        final Button btnAdd =dialogView.findViewById(R.id.btnAdd);
        final Button btnMark =dialogView.findViewById(R.id.btnMark);
        if (shoppingList.get(temp.getBarcode())==null) {
            btnMark.setText("Mark as bought");
        }
        else {
            if (tempSL[0].isBought()) {
                btnMark.setText("Mark as not bought");
            } else {
                btnMark.setText("Mark as bought");
            }
        }

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!String.valueOf(editAmount.getText()).equals(""))
                {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount= amount + 1;
                    editAmount.setText(String.valueOf(amount));
                }
            }
        });
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!String.valueOf(editAmount.getText()).equals("")) {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount = amount - 1;
                    editAmount.setText(String.valueOf(amount));
                }
            }
        });

        editAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>0) {
                    int amount = Integer.parseInt(String.valueOf(s));
                    if (amount > 1 && amount < 99) {
                        btnMinus.setVisibility(View.VISIBLE);
                        btnPlus.setVisibility(View.VISIBLE);
                    } else if (amount == 99) {
                            btnPlus.setVisibility(View.INVISIBLE);
                        btnMinus.setVisibility(View.VISIBLE);
                    }else if (amount > 99) {
                        btnPlus.setVisibility(View.INVISIBLE);
                        btnMinus.setVisibility(View.VISIBLE);
                        editAmount.setText("99");
                    } else if (amount == 1) {
                        btnMinus.setVisibility(View.INVISIBLE);
                        btnPlus.setVisibility(View.VISIBLE);
                    } else {
                        btnMinus.setVisibility(View.INVISIBLE);
                        editAmount.setText("1");
                        btnPlus.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showCustomLoadingDialog();
                if (!String.valueOf(editAmount.getText()).equals("")) {
                    tempSL[0] = shoppingList.get(barcode);
                    if (tempSL[0].isBought()) {
                        btnMark.setText("Mark as bought");
                        databaseHandler.updateIsBought(temp.getName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString());
                    } else {
                        btnMark.setText("Mark as not bought");
                        databaseHandler.updateIsBought(temp.getName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString());
                    }
                    refreshAdapter();
                    tempSL[0] = shoppingList.get(temp.getBarcode());
                }
                else {Toast.makeText(context,"Please insert product amount",Toast.LENGTH_SHORT).show();}
            }
        });

        TextView name = dialogView.findViewById(R.id.name);
        TextView price = dialogView.findViewById(R.id.price);
        ImageView image = dialogView.findViewById(R.id.image);
        final TextView infoList = dialogView.findViewById(R.id.listInfo);

        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnUpdate.setClickable(true);
        btnMark.setClickable(true);
        btnDelete.setClickable(true);
        name.setText(product);
        final ConstraintLayout isOnListTrue = dialogView.findViewById(R.id.isOnListTrue);
        final ConstraintLayout isOnListFalse = dialogView.findViewById(R.id.isOnListFalse);

        if(shoppingList.get(temp.getBarcode())==null) {
            isOnListFalse.setVisibility(View.VISIBLE);
            isOnListTrue.setVisibility(View.GONE);
            infoList.setText("This product is not on the shopping list");
            editAmount.setText("1");

        }
        else {
            isOnListFalse.setVisibility(View.GONE);
            isOnListTrue.setVisibility(View.VISIBLE);
            infoList.setText("This product is already on shopping list");
            tempSL[0] = shoppingList.get(temp.getBarcode());
            editAmount.setText(String.valueOf(tempSL[0].getAmount()));
        }

        if (String.valueOf(editAmount.getText()).equals("1")){
            btnMinus.setVisibility(View.INVISIBLE);
        }
        else if (String.valueOf(editAmount.getText()).equals("99")){
            btnPlus.setVisibility(View.INVISIBLE);
        }

        if (Double.parseDouble(temp.getDiscount().toString())>0)
        {
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd' 'HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
            try {
                Date d1 = df.parse(date);

                Date d2 = df.parse(temp.getPromoStart());

                Date d3 = df.parse(temp.getPromoEnd());
                DecimalFormat decf = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.US));

                if ((d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                    price.setText(decf.format(Double.parseDouble(temp.getPrice().toString())-
                            Double.parseDouble(temp.getPrice().toString())*
                                    Double.parseDouble(temp.getDiscount().toString())) + "zł");
                }
                else {
                    price.setText(decf.format(temp.getPrice()) + "zł");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            price.setText(temp.getPrice().toString() + "zł");
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder);


        StorageReference pathReference = storageReference.child("products/"+temp.getBarcode()+".jpg");

        Glide.with(image.getContext())
                .load(pathReference)
                .apply(requestOptions)
                .into(image);

        dialogBuilder.setView(dialogView);
        //dialogBuilder.setTitle("Select date");
        dialogBuilder.setCancelable(true);


        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setVisibility(View.GONE);

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(String.valueOf(editAmount.getText()).equals("")){
                            Toast.makeText(context,"Please insert product amount.",Toast.LENGTH_SHORT).show();}
                        else{
                            databaseHandler.addToList(product,String.valueOf(editAmount.getText()),"false",temp.getBarcode(),temp.getPrice().toString());
                            tempSL[0] = shoppingList.get(temp.getBarcode());
                            infoList.setText("This product is already on shopping list");
                           isOnListTrue.setVisibility(View.VISIBLE);
                           isOnListFalse.setVisibility(View.GONE);
                           Toast.makeText(context,"Product added to list successfully.",Toast.LENGTH_SHORT).show();
                           refreshAdapter();

                        }
                    }
                });



                final Button btnClose = dialogView.findViewById(R.id.btnClose);
                final Button btnClose2 = dialogView.findViewById(R.id.btnClose2);

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                btnClose2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //showCustomLoadingDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Are you sure to delete this product from your shopping list?")
                                .setCancelable(false)
                                .setTitle("** Delete confirmation **")
                                .setPositiveButton("Delete",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                databaseHandler.deleteFromShoppingList(temp.getBarcode());
                                                refreshAdapter();
                                                //tempSL[0] = shoppingList.get(product);
                                                dialog.cancel();
                                                alertDialog.dismiss();
                                                moviesListAll.remove(position);
                                                Toast.makeText(context,"Product deleted from list",Toast.LENGTH_SHORT).show();
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

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //showCustomLoadingDialog();
                        if(!String.valueOf(editAmount.getText()).equals("")){
                            tempSL[0] = shoppingList.get(product);
                            databaseHandler.updateIsBought(temp.getName(), String.valueOf(tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString());
                            refreshAdapter();
                            tempSL[0] = shoppingList.get(product);
                            alertDialog.dismiss();
                            Toast.makeText(context,"List updated successfully",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context,"Please insert product amount",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        alertDialog.show();
    }

    private void showCustomLoadingDialog(final int adapterPosition) {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                buyLayout(moviesList.get(adapterPosition), adapterPosition);
            }
        }, 500);
    }
    private void refreshAdapter() {
        shoppingList = databaseHandler.getShoppingList();
        //products = databaseHandler.getProductsDetail();
        notifyDataSetChanged();
    }
}