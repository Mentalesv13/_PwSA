package com.example.scanandgo_pwsa.helper;

import android.app.Dialog;
import android.view.Window;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.example.scanandgo_pwsa.R;

public class LoadingDialog {

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    private FragmentActivity activity;
    private Dialog dialog;

    public LoadingDialog(FragmentActivity activity) {
        this.activity = activity;
    }

    public void showDialog() {

        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCancelable(false);

        dialog.setContentView(R.layout.loading);

        ImageView gifImageView = dialog.findViewById(R.id.custom_loading_imageView);

        Glide.with(activity)
                .load(R.drawable.loading)
                .placeholder(R.drawable.loading)
                //.centerCrop()
                .into(new DrawableImageViewTarget(gifImageView));

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
