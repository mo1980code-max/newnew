package com.clock.livewallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.ResponseWallpaperItem;

import java.util.List;





public class CategoryWallpaperAdapter extends RecyclerView.Adapter<CategoryWallpaperAdapter.ViewHolder> {
    private ClickListener clickListener;
    List<ResponseWallpaperItem> imagesItems;



    public interface ClickListener {
        void setClick(ResponseWallpaperItem responseWallpaperItem);
    }

    public CategoryWallpaperAdapter(List<ResponseWallpaperItem> list) {
        this.imagesItems = list;
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final ImageView viewStub;

        public ViewHolder(View view) {
            super(view);
            this.viewStub = (ImageView) view.findViewById(R.id.iv_clock);
            this.textName = (TextView) view.findViewById(R.id.textName);
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cat_wallpaper, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Glide.with(viewHolder.viewStub.getContext()).load(this.imagesItems.get(i).getImageUrls().get(0).getImageUrl()).centerCrop().placeholder(R.drawable.placeholder).apply((BaseRequestOptions<?>) new RequestOptions().override(600, 600).centerCrop()).into(viewHolder.viewStub);
        viewHolder.textName.setText(this.imagesItems.get(i).getCategoryName());
        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (CategoryWallpaperAdapter.this.clickListener != null) {
                    CategoryWallpaperAdapter.this.clickListener.setClick(CategoryWallpaperAdapter.this.imagesItems.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.imagesItems.size();
    }
}
