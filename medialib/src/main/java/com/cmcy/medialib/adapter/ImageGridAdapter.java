package com.cmcy.medialib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;

    private boolean showCamera = true;
    private boolean showSelectIndicator = true;

    private List<Image> mImages = new ArrayList<Image>();
    private List<Image> mSelectedImages = new ArrayList<Image>();
    private int mediaType;

    public interface ItemCallback{
        //拍照点击
        void cameraClick();
        //正常item点击
        void itemClick(int position);
    }

    private ItemCallback itemCallback;

    public ImageGridAdapter(Context context, boolean showCamera, ItemCallback itemCallback){
        mContext = context;
        this.showCamera = showCamera;
        this.itemCallback = itemCallback;
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
    public int getItemViewType(int position) {
        if(showCamera){
            return position==0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return showCamera ? mImages.size()+1 : mImages.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case TYPE_CAMERA:
                return new CameraHolder(LayoutInflater.from(mContext).inflate(R.layout.item_media_camera, parent, false));
            case TYPE_NORMAL:
                return new ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_media_image, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i)
    {
        int type = getItemViewType(i);
        if(type == TYPE_CAMERA){
            CameraHolder cameraHolder = (CameraHolder) holder;
            cameraHolder.title.setText(mediaType == MediaSelector.PICTURE ? R.string.shoot_picture : R.string.shoot_video);

        }else if(type == TYPE_NORMAL){
            ImageHolder imageHolder = (ImageHolder) holder;
            imageHolder.itemView.setTag(i);
            Image data = getItem(i);

            // 处理单选和多选状态
            if(showSelectIndicator){
                imageHolder.indicator.setVisibility(View.VISIBLE);
                if(mSelectedImages.contains(data)){
                    // 设置选中状态
                    imageHolder.indicator.setImageResource(R.drawable.ic_media_btn_selected);
                    imageHolder.mask.setVisibility(View.VISIBLE);
                }else{
                    // 未选择
                    imageHolder.indicator.setImageResource(R.drawable.ic_media_btn_unselected);
                    imageHolder.mask.setVisibility(View.GONE);
                }
            }else{
                imageHolder.indicator.setVisibility(View.GONE);
            }

            if(mediaType == MediaSelector.VIDEO)
            {
                imageHolder.image.setImageResource(R.color.color_placeholder_bg);
                if(data.bitmap != null)
                    imageHolder.image.setImageBitmap(data.bitmap);
            }
            else
            {
                RequestOptions options = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.color.color_placeholder_bg)
                        .error(R.color.color_placeholder_bg)
                        .centerCrop();

                Glide.with(mContext)
                        .load(data.path)
                        .apply(options)
                        .into(imageHolder.image);
            }
        }
    }

     class CameraHolder extends RecyclerView.ViewHolder{
        TextView title;
        CameraHolder(View view){
            super(view);
            title = view.findViewById(R.id.title);

            view.setOnClickListener(v -> {
                if(itemCallback != null){
                    itemCallback.cameraClick();
                }
            });
        }
    }

     class ImageHolder extends RecyclerView.ViewHolder{
        ImageView image;
        ImageView indicator;
        View mask;

        ImageHolder(View view){
            super(view);
            image = view.findViewById(R.id.image);
            indicator = view.findViewById(R.id.checkmark);
            mask = view.findViewById(R.id.mask);

            view.setOnClickListener(v -> {
                int position = Integer.parseInt(v.getTag().toString());
                if(itemCallback != null){
                    itemCallback.itemClick(position);
                }
            });
        }
    }

}
