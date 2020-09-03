package com.cmcy.medialib.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cmcy.medialib.PhotoPreviewActivity;
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
     * @param position
     */
    public void select(int position) {
        if(mSelectedImages.contains(getItem(position))){
            mSelectedImages.remove(getItem(position));
        }else{
            mSelectedImages.add(getItem(position));
        }
        notifyItemChanged(position);
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
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return showCamera ? mImages.size()+1 : mImages.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(viewType == TYPE_CAMERA ){
            return new CameraHolder(LayoutInflater.from(mContext).inflate(R.layout.item_media_camera, parent, false));
        }else {
            return new ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_media_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i)
    {
        int type = getItemViewType(i);
        if(type == TYPE_CAMERA){
            CameraHolder cameraHolder = (CameraHolder) holder;
            cameraHolder.title.setText(mediaType == MediaSelector.PICTURE ? R.string.shoot_picture : R.string.shoot_video);

        }else if(type == TYPE_NORMAL){
            ImageHolder imageHolder = (ImageHolder) holder;
            imageHolder.setPosition(i);
            Image data = getItem(i);

            // 处理单选和多选状态
            if(showSelectIndicator){
                imageHolder.checkBox.setVisibility(View.VISIBLE);
                if(mSelectedImages.contains(data)){
                    // 设置选中状态
                    imageHolder.checkBox.setChecked(true);
                    imageHolder.mask.setVisibility(View.VISIBLE);
                }else{
                    // 未选择
                    imageHolder.checkBox.setChecked(false);
                    imageHolder.mask.setVisibility(View.GONE);
                }
            }else{
                imageHolder.checkBox.setVisibility(View.GONE);
            }

            if(mediaType == MediaSelector.VIDEO) {
                imageHolder.image.setImageResource(R.color.color_placeholder_bg);
                if(data.bitmap != null)
                    imageHolder.image.setImageBitmap(data.bitmap);
            }
            else {
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
        CheckBox checkBox;
        View mask;
        int position;

        public void setPosition(int position) {
            this.position = position;
        }

        ImageHolder(View view){
            super(view);
            image = view.findViewById(R.id.image);
            checkBox = view.findViewById(R.id.checkbox);
            mask = view.findViewById(R.id.mask);

            image.setOnClickListener(v -> {
                Image data = getItem(position);
                Intent intent = new Intent(mContext, PhotoPreviewActivity.class);
                intent.putExtra("url", data.path);
                intent.putExtra("type", mediaType);
                mContext.startActivity(intent);
            });

            checkBox.setOnClickListener(v -> {
                if(itemCallback != null){
                    itemCallback.itemClick(position);
                }
            });
        }
    }

}
