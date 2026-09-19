package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperCategory;
import org.Allah_Clock_Live_Wallpaper.utils.UiMotion;

import java.util.List;

/** Wallpaper category grid. Cover image and title both come from bundled resources. */
public class CategoryWallpaperAdapter
        extends RecyclerView.Adapter<CategoryWallpaperAdapter.ViewHolder> {

    private ClickListener clickListener;
    private final List<WallpaperCategory> items;

    public interface ClickListener {
        void setClick(int position);
    }

    public CategoryWallpaperAdapter(List<WallpaperCategory> list) {
        this.items = list;
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
        ViewHolder holder = new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_cat_wallpaper, viewGroup, false));
        UiMotion.pressable(holder.itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        WallpaperCategory category = this.items.get(position);
        viewHolder.viewStub.setImageResource(category.getCoverRes());
        viewHolder.textName.setText(category.getTitleRes());

        viewHolder.itemView.setOnClickListener(view -> {
            int pos = viewHolder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            UiMotion.tick(view);
            if (CategoryWallpaperAdapter.this.clickListener != null) {
                CategoryWallpaperAdapter.this.clickListener.setClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }
}
