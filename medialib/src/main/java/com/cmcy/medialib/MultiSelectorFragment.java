package com.cmcy.medialib;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;


import com.cmcy.medialib.adapter.FolderAdapter;
import com.cmcy.medialib.adapter.ImageGridAdapter;
import com.cmcy.medialib.bean.Folder;
import com.cmcy.medialib.bean.Image;
import com.cmcy.medialib.presenter.MediaContract;
import com.cmcy.medialib.presenter.MediaPresenter;
import com.cmcy.medialib.utils.CameraJump;
import com.cmcy.medialib.utils.MediaSelector;
import com.cmcy.medialib.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
  * Description : 图片/视频选择Fragment
  * ClassName : MultiSelectorFragment
  * Author : Cybing
  * Date : 2020/6/2 11:46
 */
public class MultiSelectorFragment extends Fragment implements MediaContract.MediaView
{
    protected final String TAG = this.getClass().getName();

    private static final int LOADER_ALL = 0;

    // 结果数据
    private ArrayList<String> resultList = new ArrayList<String>();

    // 图片Grid
    private GridView mGridView;
    private Callback mCallback;

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    private ListPopupWindow mFolderPopupWindow;

    // 时间线
    private TextView mTimeLineText;
    // 类别
    private TextView mCategoryText;
    // 预览按钮
    private Button mPreviewBtn;
    // 底部View
    private View mPopupAnchorView;

    private int mDesireImageCount;

    private int mediaType = 1;// 1图片， 2视频
    private int selectMode;

    private boolean mIsShowCamera = false;

    private MediaPresenter mediaPresenter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        }catch (ClassCastException e){
            throw new ClassCastException("The Activity must implement MultiSelectorFragment.Callback interface...");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首次加载所有图片
        mediaPresenter.loadMediaList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_multi_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mediaPresenter = new MediaPresenter(getActivity(), this);

        // 选择图片数量
        mDesireImageCount = getArguments().getInt(MediaSelector.EXTRA_MAX_COUNT);

        mediaType = getArguments().getInt(MediaSelector.MEDIA_TYPE);

        // 图片选择模式
        selectMode = getArguments().getInt(MediaSelector.EXTRA_SELECT_MODE);

        // 默认选择
        if(selectMode == MediaSelector.MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(MediaSelector.EXTRA_DEFAULT_SELECTED_LIST);
            if(tmp != null && tmp.size()>0) {
                resultList = tmp;
            }
        }

        // 是否显示照相机
        mIsShowCamera = getArguments().getBoolean(MediaSelector.EXTRA_SHOW_CAMERA, true);
        mImageAdapter = new ImageGridAdapter(getActivity(), mIsShowCamera);
        mImageAdapter.setMediaType(mediaType);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(selectMode == MediaSelector.MODE_MULTI);

        mPopupAnchorView = view.findViewById(R.id.footer);

        mTimeLineText = (TextView) view.findViewById(R.id.timeline_area);
        // 初始化，先隐藏当前timeline
        mTimeLineText.setVisibility(View.GONE);

        mCategoryText = (TextView) view.findViewById(R.id.category_btn);
        // 初始化，加载所有图片/视频
        mCategoryText.setText(mediaType == MediaSelector.VIDEO ? R.string.folder_all_video : R.string.folder_all_picture);
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mFolderPopupWindow == null){
                    createPopupFolderList();
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
                    int index = mFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        mPreviewBtn = (Button) view.findViewById(R.id.preview);
        // 初始化，按钮状态初始化
        if(resultList == null || resultList.size()<=0){
            mPreviewBtn.setText(R.string.media_preview);
            mPreviewBtn.setEnabled(false);
        }
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 预览
            }
        });

        mGridView = (GridView) view.findViewById(R.id.grid);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
                if(state == SCROLL_STATE_IDLE){
                    // 停止滑动，日期指示器消失
                    mTimeLineText.setVisibility(View.GONE);
                }else if(state == SCROLL_STATE_FLING){
                    mTimeLineText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(mTimeLineText.getVisibility() == View.VISIBLE) {
                    int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
                    Image image = (Image) view.getAdapter().getItem(index);
                    if (image != null) {
                        mTimeLineText.setText(Utils.formatPhotoDate(image.path));
                    }
                }
            }
        });
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mImageAdapter.isShowCamera()){
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if(i == 0){
                        CameraJump.showCameraAction(MultiSelectorFragment.this, mediaType);
                    }else{
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, selectMode);
                    }
                }else{
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, selectMode);
                }
            }
        });

        mFolderAdapter = new FolderAdapter(getActivity());
        mFolderAdapter.setMediaType(mediaType);
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new ListPopupWindow(getActivity());
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(mGridView.getWidth());
        mFolderPopupWindow.setWidth(mGridView.getWidth());
        mFolderPopupWindow.setHeight(mGridView.getHeight() * 5 / 8);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            mediaPresenter.loadMediaList();

                            mCategoryText.setText(mediaType == MediaSelector.VIDEO ? R.string.folder_all_video : R.string.folder_all_picture);
                            if (mIsShowCamera) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.images);
                                mCategoryText.setText(folder.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if(requestCode == CameraJump.REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (CameraJump.mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(CameraJump.mTmpFile);
                    }
                }
            }else{
                if(CameraJump.mTmpFile != null && CameraJump.mTmpFile.exists()){
                    CameraJump.mTmpFile.delete();
                    CameraJump.mTmpFile = null;
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "on change");

        if(mFolderPopupWindow != null){
            if(mFolderPopupWindow.isShowing()){
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    /**
     * 选择图片操作
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if(image != null) {
            // 多选模式
            if(mode == MediaSelector.MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if(resultList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText(getResources().getString(R.string.media_preview) + "(" + resultList.size() + ")");
                    }else{
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText(R.string.media_preview);
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    // 判断选择数量问题
                    if(mDesireImageCount == resultList.size()){
                        Toast.makeText(getActivity(), R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.path);
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText(getResources().getString(R.string.media_preview) + "(" + resultList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);
            }else{
                // 单选模式
                if(mCallback != null){
                    mCallback.onSingleImageSelected(image.path);
                }
            }
        }
    }


    @Override
    public void showMediaList(List<Image> imageList) {
        mImageAdapter.setData(imageList);

        // 设定默认选择
        if(resultList != null && resultList.size()>0){
            mImageAdapter.setDefaultSelected(resultList);
        }

        mediaPresenter.loadVideoThumb(imageList);
    }

    @Override
    public void showFolderList(List<Folder> folderList) {
        mFolderAdapter.setData(folderList);
    }

    @Override
    public void notifyVideoThumb() {
        mImageAdapter.notifyDataSetChanged();
        mFolderAdapter.notifyDataSetChanged();
    }

    @Override
    public int getMediaType() {
        return mediaType;
    }

    @Override
    public int getLoaderModel() {
        return LOADER_ALL;
    }


    /**
     * 回调接口
     */
    public interface Callback{
        void onSingleImageSelected(String path);
        void onImageSelected(String path);
        void onImageUnselected(String path);
        void onCameraShot(File imageFile);
    }
}
