package com.example.application;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cmcy.medialib.utils.MediaSelector;
import com.cmcy.medialib.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , MediaSelector.MediaSelectorListener{

    private MediaPickAdapter imageAdapter;
    private MediaPickAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        setContentView(R.layout.activity_main);

        RecyclerView rv_image_list = findViewById(R.id.rv_image_list);
        RecyclerView rv_video_list = findViewById(R.id.rv_video_list);

        imageAdapter = new MediaPickAdapter(this, MediaSelector.PICTURE);
        rv_image_list.setLayoutManager(new GridLayoutManager(this, 4));
        rv_image_list.setAdapter(imageAdapter);

        videoAdapter = new MediaPickAdapter(this, MediaSelector.VIDEO);
        rv_video_list.setLayoutManager(new GridLayoutManager(this, 4));
        rv_video_list.setAdapter(videoAdapter);
    }

    int clickType;

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_image:
                clickType = 1;

                MediaSelector.get(this)
                        .showCamera(true)//默认显示，可以不用设置
                        .setSelectMode(MediaSelector.MODE_MULTI)//默认多选
                        .setMaxCount(20)//默认最多选择5张，设置单选后此设置无效
                        .setMediaType(MediaSelector.PICTURE)//默认选择图片
                        .setDefaultList(imageAdapter.getSelect())//默认选中的图片/视频
                        .setListener(this)//选择完成的回调, （可以设置回调或者用onActivityResult方式接收）
                        .jump();

                break;

            case R.id.tv_video:
                clickType = 2;

                MediaSelector.get(this)
                        .showCamera(true)
                        .setMaxCount(20)
                        .setMediaType(MediaSelector.VIDEO)
                        .setDefaultList(videoAdapter.getSelect())
                        .setListener(this)
                        .jump();


                break;
        }
    }

    //使用回调的方式接收
    @Override
    public void onMediaResult(List<String> resultList) {
        List<MediaPickBean> beanList = new ArrayList<>();

        if(resultList.size() > 0){
            for (String url : resultList){
                MediaPickBean bean = new MediaPickBean();
                bean.setUri(url);
                beanList.add(bean);
            }
        }

        MediaPickAdapter mediaPickAdapter = clickType == 1 ? imageAdapter : videoAdapter;

        if(mediaPickAdapter != null){
            mediaPickAdapter.addAll(beanList);
        }
    }

    //使用onActivityResult方式接收
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MediaSelector.REQUEST_IMAGE && resultCode == RESULT_OK){

            List<String> resultList = data.getStringArrayListExtra(MediaSelector.EXTRA_RESULT);

            Log.e("TAG", "size-->" + resultList.size());
        }
    }
}
