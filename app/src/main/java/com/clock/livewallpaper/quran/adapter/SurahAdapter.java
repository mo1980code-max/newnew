package com.clock.livewallpaper.quran.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.quran.model.SurahItem;

import java.util.ArrayList;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener {
        void onSurahClick(SurahItem surah);
    }

    private Context context;
    private List<SurahItem> originalList;
    private List<SurahItem> filteredList;
    private OnSurahClickListener listener;
    private boolean isArabic;

    public SurahAdapter(Context context, List<SurahItem> list, boolean isArabic, OnSurahClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<SurahItem>();
        this.filteredList = new ArrayList<SurahItem>(this.originalList);
        this.isArabic = isArabic;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final SurahItem item = filteredList.get(position);
        holder.tvSurahNumber.setText(String.valueOf(item.getNumber()));
        holder.tvSurahNameAr.setText(item.getNameAr());
        holder.tvSurahNameEn.setText(item.getNameEn());
        holder.tvSurahType.setText(item.getTypeName(isArabic));
        holder.tvAyahCount.setText(item.getAyahCountText(isArabic));
        holder.tvStartPage.setText(item.getStartPageText(isArabic));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSurahClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String q = query.trim().toLowerCase();
            for (SurahItem item : originalList) {
                if (item.getNameAr().toLowerCase().contains(q)
                        || item.getNameEn().toLowerCase().contains(q)
                        || String.valueOf(item.getNumber()).equals(q)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSurahNumber;
        TextView tvSurahNameAr;
        TextView tvSurahNameEn;
        TextView tvSurahType;
        TextView tvAyahCount;
        TextView tvStartPage;

        ViewHolder(View itemView) {
            super(itemView);
            tvSurahNumber = itemView.findViewById(R.id.tvSurahNumber);
            tvSurahNameAr = itemView.findViewById(R.id.tvSurahNameAr);
            tvSurahNameEn = itemView.findViewById(R.id.tvSurahNameEn);
            tvSurahType = itemView.findViewById(R.id.tvSurahType);
            tvAyahCount = itemView.findViewById(R.id.tvAyahCount);
            tvStartPage = itemView.findViewById(R.id.tvStartPage);
        }
    }
}
