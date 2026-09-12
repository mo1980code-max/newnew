package com.clock.livewallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.ImageUrlsItem;

import java.util.List;





public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {
    private ClickListener clickListener;
    List<ImageUrlsItem> imagesItems;



    public interface ClickListener {
        void setClick(int i);
    }

    public WallpaperAdapter(List<ImageUrlsItem> list) {
        this.imagesItems = list;
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView viewStub;

        public ViewHolder(View view) {
            super(view);
            this.viewStub = (ImageView) view.findViewById(R.id.iv_clock);
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_wallpaper, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Glide.with(viewHolder.viewStub.getContext()).load(this.imagesItems.get(i).getImageUrl()).centerCrop().placeholder(R.drawable.placeholder).apply((BaseRequestOptions<?>) new RequestOptions().override(600, 600).centerCrop()).into(viewHolder.viewStub);
        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (WallpaperAdapter.this.clickListener != null) {
                    WallpaperAdapter.this.clickListener.setClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.imagesItems.size();
    }
}
