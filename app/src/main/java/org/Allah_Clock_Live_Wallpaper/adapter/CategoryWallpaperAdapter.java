package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.ImageUrlsItem;
import org.Allah_Clock_Live_Wallpaper.model.ResponseWallpaperItem;

import java.util.List;

/** Wallpaper category grid. */
public class CategoryWallpaperAdapter extends RecyclerView.Adapter<CategoryWallpaperAdapter.ViewHolder> {

    private ClickListener clickListener;
    private final List<ResponseWallpaperItem> imagesItems;

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_cat_wallpaper, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        ResponseWallpaperItem item = this.imagesItems.get(i);
        String thumbnail = firstImageUrl(item);

        Glide.with(viewHolder.viewStub.getContext())
                .load(thumbnail)
                .apply(new RequestOptions().override(600, 600).centerCrop())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(viewHolder.viewStub);

        viewHolder.textName.setText(item.getCategoryName());

        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CategoryWallpaperAdapter.this.clickListener != null) {
                    CategoryWallpaperAdapter.this.clickListener.setClick(
                            CategoryWallpaperAdapter.this.imagesItems.get(i));
                }
            }
        });
    }

    /** Guards against a category that ships without any image url. */
    private static String firstImageUrl(ResponseWallpaperItem item) {
        if (item == null) {
            return null;
        }
        List<ImageUrlsItem> urls = item.getImageUrls();
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        return urls.get(0).getImageUrl();
    }

    @Override
    public int getItemCount() {
        return this.imagesItems == null ? 0 : this.imagesItems.size();
    }
}
