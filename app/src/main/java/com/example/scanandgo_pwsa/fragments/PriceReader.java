package com.example.scanandgo_pwsa.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.example.scanandgo_pwsa.helper.LoadingDialog;
import com.example.scanandgo_pwsa.model.Product;
import com.example.scanandgo_pwsa.model.ShoppingList;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PriceReader extends Fragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private DatabaseHandler databaseHandler;
    private HashMap<String, Product> products;
    private StorageReference storageReference;
    private HashMap<String, ShoppingList> shoppingList;
    private LoadingDialog loadingDialog;
    private Button insertCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_reader, container, false);
        mScannerView = view.findViewById(R.id.zxscan);
        databaseHandler = new DatabaseHandler(getContext());
        products  = new HashMap<>();
        loadingDialog = new LoadingDialog(getActivity());
        products = databaseHandler.getProductsDetails();
        storageReference = FirebaseStorage.getInstance().getReference();
        shoppingList = new HashMap<>();
        shoppingList = databaseHandler.getShoppingList();
        insertCode = view.findViewById(R.id.button);

        if ( ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, 1);
        }

        insertCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScannerView.stopCameraPreview();
                showBarcodeInsertDialog();
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK) {
//                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "TESKT",Toast.LENGTH_SHORT).show();
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
        return view;
    }

    private void showBarcodeInsertDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.barcode_insert, null);
        final Button cancel = dialogView.findViewById(R.id.btnCancel);
        final Button apply = dialogView.findViewById(R.id.btnApply);
        final EditText barcode = dialogView.findViewById(R.id.barcode);
        //dialogBuilder.setTitle("Insert product barcode ( EAN-13 )");
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final AlertDialog alertDialog = dialogBuilder.create();

                apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.e("TAG","click");
                        if(products.get(barcode.getText().toString().trim())!=null) {
                            showCustomLoadingDialog(barcode.getText().toString());
                        }
                        else {
                            Toast.makeText(getContext(), "No product with this code!", Toast.LENGTH_LONG).show();
                            Toast.makeText(getContext(), "Try Again...!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.setCancelable(true);
                        alertDialog.dismiss();
                        dialog.cancel();
                        mScannerView.resumeCameraPreview(PriceReader.this);
                    }
                });
            }
        });
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(final Result rawResult) {
        if(products.get(rawResult.getText())!=null) {
            showCustomLoadingDialog(rawResult.getText());
            mScannerView.stopCameraPreview();
        }
        else {
            Toast.makeText(getActivity(), R.string.NoProductWithCode, Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity(), R.string.TryAgain, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScannerView.resumeCameraPreview(PriceReader.this);
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    private void buyLayout(final String product) {
        final Product temp = products.get(product.trim());

        final ShoppingList[] tempSL = new ShoppingList[1];
        final String barcode = temp.getBarcode();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.row_item, null);
        final ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
        final ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);
        final EditText editAmount = dialogView.findViewById(R.id.editText);
        final Button btnAdd =dialogView.findViewById(R.id.btnAdd);
        final Button btnMark =dialogView.findViewById(R.id.btnMark);


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
                tempSL[0] = shoppingList.get(barcode);
                if (!String.valueOf(editAmount.getText()).equals("")) {
                    if (tempSL[0].isBought()) {
                        btnMark.setText(R.string.mark_as_bought);
                        databaseHandler.updateIsBought(temp.getName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString());
                    } else {
                        btnMark.setText(R.string.Mark_as_not_bought);
                        databaseHandler.updateIsBought(temp.getName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString()); }
                    refreshAdapter();
                    tempSL[0] = shoppingList.get(product);
                }
                else {Toast.makeText(getContext(), R.string.pleaseInsertProductAmount,Toast.LENGTH_SHORT).show();}
            }
        });

        TextView name = dialogView.findViewById(R.id.name);
        TextView price = dialogView.findViewById(R.id.price);
        ImageView image = dialogView.findViewById(R.id.image);
        TextView infoList = dialogView.findViewById(R.id.listInfo);

        final Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        final Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnUpdate.setClickable(true);
        btnMark.setClickable(true);
        btnDelete.setClickable(true);
        name.setText(temp.getName());
        final ConstraintLayout isOnListTrue = dialogView.findViewById(R.id.isOnListTrue);
        final ConstraintLayout isOnListFalse = dialogView.findViewById(R.id.isOnListFalse);

        if(shoppingList.get(temp.getBarcode())==null) {
            isOnListFalse.setVisibility(View.VISIBLE);
            isOnListTrue.setVisibility(View.GONE);
            infoList.setText(R.string.This_product_is_not);
            editAmount.setText("1");

        }
        else {
            isOnListFalse.setVisibility(View.GONE);
            isOnListTrue.setVisibility(View.VISIBLE);
            infoList.setText(R.string.This_product_is_already);
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
                DecimalFormat decf = new DecimalFormat("#####0.00");
                if ((d1.compareTo(d2) > 0) && (d1.compareTo(d3) < 0)) {
                    price.setText(decf.format(Double.parseDouble(temp.getPrice().toString())-
                            Double.parseDouble(temp.getPrice().toString())*
                                    Double.parseDouble(temp.getDiscount().toString())) + "zł");
                }
                else {
                    price.setText(temp.getPrice().toString() + "zł");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            price.setText(temp.getPrice().toString() + "zł");
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_placehold)
                .error(R.drawable.ic_placehold);


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
                            Toast.makeText(getContext(),R.string.pleaseInsertProductAmount,Toast.LENGTH_SHORT).show();}
                        else{
                            databaseHandler.addToList(product,String.valueOf(editAmount.getText()),"false",temp.getBarcode(),temp.getPrice().toString());


                            isOnListTrue.setVisibility(View.VISIBLE);
                            isOnListFalse.setVisibility(View.GONE);
                            Toast.makeText(getContext(), R.string.Product_added,Toast.LENGTH_SHORT).show();

                        }
                    }
                });



                final Button btnClose = dialogView.findViewById(R.id.btnClose);
                final Button btnClose2 = dialogView.findViewById(R.id.btnClose2);

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        showCustomLoadingDialog();

                    }
                });
                btnClose2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        showCustomLoadingDialog();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //showCustomLoadingDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(getString(R.string.AreYouDeleteProd))
                                .setCancelable(false)
                                .setTitle(getString(R.string.delete_conf))
                                .setPositiveButton(getString(R.string.delete),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                databaseHandler.deleteFromShoppingList(product);
                                                refreshAdapter();
                                                tempSL[0] = shoppingList.get(product);
                                                dialog.cancel();
                                                alertDialog.dismiss();
                                                showCustomLoadingDialog();
                                                Toast.makeText(getContext(), R.string.ProdDeleteList,Toast.LENGTH_SHORT).show();
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
                            databaseHandler.updateIsBought(temp.getName(), String.valueOf(!tempSL[0].isBought()), String.valueOf(editAmount.getText()),temp.getBarcode(),temp.getPrice().toString());

                            refreshAdapter();
                            tempSL[0] = shoppingList.get(product);
                            //alertDialog.dismiss();
                            Toast.makeText(getContext(), R.string.ListUpdateSuccess,Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getContext(),R.string.pleaseInsertProductAmount,Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        alertDialog.show();
    }

    private void refreshAdapter(){
        shoppingList = databaseHandler.getShoppingList();
        products = databaseHandler.getProductsDetail();
    }

    private void showCustomLoadingDialog(final String rawResult) {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                buyLayout(rawResult);
                loadingDialog.hideDialog();
            }
        }, 500);
    }

    private void showCustomLoadingDialog() {

        loadingDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
                mScannerView.resumeCameraPreview(PriceReader.this);
            }
        }, 1000);
    }

}
