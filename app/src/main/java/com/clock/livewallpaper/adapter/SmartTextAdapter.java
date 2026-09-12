package com.clock.livewallpaper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.SmartClocks;
import com.clock.livewallpaper.viewUtils.SquareRelativeLayout;

import java.util.ArrayList;





public class SmartTextAdapter extends RecyclerView.Adapter<SmartTextAdapter.ViewHolder> {
    static int height = 300;
    static int width = 300;
    private ClickListener clickListener;
    private View[] layouts;
    ArrayList<SmartClocks> smartClocks;



    public interface ClickListener {
        void setClick(int i, SmartClocks smartClocks);
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bgImage;
        SquareRelativeLayout layout;
        private final ImageView viewStub;

        public ViewHolder(View view) {
            super(view);
            this.viewStub = (ImageView) view.findViewById(R.id.clockwise);
            this.layout = (SquareRelativeLayout) view.findViewById(R.id.layoutBackground);
            this.bgImage = (ImageView) view.findViewById(R.id.bgImage);
        }
    }

    public SmartTextAdapter(ArrayList<SmartClocks> arrayList) {
        this.smartClocks = arrayList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_smarttextclock, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.viewStub.setImageResource(this.smartClocks.get(i).getThumb());
        viewHolder.bgImage.setImageResource(this.smartClocks.get(i).getBgColor());
        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (SmartTextAdapter.this.clickListener != null) {
                    SmartTextAdapter.this.clickListener.setClick(i, SmartTextAdapter.this.smartClocks.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.smartClocks.size();
    }
}
