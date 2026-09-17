package org.Allah_Clock_Live_Wallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.PremiumBackgroundHelper;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperItem;
import org.Allah_Clock_Live_Wallpaper.utils.UiMotion;

import java.util.List;

/**
 * Wallpaper grid. Every image is a bundled drawable resource, and the premium ones carry the
 * rewarded gate.
 *
 * <p>The lock badge is painted while a cell is bound, from the persisted unlock state — so a
 * background the user has just earned comes back unbadged the next time the grid is bound, and a
 * cell recycled from a locked position never shows a stale padlock on an unlocked image.</p>
 */
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
        private final ImageView lock;
        private final TextView premiumBadge;

        public ViewHolder(View view) {
            super(view);
            this.viewStub = view.findViewById(R.id.iv_clock);
            this.lock = view.findViewById(R.id.wallpaperLock);
            this.premiumBadge = view.findViewById(R.id.wallpaperPremiumBadge);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_wallpaper, viewGroup, false));
        // The whole cell is the tap target (the image, the lock scrim, the premium chip), with
        // the same press micro-interaction as the header tiles.
        UiMotion.pressable(holder.itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final WallpaperItem item = this.items.get(i);
        viewHolder.viewStub.setImageResource(item.getDrawableRes());
        PremiumBackgroundHelper.bindLock(viewHolder.lock, viewHolder.premiumBadge,
                viewHolder.itemView.getContext(), item.getDrawableRes());

        viewHolder.viewStub.setOnClickListener(null);
        viewHolder.itemView.setOnClickListener(view -> {
            UiMotion.tick(view);
            if (WallpaperAdapter.this.clickListener != null) {
                WallpaperAdapter.this.clickListener.setClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }
}
