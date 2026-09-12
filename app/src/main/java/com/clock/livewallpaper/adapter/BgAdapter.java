package com.clock.livewallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;



public class BgAdapter extends RecyclerView.Adapter<BgAdapter.ViewHolder> {
    private ClickListener clickListener;
    private int[] layouts = {R.drawable.background7, R.drawable.background1, R.drawable.background2, R.drawable.background3, R.drawable.background4, R.drawable.background5, R.drawable.background6, R.drawable.background8, R.drawable.background9, R.drawable.pattern_gradient_01, R.drawable.pattern_gradient_02, R.drawable.pattern_gradient_03, R.drawable.pattern_gradient_04, R.drawable.pattern_gradient_05, R.drawable.pattern_gradient_06, R.drawable.pattern_gradient_07, R.drawable.pattern_gradient_08, R.drawable.pattern_gradient_09, R.drawable.pattern_gradient_10, R.drawable.pattern_gradient_11, R.drawable.pattern_gradient_12, R.drawable.pattern_gradient_13, R.drawable.pattern_gradient_14, R.drawable.pattern_gradient_15, R.drawable.pattern_gradient_16, R.drawable.pattern_gradient_17, R.drawable.pattern_gradient_18, R.drawable.pattern_gradient_19, R.drawable.pattern_gradient_20, R.drawable.pattern_gradient_21, R.drawable.pattern_gradient_22, R.drawable.pattern_gradient_23, R.drawable.bg1, R.drawable.bg2, R.drawable.bg3, R.drawable.bg4, R.drawable.bg5};



    public interface ClickListener {
        void setClick(int i);
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
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bg, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.viewStub.setImageResource(this.layouts[i]);
        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (BgAdapter.this.clickListener != null) {
                    BgAdapter.this.clickListener.setClick(BgAdapter.this.layouts[i]);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.layouts.length;
    }
}
