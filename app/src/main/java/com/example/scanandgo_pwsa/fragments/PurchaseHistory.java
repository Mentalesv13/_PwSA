package com.example.scanandgo_pwsa.fragments;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.scanandgo_pwsa.MainActivity;
import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.adapters.ChildItemsInfo;
import com.example.scanandgo_pwsa.adapters.GroupItemsInfo;
import com.example.scanandgo_pwsa.helper.DatabaseHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PurchaseHistory extends Fragment {

    private LinkedHashMap<String, GroupItemsInfo> songsList = new LinkedHashMap<String, GroupItemsInfo>();
    private ArrayList<GroupItemsInfo> deptList = new ArrayList<GroupItemsInfo>();

    private CustomExpandableAdapter myExpandableListAdapter;
    private ExpandableListView simpleExpandableListView;
    private FirebaseFirestore db;
    private DatabaseHandler databaseHandler;
    private ConstraintLayout clEmpty;
    private FloatingActionButton btnBack;
    private FirebaseAuth mAuth;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_purchase_history, container, false);
        clEmpty = view.findViewById(R.id.emptyTransaction);
        clEmpty.setVisibility(View.GONE);
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setFragment(new Profile());
            }
        });

        db = FirebaseFirestore.getInstance();
        databaseHandler = new DatabaseHandler(getContext());
        mAuth = FirebaseAuth.getInstance();
        loadData();

return view;
    }

    private int addProduct(String shopName, String productName,  String price, String date, String total, String shopAddress, String amount) {

        int groupPosition = 0;

        //check the hashmap if the group already exists
        GroupItemsInfo headerInfo = songsList.get(shopName + " - " + date);
        //add the group if doesn't exists
        if (headerInfo == null) {
            headerInfo = new GroupItemsInfo();
            headerInfo.setName(shopName + " - " + date);
            headerInfo.setTotalPrice(total);
            headerInfo.setShopAddress(shopAddress);
            songsList.put(shopName + " - " + date, headerInfo);
            deptList.add(headerInfo);
        }

        // get the children for the group
        ArrayList<ChildItemsInfo> productList = headerInfo.getSongName();
        // size of the children list

        int listSize = productList.size();
        // add to the counter
        listSize++;

        // create a new child and add that to the group
        ChildItemsInfo detailInfo = new ChildItemsInfo();
        detailInfo.setName(productName);
        detailInfo.setPrice(price);
        detailInfo.setAmount(amount);
        productList.add(detailInfo);
        headerInfo.setPlayerName(productList);

        // find the group position inside the list
        groupPosition = deptList.indexOf(headerInfo);
        return groupPosition;
    }

    class CustomExpandableAdapter implements ExpandableListAdapter {
        private Context context;
        private ArrayList<GroupItemsInfo> teamName;
        int previousItem = -1;

        CustomExpandableAdapter(Context context, ArrayList<GroupItemsInfo> deptList) {
            this.context = context;
            this.teamName = deptList;
        }
        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getGroupCount() {
            return teamName.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            ArrayList<ChildItemsInfo> productList = teamName.get(groupPosition).getSongName();
            return productList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return teamName.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            ArrayList<ChildItemsInfo> productList = teamName.get(groupPosition).getSongName();
            return productList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupItemsInfo headerInfo = (GroupItemsInfo) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inf.inflate(R.layout.group_items, null);
            }

            TextView heading = convertView.findViewById(R.id.heading);
            TextView totalPrice = convertView.findViewById(R.id.totalPrice);
            TextView shopAddress = convertView.findViewById(R.id.address);
            heading.setText(headerInfo.getName().trim());
            totalPrice.setText(headerInfo.getTotalPrice().trim() + "zł");
            shopAddress.setText(headerInfo.getShopAddress().trim());
            return convertView;

        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildItemsInfo detailInfo = (ChildItemsInfo) getChild(groupPosition, childPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.child_items, null);
            }
            TextView childItem = (TextView) convertView.findViewById(R.id.childItem);
            TextView childItem2 = (TextView) convertView.findViewById(R.id.childItem2);
            childItem.setText(detailInfo.getName().trim());
            childItem2.setText(detailInfo.getPrice() + " zł / " + detailInfo.getAmount() + " pcs.");

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            if(groupPosition != previousItem )
                simpleExpandableListView.collapseGroup(previousItem);
            previousItem = groupPosition;

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }
    }

    private void loadData() {

        DocumentReference docRef = db.collection("bills").document(mAuth.getCurrentUser().getUid().trim());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            final HashMap<Integer,String> temporary = new HashMap<>();
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                        DecimalFormat decf = new DecimalFormat("#0", symbols);
                        decf.setRoundingMode(RoundingMode.HALF_UP);
                        //Log.e("TAG", document.getData().toString());
                        List<Map> categories = (List<Map>) document.get("bill");
                        if (categories.size() > 0) {
                            clEmpty.setVisibility(View.GONE);
                            for (int i = categories.size() - 1; i >= 0; i--) {
                                Map<String, Object> bill = categories.get(i);
                                //Log.e("TAG", bill.get("date").toString());
                                //Log.e("TAG", bill.get("shopName").toString());
                                //Log.e("TAG", bill.get("shopAddress").toString());
                                //Log.e("TAG", bill.get("total").toString());
                                List<Map> products = (List<Map>) bill.get("products");
                                for (int j = 0; j < products.size(); j++) {
                                    Map<String, Object> product = products.get(j);
                                    //Log.e("TAG", product.get("total").toString());
                                    //Log.e("TAG", product.get("name").toString());
                                    //Log.e("TAG", product.get("amount").toString());
                                    addProduct(bill.get("shopName").toString().trim(),
                                            product.get("name").toString(),
                                            product.get("total").toString(),
                                            bill.get("date").toString().trim(),
                                            bill.get("total").toString(),
                                            bill.get("shopAddress").toString(),
                                            decf.format(product.get("amount")));
                                }
                            }
                        }
                        else
                        {
                            clEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                }

                simpleExpandableListView = (ExpandableListView) view.findViewById(R.id.listView);
                // create the adapter by passing your ArrayList data
                myExpandableListAdapter = new CustomExpandableAdapter(getContext(), deptList);
                // attach the adapter to the expandable list view
                simpleExpandableListView.setAdapter(myExpandableListAdapter);

                // setOnChildClickListener listener for child row click or song name click
                simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        GroupItemsInfo headerInfo = deptList.get(groupPosition);
                        return false;
                    }
                });
                // setOnGroupClickListener listener for group Song List click
                simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                        return false;
                    }
                });
            }
        });
    }

}
