package com.example.application;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmcy.medialib.PhotoPreviewActivity;
import com.cmcy.medialib.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
  * Description : 图片选择
  * ClassName : MediaPickAdapter
  * Author : Cybing
  * Date : 2020/5/21 11:13
 */
public class MediaPickAdapter extends RecyclerView.Adapter<MediaPickAdapter.ViewHolder>
{
    private Context mContext;

    private List<MediaPickBean> mDataList = new ArrayList<>();

    private int meadeType = 1; //1图片，2视频

    private Disposable observeOnDataChange;


    public MediaPickAdapter(Context context, int meadeType)
    {
        mContext = context;
        this.meadeType = meadeType;
    }


    public void addAll(List<MediaPickBean> datas)
    {
        mDataList.clear();
        if (datas != null)
            mDataList.addAll(datas);

        notifyBitmapData();
        notifyDataSetChanged();
    }


    public ArrayList<String> getSelect(){
        ArrayList<String> imageList = new ArrayList<>();

        for (int i = 0; i < mDataList.size(); i++){
            MediaPickBean imgPicBean = mDataList.get(i);
            imageList.add(imgPicBean.getUri());
        }

        return imageList;
    }

    public void notifyBitmapData()
    {

        if (mDataList.size() == 0 || meadeType == 1) return;

        Utils.dispose(observeOnDataChange);

        Observable<MediaPickBean> listObservable = Observable.fromIterable(mDataList);
        observeOnDataChange = listObservable.subscribeOn(Schedulers.io())
                .filter(imgPicBean -> (imgPicBean.getBitmap() == null))
                .map(imgPicBean -> {
                    Bitmap bitmap = Utils.getVideoUrlBitmap(TextUtils.isEmpty(imgPicBean.getUri()) ? imgPicBean.getUrl() : imgPicBean.getUri());
                    imgPicBean.setBitmap(bitmap);
                    return bitmap != null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(isFinished -> {
                    if (isFinished) {
                        notifyDataSetChanged();
                    }
                })
                .subscribe();

    }

    @Override
    public int getItemCount()
    {
        return mDataList.size() ;
    }


    @Override
    public MediaPickAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
         return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_media_img_pic, parent, false));
    }

    @Override
    public void onBindViewHolder(MediaPickAdapter.ViewHolder holder, final int position)
    {
        MediaPickBean imgPicBean = mDataList.get(position);

        if(meadeType == 2) {
            holder.play.setVisibility(View.VISIBLE);
            if (imgPicBean.getBitmap() != null) {
                holder.image.setImageBitmap(imgPicBean.getBitmap());
            }else {
                holder.image.setImageResource(com.cmcy.medialib.R.color.color_placeholder_bg);
            }

        }
        else {
            holder.play.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(imgPicBean.getUri())
                    .error(com.cmcy.medialib.R.color.color_placeholder_bg)
                    .placeholder(com.cmcy.medialib.R.color.color_placeholder_bg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .crossFade()
                    .into(holder.image);
        }

        holder.image.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, PhotoPreviewActivity.class);
            intent.putExtra("url", TextUtils.isEmpty(imgPicBean.getUri()) ? imgPicBean.getUrl() : imgPicBean.getUri());
            intent.putExtra("type", meadeType);
            mContext.startActivity(intent);

        });
    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        ImageView play;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            play = view.findViewById(R.id.play);
        }
    }





}
