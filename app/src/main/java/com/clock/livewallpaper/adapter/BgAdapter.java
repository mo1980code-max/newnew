package com.clock.livewallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.utils.AutoBackground;



public class BgAdapter extends RecyclerView.Adapter<BgAdapter.ViewHolder> {
    private ClickListener clickListener;
    private int[] layouts = AutoBackground.BACKGROUNDS;



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
