package com.clock.livewallpaper.quran.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.quran.adapter.JuzAdapter;
import com.clock.livewallpaper.quran.adapter.SurahAdapter;
import com.clock.livewallpaper.quran.model.JuzItem;
import com.clock.livewallpaper.quran.model.SurahItem;
import com.clock.livewallpaper.quran.utils.QuranDataProvider;
import com.clock.livewallpaper.quran.utils.QuranPreferences;
import com.clock.livewallpaper.utils.LocaleHelper;

public class QuranIndexActivity extends AppCompatActivity {

    public static final String EXTRA_START_PAGE = "extra_start_page";

    private QuranPreferences quranPrefs;
    private boolean isArabic;

    private ImageView btnBack;
    private TextView tvIndexTitle;
    private ImageView btnSearchToggle;
    private LinearLayout layoutSearchBar;
    private EditText etSearchSurah;

    private View cardBookmark;
    private TextView tvBookmarkDetails;

    private Button btnTabSurahs;
    private Button btnTabJuz;

    private RecyclerView rvSurahs;
    private RecyclerView rvJuz;

    private SurahAdapter surahAdapter;
    private JuzAdapter juzAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_index);

        this.quranPrefs = new QuranPreferences(this);
        this.isArabic = LocaleHelper.isArabic(this);

        initViews();
        setupTabs();
        setupRecyclerViews();
        setupSearch();
        updateBookmarkCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBookmarkCard();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvIndexTitle = findViewById(R.id.tvIndexTitle);
        btnSearchToggle = findViewById(R.id.btnSearchToggle);
        layoutSearchBar = findViewById(R.id.layoutSearchBar);
        etSearchSurah = findViewById(R.id.etSearchSurah);

        cardBookmark = findViewById(R.id.cardBookmark);
        tvBookmarkDetails = findViewById(R.id.tvBookmarkDetails);

        btnTabSurahs = findViewById(R.id.btnTabSurahs);
        btnTabJuz = findViewById(R.id.btnTabJuz);

        rvSurahs = findViewById(R.id.rvSurahs);
        rvJuz = findViewById(R.id.rvJuz);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cardBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = quranPrefs.getBookmarkPage();
                openReaderAtPage(page);
            }
        });
    }

    private void updateBookmarkCard() {
        int page = quranPrefs.getBookmarkPage();
        String defaultName = isArabic ? "الفاتحة" : "Al-Fatihah";
        String surah = quranPrefs.getBookmarkSurah(defaultName);

        if (isArabic) {
            tvBookmarkDetails.setText("سورة " + surah + " • صفحة " + page);
        } else {
            tvBookmarkDetails.setText("Surah " + surah + " • Page " + page);
        }
    }

    private void setupTabs() {
        btnTabSurahs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSurahsTab();
            }
        });

        btnTabJuz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJuzTab();
            }
        });
    }

    private void showSurahsTab() {
        btnTabSurahs.setBackgroundResource(R.drawable.quran_tab_selected);
        btnTabSurahs.setTextColor(Color.parseColor("#161E2E"));
        btnTabSurahs.setTypeface(null, android.graphics.Typeface.BOLD);

        btnTabJuz.setBackgroundResource(R.drawable.quran_tab_unselected);
        btnTabJuz.setTextColor(Color.parseColor("#8F9BB3"));
        btnTabJuz.setTypeface(null, android.graphics.Typeface.NORMAL);

        rvSurahs.setVisibility(View.VISIBLE);
        rvJuz.setVisibility(View.GONE);
        btnSearchToggle.setVisibility(View.VISIBLE);
    }

    private void showJuzTab() {
        btnTabJuz.setBackgroundResource(R.drawable.quran_tab_selected);
        btnTabJuz.setTextColor(Color.parseColor("#161E2E"));
        btnTabJuz.setTypeface(null, android.graphics.Typeface.BOLD);

        btnTabSurahs.setBackgroundResource(R.drawable.quran_tab_unselected);
        btnTabSurahs.setTextColor(Color.parseColor("#8F9BB3"));
        btnTabSurahs.setTypeface(null, android.graphics.Typeface.NORMAL);

        rvJuz.setVisibility(View.VISIBLE);
        rvSurahs.setVisibility(View.GONE);
        btnSearchToggle.setVisibility(View.GONE);
        layoutSearchBar.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        // Surahs
        rvSurahs.setLayoutManager(new LinearLayoutManager(this));
        surahAdapter = new SurahAdapter(this, QuranDataProvider.getSurahs(), isArabic, new SurahAdapter.OnSurahClickListener() {
            @Override
            public void onSurahClick(SurahItem surah) {
                openReaderAtPage(surah.getStartPage());
            }
        });
        rvSurahs.setAdapter(surahAdapter);

        // Juz
        rvJuz.setLayoutManager(new LinearLayoutManager(this));
        juzAdapter = new JuzAdapter(this, QuranDataProvider.getJuzList(), isArabic, new JuzAdapter.OnJuzClickListener() {
            @Override
            public void onJuzClick(JuzItem juz) {
                openReaderAtPage(juz.getStartPage());
            }
        });
        rvJuz.setAdapter(juzAdapter);
    }

    private void setupSearch() {
        btnSearchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutSearchBar.getVisibility() == View.VISIBLE) {
                    layoutSearchBar.setVisibility(View.GONE);
                    etSearchSurah.setText("");
                } else {
                    layoutSearchBar.setVisibility(View.VISIBLE);
                    etSearchSurah.requestFocus();
                }
            }
        });

        etSearchSurah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (surahAdapter != null) {
                    surahAdapter.filter(s != null ? s.toString() : "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void openReaderAtPage(int page) {
        Intent intent = new Intent(this, QuranReaderActivity.class);
        intent.putExtra(EXTRA_START_PAGE, page);
        startActivity(intent);
    }
}
