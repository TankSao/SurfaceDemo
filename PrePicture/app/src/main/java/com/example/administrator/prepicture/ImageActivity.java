package com.example.administrator.prepicture;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/6/22.
 */

public class ImageActivity extends Activity {
    @BindView(R.id.iv_large)
    ImageView largeImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        String url = getIntent().getStringExtra("imgurl");
        Glide.with(this).load(url).fitCenter().into(largeImage);
        largeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
