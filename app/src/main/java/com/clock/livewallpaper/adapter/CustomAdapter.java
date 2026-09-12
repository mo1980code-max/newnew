package com.clock.livewallpaper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.Clocks;
import com.clock.livewallpaper.viewUtils.AnalogClock;
import com.clock.livewallpaper.viewUtils.SquareRelativeLayout;

import java.util.ArrayList;




public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    static int height;
    static int width;
    private ClickListener clickListener;
    private ArrayList<Clocks> localDataSet;



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

        public ViewHolder(View view) {
            super(view);
            this.layout = (SquareRelativeLayout) view.findViewById(R.id.layoutBackground);
            this.textView = (AnalogClock) view.findViewById(R.id.iv_clock);
        }

        public AnalogClock getTextView() {
            return this.textView;
        }
    }

    public CustomAdapter(ArrayList<Clocks> arrayList) {
        this.localDataSet = arrayList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_clocks, viewGroup, false));
    }

    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        viewHolder.layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                CustomAdapter.width = viewHolder.layout.getMeasuredWidth();
                CustomAdapter.height = viewHolder.layout.getMeasuredHeight();
                viewHolder.getTextView().setClock((Clocks) CustomAdapter.this.localDataSet.get(i));
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
        viewHolder.getTextView().setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (CustomAdapter.this.clickListener != null) {
                    CustomAdapter.this.clickListener.setClick((Clocks) CustomAdapter.this.localDataSet.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.localDataSet.size();
    }
}
