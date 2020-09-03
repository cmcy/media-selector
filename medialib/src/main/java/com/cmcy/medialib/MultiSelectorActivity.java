package com.cmcy.medialib;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import com.cmcy.medialib.clipimage.ClipImageActivity;
import com.cmcy.medialib.utils.MediaSelector;
import com.cmcy.medialib.utils.Utils;

import java.io.File;
import java.util.ArrayList;


/**
  * Description : 图片/视频选择
  * ClassName : MultiSelectorActivity
  * Author : Cybing
  * Date : 2020/6/2 11:45
 */
public class MultiSelectorActivity extends FragmentActivity implements View.OnClickListener, MultiSelectorFragment.Callback{

    public static final int REQUEST_CLIPL_IMAGE = 0x10;

    private int maxCount;
    private int selectMode;
    private int mediaType;
    private ArrayList<String> resultList = new ArrayList<>();
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarColor(this, 0XFF505153);
        setContentView(R.layout.activity_media_multi_selector);
        initView();
    }

    protected void initView() {
        Intent intent = getIntent();
        maxCount = intent.getIntExtra(MediaSelector.EXTRA_MAX_COUNT, 9);
        selectMode = intent.getIntExtra(MediaSelector.EXTRA_SELECT_MODE, MediaSelector.MODE_MULTI);
        mediaType = intent.getIntExtra(MediaSelector.MEDIA_TYPE, MediaSelector.PICTURE);
        boolean showCamera = intent.getBooleanExtra(MediaSelector.EXTRA_SHOW_CAMERA, true);
        if(selectMode == MediaSelector.MODE_MULTI && intent.hasExtra(MediaSelector.EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(MediaSelector.EXTRA_DEFAULT_SELECTED_LIST);
            resultList = resultList == null ? new ArrayList<>() : resultList;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(MediaSelector.EXTRA_MAX_COUNT, maxCount);
        bundle.putInt(MediaSelector.EXTRA_SELECT_MODE, selectMode);
        bundle.putInt(MediaSelector.MEDIA_TYPE, mediaType);
        bundle.putBoolean(MediaSelector.EXTRA_SHOW_CAMERA, showCamera);
        bundle.putStringArrayList(MediaSelector.EXTRA_DEFAULT_SELECTED_LIST, resultList);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this, MultiSelectorFragment.class.getName(), bundle))
                .commit();

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(this);

        // 完成按钮
        mSubmitButton = (Button) findViewById(R.id.commit);
        if(selectMode != MediaSelector.MODE_MULTI) mSubmitButton.setVisibility(View.GONE);

        if(resultList == null || resultList.size()<=0){
            mSubmitButton.setText("完成");
            mSubmitButton.setEnabled(false);
        }else{
            mSubmitButton.setText(String.format("完成(%s/%s)", resultList.size(), maxCount));
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.commit) {
            if(resultList != null && resultList.size() >0){
                onResult();
                // 返回已选择的图片数据
                Intent data = new Intent();
                data.putStringArrayListExtra(MediaSelector.EXTRA_RESULT, resultList);
                setResult(RESULT_OK, data);
                finish();
            }
        }else if(v.getId() == R.id.btn_back){

            setResult(RESULT_CANCELED);
            finish();
        }
    }


    @Override
    public void onSingleImageSelected(String path) {
        selectEnd(path);
    }

    @Override
    public void onImageSelected(String path) {
        if(!resultList.contains(path)) {
            resultList.add(path);
        }
        // 有图片之后，改变按钮状态
        if(resultList.size() > 0){
            mSubmitButton.setText(String.format("完成(%s/%s)", resultList.size(), maxCount));
            if(!mSubmitButton.isEnabled()){
                mSubmitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(String path) {
        resultList.remove(path);
        mSubmitButton.setText(String.format("完成(%s/%s)", resultList.size(), maxCount));

        // 当为选择图片时候的状态
        if(resultList.size() == 0){
            mSubmitButton.setText("完成");
            mSubmitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if(imageFile != null) {
            //通知媒体刷新图片
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageFile.getAbsolutePath())));
            selectEnd(imageFile.getAbsolutePath());
        }
    }


    /**
     * 图片/视频选择完成
     * @param path 图片路径
     */
    private void selectEnd(String path)
    {
        //图片选择才能剪裁
        if(mediaType == MediaSelector.PICTURE && selectMode == MediaSelector.MODE_CLIPL)//图片剪裁模式
        {
            Intent intent = new Intent();
            intent.putExtra(ClipImageActivity.TEMP_FILE_PATH, path);
            intent.setClass(this, ClipImageActivity.class);
            startActivityForResult(intent, REQUEST_CLIPL_IMAGE);

        }else{
            resultList.add(path);
            onResult();
            Intent data = new Intent();
            data.putStringArrayListExtra(MediaSelector.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * 通知数据返回
     */
    private void onResult(){
        if(MediaSelector.getBuilder().getListener() != null){
            MediaSelector.getBuilder().getListener().onMediaResult(resultList);
        }
    }

    //图片剪裁返回数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CLIPL_IMAGE && resultCode == RESULT_OK)
        {
            String path = data.getStringExtra(ClipImageActivity.OUTPUT_PATH);
            resultList.add(path);
            onResult();
            data.putStringArrayListExtra(MediaSelector.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaSelector.clearBuilder();
    }
}
