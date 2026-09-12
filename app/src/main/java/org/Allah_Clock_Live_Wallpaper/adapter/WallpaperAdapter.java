package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.ImageUrlsItem;

import java.util.List;

/** Wallpaper grid inside a category. */
public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    private ClickListener clickListener;
    private final List<ImageUrlsItem> imagesItems;

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_wallpaper, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Glide.with(viewHolder.viewStub.getContext())
                .load(this.imagesItems.get(i).getImageUrl())
                .apply(new RequestOptions().override(600, 600).centerCrop())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(viewHolder.viewStub);

        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WallpaperAdapter.this.clickListener != null) {
                    WallpaperAdapter.this.clickListener.setClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.imagesItems == null ? 0 : this.imagesItems.size();
    }
}
