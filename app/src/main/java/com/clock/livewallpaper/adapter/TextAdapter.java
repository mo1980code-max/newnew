package com.clock.livewallpaper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.TextClocks;
import com.clock.livewallpaper.viewUtils.SquareRelativeLayout;

import java.util.ArrayList;




public class TextAdapter extends RecyclerView.Adapter<TextAdapter.ViewHolder> {
    private ClickListener clickListener;
    private View[] layouts;
    ArrayList<TextClocks> textClocks;
    int width = 0;
    int height = 0;



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

        public ViewHolder(View view) {
            super(view);
            this.viewStub = (ImageView) view.findViewById(R.id.clockwise);
            this.layout = (SquareRelativeLayout) view.findViewById(R.id.layoutBackground);
        }
    }

    public TextAdapter(ArrayList<TextClocks> arrayList) {
        this.textClocks = arrayList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_textclock, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.layout.setCardBackgroundColor(Color.parseColor(this.textClocks.get(i).getBgColor()));
        viewHolder.viewStub.setImageResource(this.textClocks.get(i).getThumb());
        viewHolder.viewStub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextAdapter.this.clickListener != null) {
                    TextAdapter.this.clickListener.setClick(i, TextAdapter.this.textClocks.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.textClocks.size();
    }
}
