package com.clock.livewallpaper.quran.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.quran.model.JuzItem;

import java.util.ArrayList;
import java.util.List;

public class JuzAdapter extends RecyclerView.Adapter<JuzAdapter.ViewHolder> {

    public interface OnJuzClickListener {
        void onJuzClick(JuzItem juz);
    }

    private Context context;
    private List<JuzItem> list;
    private OnJuzClickListener listener;
    private boolean isArabic;

    public JuzAdapter(Context context, List<JuzItem> list, boolean isArabic, OnJuzClickListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<JuzItem>();
        this.isArabic = isArabic;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_juz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final JuzItem item = list.get(position);
        holder.tvJuzNumber.setText(String.valueOf(item.getJuzNumber()));
        holder.tvJuzTitle.setText(item.getName(isArabic));
        holder.tvStartQuote.setText("«" + item.getStartAyahQuote(isArabic) + "»");
        holder.tvStartSurah.setText((isArabic ? "سورة " : "Surah ") + item.getStartSurah(isArabic));
        holder.tvJuzPage.setText(item.getStartPageText(isArabic));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onJuzClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJuzNumber;
        TextView tvJuzTitle;
        TextView tvStartQuote;
        TextView tvStartSurah;
        TextView tvJuzPage;

        ViewHolder(View itemView) {
            super(itemView);
            tvJuzNumber = itemView.findViewById(R.id.tvJuzNumber);
            tvJuzTitle = itemView.findViewById(R.id.tvJuzTitle);
            tvStartQuote = itemView.findViewById(R.id.tvStartQuote);
            tvStartSurah = itemView.findViewById(R.id.tvStartSurah);
            tvJuzPage = itemView.findViewById(R.id.tvJuzPage);
        }
    }
}
