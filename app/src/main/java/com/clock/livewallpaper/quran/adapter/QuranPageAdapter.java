package com.clock.livewallpaper.quran.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.quran.model.SurahItem;
import com.clock.livewallpaper.quran.utils.QuranDataProvider;

public class QuranPageAdapter extends RecyclerView.Adapter<QuranPageAdapter.PageViewHolder> {

    public interface OnPageTapListener {
        void onPageTap();
    }

    private Context context;
    private boolean isArabic;
    private OnPageTapListener tapListener;

    public QuranPageAdapter(Context context, boolean isArabic, OnPageTapListener tapListener) {
        this.context = context;
        this.isArabic = isArabic;
        this.tapListener = tapListener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quran_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        int page = position + 1; // 1 to 604
        SurahItem surah = QuranDataProvider.getSurahForPage(page);
        int juz = QuranDataProvider.getJuzNumberForPage(page);

        // Top inside headers
        holder.tvPageSurahHeader.setText(isArabic ? "سورة " + surah.getNameAr() : "Surah " + surah.getNameEn());
        holder.tvPageJuzHeader.setText(isArabic ? "الجزء " + juz : "Juz " + juz);

        // Surah Banner & Bismillah
        boolean isFirstPageOfSurah = (surah.getStartPage() == page);
        if (isFirstPageOfSurah) {
            holder.layoutSurahTitleBanner.setVisibility(View.VISIBLE);
            holder.tvSurahBannerTitle.setText(isArabic ? "سورة " + surah.getNameAr() : "Surah " + surah.getNameEn());
            holder.tvSurahBannerSub.setText(surah.getTypeName(isArabic) + " • " + surah.getAyahCountText(isArabic));

            if (surah.getNumber() != 9 && surah.getNumber() != 1) {
                holder.tvBismillah.setVisibility(View.VISIBLE);
            } else {
                holder.tvBismillah.setVisibility(View.GONE);
            }
        } else {
            holder.layoutSurahTitleBanner.setVisibility(View.GONE);
            holder.tvBismillah.setVisibility(View.GONE);
        }

        // Page text content
        holder.tvQuranText.setText(getPageTextContent(page, surah));

        // Bottom page number
        holder.tvPageNumberBottom.setText(String.valueOf(page));

        holder.layoutPageContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tapListener != null) {
                    tapListener.onPageTap();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return QuranDataProvider.TOTAL_PAGES;
    }

    private String getPageTextContent(int page, SurahItem surah) {
        if (page == 1) {
            return "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾\n" +
                    "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾\n" +
                    "الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾\n" +
                    "مَالِكِ يَوْمِ الدِّينِ ﴿٤﴾\n" +
                    "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٥﴾\n" +
                    "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٦﴾\n" +
                    "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴿٧﴾";
        } else if (page == 2) {
            return "الم ﴿١﴾\n" +
                    "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِلْمُتَّقِينَ ﴿٢﴾\n" +
                    "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنْفِقُونَ ﴿٣﴾\n" +
                    "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنْزِلَ إِلَيْكَ وَمَا أُنْزِلَ مِنْ قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ ﴿٤﴾\n" +
                    "أُولَٰئِكَ عَلَىٰ هُدًى مِنْ رَبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ ﴿٥﴾";
        } else if (page == 3) {
            return "إِنَّ الَّذِينَ كَفَرُوا سَوَاءٌ عَلَيْهِمْ أَأَنْذَرْتَهُمْ أَمْ لَمْ تُنْذِرْهُمْ لَا يُؤْمِنُونَ ﴿٦﴾\n" +
                    "خَتَمَ اللَّهُ عَلَىٰ قُلُوبِهِمْ وَعَلَىٰ سَمْعِهِمْ ۖ وَعَلَىٰ أَبْصَارِهِمْ غِشَاوَةٌ ۖ وَلَهُمْ عَذَابٌ عَظِيمٌ ﴿٧﴾\n" +
                    "وَمِنَ النَّاسِ مَنْ يَقُولُ آمَنَّا بِاللَّهِ وَبِالْيَوْمِ الْآخِرِ وَمَا هُمْ بِمُؤْمِنِينَ ﴿٨﴾\n" +
                    "يُخَادِعُونَ اللَّهَ وَالَّذِينَ آمَنُوا وَمَا يَخْدَعُونَ إِلَّا أَنْفُسَهُمْ وَمَا يَشْعُرُونَ ﴿٩﴾";
        } else if (page == 293) {
            return "الْحَمْدُ لِلَّهِ الَّذِي أَنْزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَلْ لَهُ عِوَجًا ۜ ﴿١﴾\n" +
                    "قَيِّمًا لِيُنْذِرَ بَأْسًا شَدِيدًا مِنْ لَدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا ﴿٢﴾\n" +
                    "مَاكِثِينَ فِيهِ أَبَدًا ﴿٣﴾\n" +
                    "وَيُنْذِرَ الَّذِينَ قَالُوا اتَّخَذَ اللَّهُ وَلَدًا ﴿٤﴾\n" +
                    "مَا لَهُمْ بِهِ مِنْ عِلْمٍ وَلَا لِآبَائِهِمْ ۚ كَبُرَتْ كَلِمَةً تَخْرُجُ مِنْ أَفْوَاهِهِمْ ۚ إِنْ يَقُولُونَ إِلَّا كَذِبًا ﴿٥﴾";
        } else if (page == 562) {
            return "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ﴿١﴾\n" +
                    "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ ﴿٢﴾\n" +
                    "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِنْ تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِنْ فُطُورٍ ﴿٣﴾\n" +
                    "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنْقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ ﴿٤﴾";
        } else if (page == 604) {
            return "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n" +
                    "قُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ﴿٤﴾\n\n" +
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n" +
                    "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِنْ شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾\n\n" +
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n" +
                    "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَٰهِ النَّاسِ ﴿٣﴾ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾";
        }

        // Generic page format with Surah name and page indicator
        return "﴿ سورة " + surah.getNameAr() + " — الصفحة " + page + " ﴾\n\n" +
                "﴿ وَإِذَا قُرِئَ الْقُرْآنُ فَاسْتَمِعُوا لَهُ وَأَنْصِتُوا لَعَلَّكُمْ تُرْحَمُونَ ﴾\n\n" +
                "﴿ كِتَابٌ أَنْزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِيَدَّبَّرُوا آيَاتِهِ وَلِيَتَذَكَّرَ أُولُو الْأَلْبَابِ ﴾\n\n" +
                "﴿ إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ وَيُبَشِّرُ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا كَبِيرًا ﴾";
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutPageContent;
        TextView tvPageJuzHeader;
        TextView tvPageSurahHeader;
        LinearLayout layoutSurahTitleBanner;
        TextView tvSurahBannerTitle;
        TextView tvSurahBannerSub;
        TextView tvBismillah;
        TextView tvQuranText;
        TextView tvPageNumberBottom;

        PageViewHolder(View itemView) {
            super(itemView);
            layoutPageContent = itemView.findViewById(R.id.layoutPageContent);
            tvPageJuzHeader = itemView.findViewById(R.id.tvPageJuzHeader);
            tvPageSurahHeader = itemView.findViewById(R.id.tvPageSurahHeader);
            layoutSurahTitleBanner = itemView.findViewById(R.id.layoutSurahTitleBanner);
            tvSurahBannerTitle = itemView.findViewById(R.id.tvSurahBannerTitle);
            tvSurahBannerSub = itemView.findViewById(R.id.tvSurahBannerSub);
            tvBismillah = itemView.findViewById(R.id.tvBismillah);
            tvQuranText = itemView.findViewById(R.id.tvQuranText);
            tvPageNumberBottom = itemView.findViewById(R.id.tvPageNumberBottom);
        }
    }
}
