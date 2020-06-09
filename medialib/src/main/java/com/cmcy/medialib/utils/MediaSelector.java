package com.cmcy.medialib.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.cmcy.medialib.MultiSelectorActivity;
import com.cmcy.medialib.utils.permission.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
  * Description : 跳转到图片/视频选择
  * ClassName : MediaSelector
  * Author : Cybing
  * Date : 2020/6/2 11:46
 */
public class MediaSelector
{
    public static final int REQUEST_IMAGE = 0x10;

    /** 最大图片选择次数，int类型，默认9 */
    public static final String EXTRA_MAX_COUNT = "MAX_SELECT_COUNT";
    /** 图片选择模式，默认多选 */
    public static final String EXTRA_SELECT_MODE = "SELECT_MODE";
    /** 是否显示相机，默认显示 */
    public static final String EXTRA_SHOW_CAMERA = "SHOW_CAMERA";
    /** 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合  */
    public static final String EXTRA_RESULT = "SELECT_RESULT";
    /** 媒体类型 1图片，2视频，int类型 */
    public static final String MEDIA_TYPE = "MEDIA_TYPE";
    /** 默认选择集 */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "DEFAULT_LIST";

    /** 单选 */
    public static final int MODE_SINGLE = 0;
    /** 多选 */
    public static final int MODE_MULTI = 1;
    /** 剪切 */
    public static final int MODE_CLIPL = 2;

    /** 媒体类型 1图片 */
    public static final int PICTURE = 1;
    /** 媒体类型 2视频 */
    public static final int VIDEO = 2;

    private static ImageBuilder imageBuilder;

    /**
     * 图片/视频选择完成监听
     *
     */
    public interface MediaSelectorListener{
        void onMediaResult(List<String> resultList);
    }

    public static ImageBuilder get() {
        return getBuilder();
    }

    public static ImageBuilder getBuilder() {
        if(imageBuilder == null){
            imageBuilder = new ImageBuilder();
        }
        return imageBuilder;
    }

    /**
     * 清除构造器
     */
    public static void clearBuilder() {
        if(imageBuilder != null)imageBuilder.dispose();
        imageBuilder = null;
    }

    public static class ImageBuilder{
        private Activity activity;
        private Fragment fragment;
        private boolean showCamera = true;//默认显示相机
        private int selectMode = MediaSelector.MODE_MULTI;//多选
        private int maxCount = 5;
        private int mediaType = MediaSelector.PICTURE;//图片
        private ArrayList<String> defaultList;
        private MediaSelectorListener listener;
        private Disposable disposable;

        public Activity getActivity() {
            return fragment != null ? fragment.getActivity() : activity;
        }

        public ImageBuilder setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public ImageBuilder setFragment(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public boolean isShowCamera() {
            return showCamera;
        }

        /**
         * 是否显示相机拍照 默认不显示
         * @param showCamera true 显示
         * @return this
         */
        public ImageBuilder showCamera(boolean showCamera) {
            this.showCamera = showCamera;
            return this;
        }

        public int getSelectMode() {
            return selectMode;
        }

        /**
         * 设置图片选择模式，默认多选
         * @param selectMode 1单选，2多选，3剪切
         * @return this
         */
        public ImageBuilder setSelectMode(int selectMode) {
            this.selectMode = selectMode;
            return this;
        }

        public int getMaxCount() {
            return maxCount;
        }

        /**
         * 设置最大选择数量
         * @param maxCount 最大数量
         * @return this
         */
        public ImageBuilder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public int getMediaType() {
            return mediaType;
        }

        /**
         * 选择图片还是视频 1图片， 2视频
         * @param mediaType {MediaSelector.PICTURE, MediaSelector.VIDEO}
         * @return this
         */
        public ImageBuilder setMediaType(int mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public ArrayList<String> getDefaultList() {
            return defaultList;
        }

        /**
         * 默认选中项
         * @param defaultList 选中项list
         * @return this
         */
        public ImageBuilder setDefaultList(ArrayList<String> defaultList) {
            this.defaultList = defaultList;
            return this;
        }

        public MediaSelectorListener getListener() {
            return listener;
        }

        /**
         * 设置选择监听
         * @param listener 回调
         * @return
         */
        public ImageBuilder setListener(MediaSelectorListener listener) {
            this.listener = listener;
            return this;
        }


        /**
         * 跳转到相册
         * @param activity activity
         */
        public void jump(Activity activity){
            setActivity(activity);
            requestPermissions();
        }

        /**
         * 跳转到相册
         * @param fragment fragment
         */
        public void jump(Fragment fragment){
            setFragment(fragment);
            requestPermissions();
        }


        public void dispose(){
            Utils.dispose(disposable);
        }


        /**
         * 检查权限
         */
        private void requestPermissions(){
            Utils.dispose(disposable);
            if(getFragment() == null){
                disposable = RxPermissions.request(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                startImage();
                            } else {
                                Toast.makeText(activity, "权限拒绝", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else {
                disposable = RxPermissions.request(getFragment(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                startImage();
                            } else {
                                Toast.makeText(activity, "权限拒绝", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        }

        /**
         * 跳转到图片选择
         */
        private void startImage(){
            if (RxPermissions.lacksPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Intent intent = new Intent(getActivity(), MultiSelectorActivity.class);
                intent.putExtra(MediaSelector.EXTRA_SHOW_CAMERA, isShowCamera());//是否显示相机true为显示
                intent.putExtra(MediaSelector.EXTRA_SELECT_MODE, getSelectMode());//0为单选1多选
                intent.putExtra(MediaSelector.EXTRA_MAX_COUNT, getMaxCount());//可选择最多数量5
                intent.putExtra(MediaSelector.MEDIA_TYPE, getMediaType());//1图片，2视频
                intent.putExtra(MediaSelector.EXTRA_DEFAULT_SELECTED_LIST, getDefaultList());//默认选择的图片
                getActivity().startActivityForResult(intent, REQUEST_IMAGE);
            }
        }
    }

}
