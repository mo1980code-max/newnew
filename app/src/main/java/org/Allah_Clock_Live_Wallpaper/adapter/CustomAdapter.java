package org.Allah_Clock_Live_Wallpaper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.PremiumBadge;
import org.Allah_Clock_Live_Wallpaper.model.Clocks;
import org.Allah_Clock_Live_Wallpaper.viewUtils.AnalogClock;
import org.Allah_Clock_Live_Wallpaper.viewUtils.SquareRelativeLayout;

import java.util.ArrayList;

/** Analog clock grid. The last {@code AdConfig.PREMIUM_ITEMS_PER_LIST} entries are golden. */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    static int height;
    static int width;

    private ClickListener clickListener;
    private final ArrayList<Clocks> localDataSet;

    public interface ClickListener {
        void setClick(Clocks clocks);
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SquareRelativeLayout layout;
        private final AnalogClock textView;
        final View premiumOverlay;
        final ImageView premiumLock;

        public ViewHolder(View view) {
            super(view);
            this.layout = (SquareRelativeLayout) view.findViewById(R.id.layoutBackground);
            this.textView = (AnalogClock) view.findViewById(R.id.iv_clock);
            this.premiumOverlay = view.findViewById(R.id.premiumOverlay);
            this.premiumLock = view.findViewById(R.id.premiumLock);
        }

        public AnalogClock getTextView() {
            return this.textView;
        }
    }

    public CustomAdapter(ArrayList<Clocks> arrayList) {
        this.localDataSet = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_clocks, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CustomAdapter.width = viewHolder.layout.getMeasuredWidth();
                CustomAdapter.height = viewHolder.layout.getMeasuredHeight();
                viewHolder.getTextView().setClock(CustomAdapter.this.localDataSet.get(i));
                viewHolder.getTextView().setClockSize(((float) CustomAdapter.width) / 1.8f);
                viewHolder.getTextView().setPosition(((float) CustomAdapter.width) / 2.18f, ((float) CustomAdapter.height) / 2.18f);
                viewHolder.layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        viewHolder.layout.setCardBackgroundColor(Color.parseColor(this.localDataSet.get(i).getBgColor()));
        viewHolder.getTextView().setClock(this.localDataSet.get(i));
        viewHolder.getTextView().setClockSize(((float) width) / 1.8f);
        viewHolder.getTextView().setPosition(((float) width) / 2.18f, ((float) height) / 2.18f);
        viewHolder.getTextView().setAutoUpdate(true);

        PremiumBadge.bind(viewHolder.premiumOverlay, viewHolder.premiumLock, i, getItemCount());

        viewHolder.getTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PremiumBadge.onItemClick(view, i, getItemCount(), new Runnable() {
                    @Override
                    public void run() {
                        CustomAdapter.this.fireClick(i);
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
        if (this.clickListener != null && position >= 0 && position < this.localDataSet.size()) {
            this.clickListener.setClick(this.localDataSet.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return this.localDataSet.size();
    }
}
