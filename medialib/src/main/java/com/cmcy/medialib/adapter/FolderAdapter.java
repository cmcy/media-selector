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
import com.cmcy.medialib.bean.Folder;
import com.cmcy.medialib.utils.MediaSelector;

import java.util.ArrayList;
import java.util.List;

/**
  * Description : 文件夹Adapter
  * ClassName : FolderAdapter
  * Author : Cybing
  * Date : 2020/6/2 11:47
 */
public class FolderAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Folder> mFolders = new ArrayList<Folder>();
    private int mediaType;

    int lastSelected = 0;
    public FolderAdapter(Context context){
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 资源类型
     * @param type 1图片， 2视频
     */
    public void setMediaType(int type) {
        this.mediaType = type;
    }

    /**
     * 设置数据集
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if(folders != null && folders.size()>0){
            mFolders = folders;
        }else{
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size()+1;
    }

    @Override
    public Folder getItem(int i) {
        if(i == 0) return null;
        return mFolders.get(i-1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            view = mInflater.inflate(R.layout.item_media_folder, viewGroup, false);
            holder = new ViewHolder(view);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        String unit = mediaType == MediaSelector.VIDEO ? "个":"张";

        if(i == 0){
            if(mFolders.size() > 0){
                holder.name.setText((mediaType == MediaSelector.VIDEO ? R.string.folder_all_video : R.string.folder_all_picture));
                holder.size.setText(String.format("%s%s", getTotalImageSize(), unit));
                holder.bindData(mFolders.get(0));
            }
        }else {
            Folder f = getItem(i);
            holder.name.setText(f.name);
            holder.size.setText(String.format("%s%s", f.images.size(), unit));
            holder.bindData(getItem(i));
        }
        if(lastSelected == i){
            holder.indicator.setVisibility(View.VISIBLE);
        }else{
            holder.indicator.setVisibility(View.INVISIBLE);
        }


        return view;
    }

    private int getTotalImageSize(){
        int result = 0;
        if(mFolders != null && mFolders.size()>0){
            for (Folder f: mFolders){
                result += f.images.size();
            }
        }
        return result;
    }

    public void setSelectIndex(int i) {
        if(lastSelected == i) return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex(){
        return lastSelected;
    }

    class ViewHolder{
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;
        
        
        ViewHolder(View view){
            cover = (ImageView)view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(Folder data) {
            // 显示图片
            if(mFolders.size() > 0) {

                if(mediaType == 2)
                {
                    cover.setImageResource(R.color.color_folder_bg);
                    if(data.cover.bitmap != null)
                        cover.setImageBitmap(data.cover.bitmap);
                }
                else
                {
                    Glide.with(mContext)
                            .load(data.cover.path)
                            .error(R.color.color_folder_bg)
                            .placeholder(R.color.color_folder_bg)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .crossFade()
                            .into(cover);
                }

            }
        }
    }
}
