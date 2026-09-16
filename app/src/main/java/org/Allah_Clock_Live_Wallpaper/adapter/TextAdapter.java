package org.Allah_Clock_Live_Wallpaper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.PremiumBadge;
import org.Allah_Clock_Live_Wallpaper.model.TextClocks;
import org.Allah_Clock_Live_Wallpaper.viewUtils.SquareRelativeLayout;

import java.util.ArrayList;

/** Digital (text) clock grid. The last {@code AdConfig.PREMIUM_ITEMS_PER_LIST} entries are golden. */
public class TextAdapter extends RecyclerView.Adapter<TextAdapter.ViewHolder> {

    private ClickListener clickListener;
    private final ArrayList<TextClocks> textClocks;

    public interface ClickListener {
        void setClick(int i, TextClocks textClocks);
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SquareRelativeLayout layout;
        private final ImageView viewStub;
        final View premiumOverlay;
        final ImageView premiumLock;

        public ViewHolder(View view) {
            super(view);
            this.viewStub = (ImageView) view.findViewById(R.id.clockwise);
            this.layout = (SquareRelativeLayout) view.findViewById(R.id.layoutBackground);
            this.premiumOverlay = view.findViewById(R.id.premiumOverlay);
            this.premiumLock = view.findViewById(R.id.premiumLock);
        }
    }

    public TextAdapter(ArrayList<TextClocks> arrayList) {
        this.textClocks = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_textclock, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.layout.setCardBackgroundColor(Color.parseColor(this.textClocks.get(i).getBgColor()));
        viewHolder.viewStub.setImageResource(this.textClocks.get(i).getThumb());

        PremiumBadge.bind(viewHolder.premiumOverlay, viewHolder.premiumLock, i, getItemCount());

        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PremiumBadge.onItemClick(view, i, getItemCount(), new Runnable() {
                    @Override
                    public void run() {
                        TextAdapter.this.fireClick(i);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void fireClick(int position) {
        if (this.clickListener != null && position >= 0 && position < this.textClocks.size()) {
            this.clickListener.setClick(position, this.textClocks.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return this.textClocks.size();
    }
}
