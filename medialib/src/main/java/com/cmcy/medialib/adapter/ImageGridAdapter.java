package com.cmcy.medialib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmcy.medialib.R;
import com.cmcy.medialib.bean.Image;
import com.cmcy.medialib.utils.MediaSelector;

import java.util.ArrayList;
import java.util.List;

/**
  * Description : 图片Adapter
  * ClassName : ImageGridAdapter
  * Author : Cybing
  * Date : 2020/6/2 11:47
 */
public class ImageGridAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;

    private LayoutInflater mInflater;
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;

    private List<Image> mImages = new ArrayList<Image>();
    private List<Image> mSelectedImages = new ArrayList<Image>();
    private int mediaType;
    public ImageGridAdapter(Context context, boolean showCamera){
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showCamera = showCamera;
    }

    public void setMediaType(int type)
    {
        this.mediaType = type;
    }

    /**
     * 显示选择指示器
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowCamera(boolean b){
        if(showCamera == b) return;

        showCamera = b;
        notifyDataSetChanged();
    }

    public boolean isShowCamera(){
        return showCamera;
    }

    /**
     * 选择某个图片，改变选择状态
     * @param image
     */
    public void select(Image image) {
        if(mSelectedImages.contains(image)){
            mSelectedImages.remove(image);
        }else{
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for(String path : resultList){
            Image image = getImageByPath(path);
            if(image != null){
                mSelectedImages.add(image);
            }
        }
        if(mSelectedImages.size() > 0){
            notifyDataSetChanged();
        }
    }

    private Image getImageByPath(String path){
        if(mImages != null && mImages.size()>0){
            for(Image image : mImages){
                if(image.path.equalsIgnoreCase(path)){
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     * @param images
     */
    public void setData(List<Image> images) {
        mSelectedImages.clear();

        if(images != null && images.size()>0){
            mImages = images;
        }else{
            mImages.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(showCamera){
            return position==0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return showCamera ? mImages.size()+1 : mImages.size();
    }

    @Override
    public Image getItem(int i) {
        if(showCamera){
            if(i == 0){
                return null;
            }
            return mImages.get(i-1);
        }else{
            return mImages.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        int type = getItemViewType(i);
        if(type == TYPE_CAMERA){
            view = mInflater.inflate(R.layout.item_media_camera, viewGroup, false);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(mediaType == MediaSelector.PICTURE ? R.string.shoot_picture : R.string.shoot_video);
            view.setTag(null);
        }else if(type == TYPE_NORMAL){
            ViewHolder holde;
            if(view == null){
                view = mInflater.inflate(R.layout.item_media_image, viewGroup, false);
                holde = new ViewHolder(view);
            }else{
                holde = (ViewHolder) view.getTag();
                if(holde == null){
                    view = mInflater.inflate(R.layout.item_media_image, viewGroup, false);
                    holde = new ViewHolder(view);
                }
            }

            holde.bindData(getItem(i));
        }

        return view;
    }

    class ViewHolder {
        ImageView image;
        ImageView indicator;
        View mask;

        ViewHolder(View view){
            image = (ImageView) view.findViewById(R.id.image);
            indicator = (ImageView) view.findViewById(R.id.checkmark);
            mask = view.findViewById(R.id.mask);
            view.setTag(this);
        }

        void bindData(final Image data){
            if(data == null) return;
            // 处理单选和多选状态
            if(showSelectIndicator){
                indicator.setVisibility(View.VISIBLE);
                if(mSelectedImages.contains(data)){
                    // 设置选中状态
                    indicator.setImageResource(R.drawable.ic_media_btn_selected);
                    mask.setVisibility(View.VISIBLE);
                }else{
                    // 未选择
                    indicator.setImageResource(R.drawable.ic_media_btn_unselected);
                    mask.setVisibility(View.GONE);
                }
            }else{
                indicator.setVisibility(View.GONE);
            }

            if(mediaType == MediaSelector.VIDEO)
            {
                image.setImageResource(R.color.color_placeholder_bg);
                if(data.bitmap != null)
                    image.setImageBitmap(data.bitmap);
            }
            else
            {
                Glide.with(mContext)
                        .load(data.path)
                        .error(R.color.color_placeholder_bg)
                        .placeholder(R.color.color_placeholder_bg)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .crossFade()
                        .into(image);
            }

        }
    }

}
