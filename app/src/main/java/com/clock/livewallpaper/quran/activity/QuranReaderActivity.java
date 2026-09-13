package com.clock.livewallpaper.quran.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.quran.adapter.QuranPageAdapter;
import com.clock.livewallpaper.quran.model.SurahItem;
import com.clock.livewallpaper.quran.utils.QuranDataProvider;
import com.clock.livewallpaper.quran.utils.QuranPreferences;
import com.clock.livewallpaper.utils.LocaleHelper;

public class QuranReaderActivity extends AppCompatActivity {

    private QuranPreferences quranPrefs;
    private boolean isArabic;

    private ViewPager2 viewPagerQuran;
    private RelativeLayout layoutTopOverlay;
    private RelativeLayout layoutBottomOverlay;
    private ImageView btnReaderBack;
    private TextView tvReaderSurahJuz;
    private ImageView btnReaderBookmark;
    private TextView tvReaderPageBottom;

    private boolean isOverlayVisible = true;
    private int currentPage = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on during reading
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_quran_reader);

        this.quranPrefs = new QuranPreferences(this);
        this.isArabic = LocaleHelper.isArabic(this);

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPagerQuran = findViewById(R.id.viewPagerQuran);
        layoutTopOverlay = findViewById(R.id.layoutTopOverlay);
        layoutBottomOverlay = findViewById(R.id.layoutBottomOverlay);
        btnReaderBack = findViewById(R.id.btnReaderBack);
        tvReaderSurahJuz = findViewById(R.id.tvReaderSurahJuz);
        btnReaderBookmark = findViewById(R.id.btnReaderBookmark);
        tvReaderPageBottom = findViewById(R.id.tvReaderPageBottom);

        btnReaderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReaderBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBookmarkAtCurrentPage();
            }
        });
    }

    private void setupViewPager() {
        int startPage = getIntent().getIntExtra(QuranIndexActivity.EXTRA_START_PAGE, quranPrefs.getLastReadPage());
        if (startPage < 1) {
            startPage = 1;
        } else if (startPage > QuranDataProvider.TOTAL_PAGES) {
            startPage = QuranDataProvider.TOTAL_PAGES;
        }
        currentPage = startPage;

        QuranPageAdapter adapter = new QuranPageAdapter(this, isArabic, new QuranPageAdapter.OnPageTapListener() {
            @Override
            public void onPageTap() {
                toggleOverlays();
            }
        });

        // Set ViewPager layout direction for RTL Quran reading
        viewPagerQuran.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        viewPagerQuran.setAdapter(adapter);
        viewPagerQuran.setCurrentItem(startPage - 1, false);

        updateHeaderAndFooter(startPage);

        viewPagerQuran.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position + 1;
                updateHeaderAndFooter(currentPage);
                quranPrefs.saveLastReadPage(currentPage);
            }
        });
    }

    private void updateHeaderAndFooter(int page) {
        tvReaderSurahJuz.setText(QuranDataProvider.getPageHeaderInfo(page, isArabic));

        if (isArabic) {
            tvReaderPageBottom.setText("صفحة " + page + " من " + QuranDataProvider.TOTAL_PAGES);
        } else {
            tvReaderPageBottom.setText("Page " + page + " of " + QuranDataProvider.TOTAL_PAGES);
        }

        // Update bookmark icon state
        boolean isBookmarked = (quranPrefs.getBookmarkPage() == page);
        btnReaderBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
    }

    private void saveBookmarkAtCurrentPage() {
        SurahItem surah = QuranDataProvider.getSurahForPage(currentPage);
        String surahName = surah.getNameAr();
        quranPrefs.saveBookmark(currentPage, surahName);

        btnReaderBookmark.setImageResource(R.drawable.ic_bookmark);

        String msg;
        if (isArabic) {
            msg = "تم حفظ العلامة: سورة " + surahName + " • صفحة " + currentPage;
        } else {
            msg = "Bookmark saved: Surah " + surah.getNameEn() + " • Page " + currentPage;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void toggleOverlays() {
        isOverlayVisible = !isOverlayVisible;
        if (isOverlayVisible) {
            fadeIn(layoutTopOverlay);
            fadeIn(layoutBottomOverlay);
        } else {
            fadeOut(layoutTopOverlay);
            fadeOut(layoutBottomOverlay);
        }
    }

    private void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        view.startAnimation(anim);
    }

    private void fadeOut(View view) {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(200);
        view.startAnimation(anim);
        view.setVisibility(View.GONE);
    }
}
