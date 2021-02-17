package com.example.scanandgo_pwsa.payments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.fragments.PurchaseHistory;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class PayPalConfirm extends Fragment {

    TextView orderDate, paymentID, paymentAmount, status;

    String date, pID, payAmount, stat;

    public PayPalConfirm(String payDetails, String paymentAmount) {

        try {
            JSONObject jsonDetails = new JSONObject(payDetails);

            JSONObject json = jsonDetails.getJSONObject("response");

            pID = json.getString("id");
            stat = json.getString("state");
            date = json.getString("create_time");

            date = date.replace('T',' ');
            date = date.replace('Z',' ');


        } catch (JSONException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        payAmount = paymentAmount;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_pay_pal_confirm, container, false);

        Button btnContinue = view.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragmentNoAnim(new PurchaseHistory());
            }
        });


        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsed = null; // => Date is in UTC now
        try {
            parsed = sourceFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TimeZone tz = TimeZone.getTimeZone("Europe/Warsaw");
        SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        destFormat.setTimeZone(tz);

        String result = destFormat.format(parsed);

        orderDate = view.findViewById(R.id.orderDate);
        paymentID = view.findViewById(R.id.paymentID);
        paymentAmount = view.findViewById(R.id.paymentAmount);
        status = view.findViewById(R.id.paymentStatus);

        orderDate.setText(result);
        paymentID.setText(pID);
        paymentAmount.setText(payAmount + " USD");
        status.setText(stat);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DatabaseHandler databaseHandler = new DatabaseHandler(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {

        jsonObject.put("createTime",result);
        jsonObject.put("currency","USD");
        jsonObject.put("paypalID",pID);
        jsonObject.put("state",stat);
        jsonObject.put("total",payAmount);
        jsonObject.put("uID",databaseHandler.getUserDetails().get("uid"));
        jsonObject.put("updateTime",result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Map<String, Object> jsonMap = new Gson().fromJson(jsonObject.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());

        db.collection("payments").document().set(jsonMap);

        if(databaseHandler.getShoppingList().size()>0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setMessage(getString(R.string.wanttoclear))
                    .setCancelable(false)
                    .setPositiveButton(R.string.clear,
                            new DialogInterface.OnClickListener() {
                                @SuppressLint("SetTextI18n")
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    databaseHandler.resetList();
                                    Toast.makeText(getContext(), R.string.Your_list_has_been_cleared,
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return view;
    }

}
