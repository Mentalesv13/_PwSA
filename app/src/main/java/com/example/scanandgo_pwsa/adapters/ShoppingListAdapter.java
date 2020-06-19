package com.example.scanandgo_pwsa.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanandgo_pwsa.R;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ShoppingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private Context context;
    private List<String> moviesListAll;
    private DatabaseHandler databaseHandler;
    private HashMap<String, Product> products;
    private StorageReference storageReference;
    private HashMap<String, ShoppingList> shoppingList;
    private LoadingDialog loadingDialog;
    private com.example.scanandgo_pwsa.fragments.ShoppingList fragment;


    public ShoppingListAdapter(List<String> allItem, Context context, Activity activity, com.example.scanandgo_pwsa.fragments.ShoppingList fragment)  {
        this.context = context;
        moviesListAll = new ArrayList<>();
        moviesListAll.addAll(allItem);
        databaseHandler = new DatabaseHandler(context);
        products  = new HashMap<>();
        products = databaseHandler.getProductsDetails();
        storageReference = FirebaseStorage.getInstance().getReference();
        shoppingList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        loadingDialog = new LoadingDialog((FragmentActivity) activity);
        this.fragment = fragment;
    }

    private void refreshAdapter() {
        shoppingList = databaseHandler.getShoppingList();
        products = databaseHandler.getProductsDetails();
        notifyDataSetChanged();
    }

        @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoplist_items, parent, false);
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
        return moviesListAll == null ? 0 : moviesListAll.size();
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
        return moviesListAll.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvItem, price, oldPrice, zloty1, zloty2, amount, toPay, tvAccessible, barcodes, promo;
        ImageView ivItem;
        CheckBox checked;
        ConstraintLayout productInfo;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccessible = itemView.findViewById(R.id.tvInaccessible);
            productInfo = itemView.findViewById(R.id.productInfo);
            checked = itemView.findViewById(R.id.checkBox);
            tvItem = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            oldPrice = itemView.findViewById(R.id.oldPrice);
            ivItem = itemView.findViewById(R.id.image);
            zloty1 = itemView.findViewById(R.id.zloty1);
            zloty2 = itemView.findViewById(R.id.zloty2);
            amount = itemView.findViewById(R.id.amount);
            toPay = itemView.findViewById(R.id.toPay);
            barcodes = itemView.findViewById(R.id.barcode);
            promo = itemView.findViewById(R.id.promo);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
           // Toast.makeText(view.getContext(), moviesListAll.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            showCustomLoadingDialog(getAdapterPosition());
        }


    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {

    }

    @SuppressLint("SetTextI18n")
    private void populateItemRows(final ItemViewHolder viewHolder, int position) {
        String item = moviesListAll.get(position);

        viewHolder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("TAG","zadymka");
                if (viewHolder.checked.isChecked())
                {
                    //Log.e("TAG","true");
                    //Log.e("TAG",viewHolder.barcodes.getText().toString());
                    viewHolder.productInfo.setAlpha(0.5f);
                    databaseHandler.updateIsBoughtByName(String.valueOf(viewHolder.tvItem.getText()),String.valueOf(true),String.valueOf(viewHolder.amount.getText()),String.valueOf(viewHolder.price.getText()));
                }
                else
                {
                    //Log.e("TAG","false");
                    viewHolder.productInfo.setAlpha(1.0f);
                    databaseHandler.updateIsBoughtByName(String.valueOf(viewHolder.tvItem.getText()),String.valueOf(false),String.valueOf(viewHolder.amount.getText()),String.valueOf(viewHolder.price.getText()));
                }
                shoppingList = databaseHandler.getShoppingList();
            }
        });

        ShoppingList temporary = shoppingList.get(item.trim());
        DecimalFormat decf = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.US));
        SimpleDateFormat dfOld = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat dfNew = new SimpleDateFormat("dd MMM yyyy");
        if (products.get(item.trim()) != null)
        {
            Product temp = products.get(item.trim());
            if (temp != null) {
                viewHolder.tvItem.setText(temp.getName());

                //Log.e("TAG", temp.getName() + " " + temp.getBarcode());

                viewHolder.tvAccessible.setVisibility(View.GONE);
                if (Double.parseDouble(temp.getDiscount().toString()) > 0) {
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy.MM.dd' 'HH:mm:ss");
                    String date = df.format(Calendar.getInstance().getTime());
                    try {
                        Date d1 = df.parse(date);

                        Date d2 = df.parse(temp.getPromoStart());

                        Date d3 = df.parse(temp.getPromoEnd());
                        if (((d1 != null ? d1.compareTo(d2) : 0) > 0) && (d1.compareTo(d3) < 0)) {
                            viewHolder.price.setText(decf.format(Double.parseDouble(temp.getPrice().toString()) -
                                    Double.parseDouble(temp.getPrice().toString()) *
                                            Double.parseDouble(temp.getDiscount().toString())));

                            viewHolder.oldPrice.setText(decf.format(temp.getPrice()) + "zł");
                            viewHolder.zloty1.setVisibility(View.VISIBLE);
                            viewHolder.zloty2.setVisibility(View.GONE);
                            viewHolder.promo.setVisibility(View.VISIBLE);
                            String tempString = "Promotions Ends ";
                            Date d4 = dfOld.parse(temp.getPromoEnd().split(" ")[0]);
                            String tempDate = dfNew.format(d4);
                            viewHolder.promo.setText(tempString + tempDate);

                        } else {
                            viewHolder.price.setText(decf.format(temp.getPrice()));
                            viewHolder.oldPrice.setVisibility(View.GONE);
                            viewHolder.zloty2.setVisibility(View.GONE);
                            viewHolder.promo.setVisibility(View.GONE);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    viewHolder.price.setText(temp.getPrice().toString());
                    viewHolder.oldPrice.setVisibility(View.GONE);
                    viewHolder.zloty2.setVisibility(View.GONE);
                    viewHolder.promo.setVisibility(View.GONE);
                }
                if (shoppingList.get(temp.getBarcode()) != null) {
                    ShoppingList tempSL = shoppingList.get(temp.getBarcode());

                    if (tempSL != null) {
                        viewHolder.amount.setText(String.valueOf(tempSL.getAmount()));


                    viewHolder.toPay.setText(decf.format(Double.parseDouble(String.valueOf(tempSL.getAmount())) * Double.parseDouble(String.valueOf(viewHolder.price.getText()))));

                    viewHolder.checked.setChecked(tempSL.isBought());
                    }
                    final RequestOptions requestOptions = new RequestOptions()
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder);
                    //Log.e(TAG, temp.getBarcode());
                    storageReference.child("products/" + temp.getBarcode() + ".jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(viewHolder.ivItem.getContext())
                                            .load(uri.toString())
                                            .apply(requestOptions)
                                            .into(viewHolder.ivItem);
                                }
                            });
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(1);
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    viewHolder.ivItem.setColorFilter(filter);

                    viewHolder.productInfo.setAlpha(1f);
                    viewHolder.checked.setAlpha(1f);
                    viewHolder.checked.setEnabled(true);

                    if (temporary != null) {
                        if (temporary.isBought()) {
                            viewHolder.productInfo.setAlpha(0.65f);
                        } else {
                            viewHolder.productInfo.setAlpha(1.0f);
                        }
                    }
                }
            }
        }
        else
        {
        if(temporary != null) {
            viewHolder.tvAccessible.setVisibility(View.VISIBLE);
            viewHolder.productInfo.setAlpha(0.65f);
            viewHolder.checked.setAlpha(0.65f);
            viewHolder.checked.setEnabled(false);
            viewHolder.tvItem.setText(temporary.getProductName());
            viewHolder.price.setText(temporary.getPrice());
            viewHolder.amount.setText(String.valueOf(temporary.getAmount()));
            viewHolder.oldPrice.setVisibility(View.GONE);
            viewHolder.zloty2.setVisibility(View.GONE);
            viewHolder.promo.setVisibility(View.GONE);
            viewHolder.barcodes.setVisibility(View.GONE);
            viewHolder.toPay.setText(decf.format(Double.valueOf(temporary.getPrice()) * temporary.getAmount()));

            final RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder);
            storageReference.child("products/" + temporary.getBarcode() + ".jpg").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(viewHolder.ivItem.getContext())
                                    .load(uri.toString())
                                    .apply(requestOptions)
                                    .into(viewHolder.ivItem);
                        }
                    });
            }
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            viewHolder.ivItem.setColorFilter(filter);
            viewHolder.ivItem.setAlpha(1f);
            //Log.e("TAG",item.trim());
            viewHolder.barcodes.setText(item.trim());
        }
    }

    @SuppressLint("SetTextI18n")
    private void buyLayout(final String product, final int position) {
        final Product temp = products.get(product.trim());

        final ShoppingList[] tempSL = new ShoppingList[1];
        tempSL[0] = shoppingList.get(product);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.list_item, null);

        TextView name = dialogView.findViewById(R.id.name);
        TextView price = dialogView.findViewById(R.id.price);
        ImageView image = dialogView.findViewById(R.id.image);
        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        final Button btnMark = dialogView.findViewById(R.id.btnMark);
        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        final CardView cardViewMark = dialogView.findViewById(R.id.cardViewMark);
        final CardView cardViewUpdate = dialogView.findViewById(R.id.cardViewUpdate);

        btnUpdate.setClickable(true);
        btnMark.setClickable(true);
        btnDelete.setClickable(true);

        if (products.get(product.trim()) != null) {

            final EditText editAmount = dialogView.findViewById(R.id.editText);
            if (tempSL[0] != null) {
                editAmount.setText(String.valueOf(tempSL[0].getAmount()));
            }
            btnMark.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    //showCustomLoadingDialog();
                    if (!String.valueOf(editAmount.getText()).equals("")) {
                        if (tempSL[0].isBought()) {
                            btnMark.setText("Mark as bought");
                            databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()), product,temp.getPrice().toString());
                        } else {
                            btnMark.setText("Mark as not bought");
                            databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()), product,temp.getPrice().toString());
                        }
                        refreshAdapter();
                        tempSL[0] = shoppingList.get(temp.getBarcode());
                    } else {
                        Toast.makeText(context, "Please insert product amount", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            if (temp != null) {
                name.setText(temp.getName());
            }
            final ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
            final ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);


            if (tempSL[0].isBought())
                btnMark.setText("Mark as not bought");
            else btnMark.setText("Mark as bought");

            btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount = amount + 1;
                    editAmount.setText(String.valueOf(amount));
                }
            });
            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount = amount - 1;
                    editAmount.setText(String.valueOf(amount));
                }
            });

            editAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count > 0) {
                        int amount = Integer.parseInt(String.valueOf(s));
                        if (amount > 1 && amount < 99) {
                            btnMinus.setVisibility(View.VISIBLE);
                            btnPlus.setVisibility(View.VISIBLE);
                        } else if (amount == 99) {
                            btnPlus.setVisibility(View.INVISIBLE);
                            btnMinus.setVisibility(View.VISIBLE);
                        } else if (amount > 99) {
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
            ConstraintLayout isOnListTrue = dialogView.findViewById(R.id.isOnListTrue);

            isOnListTrue.setVisibility(View.VISIBLE);

            DecimalFormat decf = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.US));
            if (Double.parseDouble(temp.getDiscount().toString()) > 0) {
                DateFormat df = new SimpleDateFormat("yyyy.MM.dd' 'HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());
                try {
                    Date d1 = df.parse(date);

                    Date d2 = df.parse(temp.getPromoStart());

                    Date d3 = df.parse(temp.getPromoEnd());

                    if ((d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                        price.setText(decf.format(Double.parseDouble(temp.getPrice().toString()) -
                                Double.parseDouble(temp.getPrice().toString()) *
                                        Double.parseDouble(temp.getDiscount().toString())) + "zł");
                    } else {
                        price.setText(decf.format(temp.getPrice())+ "zł");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                price.setText(decf.format(temp.getPrice()) + "zł");
            }

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder);


            StorageReference pathReference = storageReference.child("products/" + temp.getBarcode() + ".jpg");

            Glide.with(image.getContext())
                    .load(pathReference)
                    .apply(requestOptions)
                    .into(image);

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(1);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            image.setColorFilter(filter);

            dialogBuilder.setView(dialogView);
            //dialogBuilder.setTitle("Select date");
            dialogBuilder.setCancelable(true);


            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    final Button btnClose = dialogView.findViewById(R.id.btnClose);


                    btnClose.setOnClickListener(new View.OnClickListener() {
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
                                                    databaseHandler.deleteFromShoppingList(product);
                                                    refreshAdapter();
                                                    tempSL[0] = shoppingList.get(product);
                                                    dialog.cancel();
                                                    alertDialog.dismiss();
                                                    moviesListAll.remove(position);
                                                    fragment.updateValues();
                                                    Toast.makeText(context, "Product deleted from list", Toast.LENGTH_SHORT).show();
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
                            if (!String.valueOf(editAmount.getText()).equals("")) {
                                //Log.e("TAG", tempSL[0].getBarcode());
                                databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(tempSL[0].isBought()), String.valueOf(editAmount.getText()), product,temp.getPrice().toString());

                                refreshAdapter();
                                tempSL[0] = shoppingList.get(product);
                                alertDialog.dismiss();
                                fragment.updateValues();
                                Toast.makeText(context, "List updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Please insert product amount", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            alertDialog.show();
        }

        else
        {
            final EditText editAmount = dialogView.findViewById(R.id.editText);
            editAmount.setText(String.valueOf(tempSL[0].getAmount()));
            btnMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showCustomLoadingDialog();
                    if (!String.valueOf(editAmount.getText()).equals("")) {
                        if (tempSL[0].isBought()) {
                            btnMark.setText("Mark as bought");
                            databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()), product,temp.getPrice().toString());
                        } else {
                            btnMark.setText("Mark as not bought");
                            databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()), product,temp.getPrice().toString());
                        }
                        refreshAdapter();
                        tempSL[0] = shoppingList.get(temp.getBarcode());
                    } else {
                        Toast.makeText(context, "Please insert product amount", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            name.setText(tempSL[0].getProductName());
            final ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
            final ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);


            if (tempSL[0].isBought())
                btnMark.setText("Mark as not bought");
            else btnMark.setText("Mark as bought");

            btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount = amount + 1;
                    editAmount.setText(String.valueOf(amount));
                }
            });
            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int amount = Integer.parseInt(String.valueOf(editAmount.getText()));
                    amount = amount - 1;
                    editAmount.setText(String.valueOf(amount));
                }
            });

            editAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count > 0) {
                        int amount = Integer.parseInt(String.valueOf(s));
                        if (amount > 1 && amount < 99) {
                            btnMinus.setVisibility(View.VISIBLE);
                            btnPlus.setVisibility(View.VISIBLE);
                        } else if (amount == 99) {
                            btnPlus.setVisibility(View.INVISIBLE);
                            btnMinus.setVisibility(View.VISIBLE);
                        } else if (amount > 99) {
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
            ConstraintLayout isOnListTrue = dialogView.findViewById(R.id.isOnListTrue);

            isOnListTrue.setVisibility(View.VISIBLE);

            price.setText(tempSL[0].getPrice() + "zł");


            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder);


            cardViewMark.setVisibility(View.INVISIBLE);
            cardViewUpdate.setVisibility(View.INVISIBLE);
            btnPlus.setVisibility(View.INVISIBLE);
            btnMinus.setVisibility(View.INVISIBLE);
            editAmount.setEnabled(false);

            StorageReference pathReference = storageReference.child("products/" + tempSL[0].getBarcode() + ".jpg");

            Glide.with(image.getContext())
                    .load(pathReference)
                    .apply(requestOptions)
                    .into(image);

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            image.setColorFilter(filter);

            dialogBuilder.setView(dialogView);
            //dialogBuilder.setTitle("Select date");
            dialogBuilder.setCancelable(true);


            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    final Button btnClose = dialogView.findViewById(R.id.btnClose);


                    btnClose.setOnClickListener(new View.OnClickListener() {
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
                                                    databaseHandler.deleteFromShoppingList(product);
                                                    refreshAdapter();
                                                    tempSL[0] = shoppingList.get(product);
                                                    dialog.cancel();
                                                    alertDialog.dismiss();
                                                    moviesListAll.remove(position);
                                                    fragment.updateValues();
                                                    Toast.makeText(context, "Product deleted from list", Toast.LENGTH_SHORT).show();
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
                            if (!String.valueOf(editAmount.getText()).equals("")) {
                                databaseHandler.updateIsBought(tempSL[0].getProductName(), String.valueOf(tempSL[0].isBought()), String.valueOf(editAmount.getText()),product,tempSL[0].getPrice());

                                refreshAdapter();
                                tempSL[0] = shoppingList.get(product);
                                alertDialog.dismiss();
                                fragment.updateValues();
                                Toast.makeText(context, "List updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Please insert product amount", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            alertDialog.show();
        }
    }
    private void showCustomLoadingDialog(final int adapterPosition) {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                buyLayout(moviesListAll.get(adapterPosition), adapterPosition);
            }
        }, 500);
    }
    public void showCustomLoadingDialog() {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
            }
        }, 500);
    }


}