package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperItem;

import java.util.List;

/** Wallpaper grid inside one category. Every image is a bundled drawable resource. */
public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    private ClickListener clickListener;
    private final List<WallpaperItem> items;

    public interface ClickListener {
        void setClick(int i);
    }

    public WallpaperAdapter(List<WallpaperItem> list) {
        this.items = list;
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
        WallpaperItem item = this.items.get(i);
        viewHolder.viewStub.setImageResource(item.getDrawableRes());

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
        return this.items == null ? 0 : this.items.size();
    }
}
