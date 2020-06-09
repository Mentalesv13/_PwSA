package com.example.scanandgo_pwsa.adapters;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.scanandgo_pwsa.R;
import com.example.scanandgo_pwsa.helper.SessionManager;
import com.example.scanandgo_pwsa.model.ExampleItem;

import java.util.ArrayList;


public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ExampleViewHolder> {
    private final Context context;
    private ArrayList<ExampleItem> mExampleList;
    private OnItemClickListener mListener;
    private int selectedPosition;
    SessionManager sessionManager;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mDistance;


        ExampleViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image);
            mTextView1 = itemView.findViewById(R.id.name);
            mTextView2 = itemView.findViewById(R.id.address);
            mDistance = itemView.findViewById(R.id.distance);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }



    public ShopAdapter(ArrayList<ExampleItem> exampleList, Context context) {
        mExampleList = exampleList;
        this.context= context;
        selectedPosition = -1;
        sessionManager = new SessionManager(context);
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v, mListener);
        selectedPosition = -1;
        return evh;
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, final int position) {
        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(Color.parseColor("#dedede"));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        ExampleItem currentItem = mExampleList.get(position);
        holder.mImageView.setImageResource(currentItem.getImageResource());
        holder.mTextView1.setText(currentItem.getText1());
        holder.mTextView2.setText(currentItem.getText2());
        holder.mDistance.setText(currentItem.getmDistance());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                sessionManager.setShop(position);
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }
}