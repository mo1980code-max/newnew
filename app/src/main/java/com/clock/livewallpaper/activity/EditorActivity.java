package com.clock.livewallpaper.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clock.livewallpaper.R;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import com.clock.livewallpaper.LiveClockWallpaper;
import com.clock.livewallpaper.adapter.BgAdapter;
import com.clock.livewallpaper.model.Clocks;
import com.clock.livewallpaper.utils.AutoBackground;
import com.clock.livewallpaper.utils.LocaleHelper;
import com.clock.livewallpaper.utils.RealPathUtil;
import com.clock.livewallpaper.utils.TinyDB;
import com.clock.livewallpaper.viewUtils.AnalogClock;
import com.clock.livewallpaper.viewUtils.SmartClockPreview;
import com.clock.livewallpaper.viewUtils.TextClockPreview;

import java.io.File;
import java.util.ArrayList;



public class EditorActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SELECT_PICTURE = 1;
    public static boolean isDone = false;
    private AnalogClock analogClock;
    private BgAdapter bgAdapter;
    private RecyclerView bgRecyclerView;
    private Button btnColor1;
    private Button btnColor2;
    private ImageView btnOk;
    private ImageView icDone;
    private ImageView ivBackground;
    private ImageView ivCenter;
    private ImageView ivColor;
    private ImageView ivGallery;
    private ImageView ivTextColor;
    private ImageView ivZoomIn;
    private ImageView ivZoomOut;
    private LinearLayout layoutBottom;
    private LinearLayout layoutColor;
    private LinearLayout layoutSliders;
    private LinearLayout layoutBgPanel;
    private LinearLayout rowCardAlpha;
    private LinearLayout rowTextSize;
    private int mClockSize;
    private int mCardAlphaPct;
    private int mTextScalePct;
    private int mHeight;
    private ImageView mIvMainScreen;
    private ImageView ivAutoBg;
    public int mWidth;
    private AppCompatSeekBar seekBar;
    private AppCompatSeekBar seekBarCardAlpha;
    private AppCompatSeekBar seekBarTextSize;
    private TextView tvSizeValue;
    private TextView tvCardAlphaValue;
    private TextView tvTextSizeValue;
    private SmartClockPreview smartClockPreview;
    private TextClockPreview textClockPreview;
    TinyDB tinyDB;
    String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE"};
    private float mClockPosX = 100.0f;
    private float mClockPosY = 100.0f;
    private int textClockPosition = 0;
    private boolean mIsCenterLine = false;
    private int color1 = -1;
    private int color2 = InputDeviceCompat.SOURCE_ANY;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.activity_editor);
        isDone = false;
        this.tinyDB = new TinyDB(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mWidth = displayMetrics.widthPixels;
        this.mHeight = displayMetrics.heightPixels;
        initView();
    }

    @Override
    public void onBackPressed() {

        EditorActivity.this.finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideMenu();
    }

    public void hideMenu() {
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    private void initView() {
        this.analogClock = (AnalogClock) findViewById(R.id.analogClock);
        this.textClockPreview = (TextClockPreview) findViewById(R.id.textClockPreview);
        this.smartClockPreview = (SmartClockPreview) findViewById(R.id.smartClockPreview);
        this.bgRecyclerView = (RecyclerView) findViewById(R.id.bgRecyclerView);
        this.layoutBottom = (LinearLayout) findViewById(R.id.layoutBottom);
        this.mIvMainScreen = (ImageView) findViewById(R.id.imageView);
        this.ivColor = (ImageView) findViewById(R.id.ivColor);
        this.ivGallery = (ImageView) findViewById(R.id.ivGallery);
        this.ivBackground = (ImageView) findViewById(R.id.ivBackground);
        this.btnOk = (ImageView) findViewById(R.id.btn_ok);
        this.ivZoomOut = (ImageView) findViewById(R.id.ivZoomOut);
        this.ivTextColor = (ImageView) findViewById(R.id.ivTextColor);
        this.ivZoomIn = (ImageView) findViewById(R.id.ivZoomIn);
        this.ivCenter = (ImageView) findViewById(R.id.ivCenter);
        this.icDone = (ImageView) findViewById(R.id.icDone);
        this.layoutColor = (LinearLayout) findViewById(R.id.layoutColor);
        this.btnColor1 = (Button) findViewById(R.id.btnColor1);
        this.btnColor2 = (Button) findViewById(R.id.btnColor2);
        this.seekBar = (AppCompatSeekBar) findViewById(R.id.seekBar);
        this.seekBarCardAlpha = (AppCompatSeekBar) findViewById(R.id.seekBarCardAlpha);
        this.seekBarTextSize = (AppCompatSeekBar) findViewById(R.id.seekBarTextSize);
        this.layoutSliders = (LinearLayout) findViewById(R.id.layoutSliders);
        this.layoutBgPanel = (LinearLayout) findViewById(R.id.layoutBgPanel);
        this.rowCardAlpha = (LinearLayout) findViewById(R.id.rowCardAlpha);
        this.rowTextSize = (LinearLayout) findViewById(R.id.rowTextSize);
        this.tvSizeValue = (TextView) findViewById(R.id.tvSizeValue);
        this.tvCardAlphaValue = (TextView) findViewById(R.id.tvCardAlphaValue);
        this.tvTextSizeValue = (TextView) findViewById(R.id.tvTextSizeValue);
        this.ivAutoBg = (ImageView) findViewById(R.id.ivAutoBg);
        this.analogClock.setAutoUpdate(true);
        this.bgRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        getUserSettings();
        this.ivZoomOut.setOnClickListener(this);
        this.ivZoomIn.setOnClickListener(this);
        this.ivColor.setOnClickListener(this);
        this.ivCenter.setOnClickListener(this);
        this.ivGallery.setOnClickListener(this);
        this.ivBackground.setOnClickListener(this);
        this.ivTextColor.setOnClickListener(this);
        this.btnColor1.setOnClickListener(this);
        this.btnColor2.setOnClickListener(this);
        this.icDone.setOnClickListener(this);
        this.btnOk.setOnClickListener(this);
        BgAdapter bgAdapter = new BgAdapter();
        this.bgAdapter = bgAdapter;
        this.bgRecyclerView.setAdapter(bgAdapter);
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditorActivity.this.mClockSize = i;
                EditorActivity.this.tvSizeValue.setText(String.valueOf(i));
                EditorActivity.this.updateClock();
            }
        });
        this.seekBarCardAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditorActivity.this.mCardAlphaPct = i;
                EditorActivity.this.tvCardAlphaValue.setText(i + "%");
                EditorActivity.this.updateClock();
            }
        });
        this.seekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditorActivity.this.mTextScalePct = i + 50;
                EditorActivity.this.tvTextSizeValue.setText(EditorActivity.this.mTextScalePct + "%");
                EditorActivity.this.updateClock();
            }
        });
        this.ivAutoBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditorActivity.this.showAutoBgDialog();
            }
        });
        this.mIvMainScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != 2) {
                    return false;
                }
                float f = (float) ((int) (EditorActivity.this.mClockPosX + ((float) (EditorActivity.this.mClockSize / 2))));
                float f2 = (float) ((int) (EditorActivity.this.mClockPosY - ((float) (EditorActivity.this.mClockSize / 2))));
                float f3 = (float) ((int) (EditorActivity.this.mClockPosY + ((float) (EditorActivity.this.mClockSize / 2))));
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (x <= ((float) ((int) (EditorActivity.this.mClockPosX - ((float) (EditorActivity.this.mClockSize / 2))))) || x >= f || y <= f2 || y >= f3) {
                    return false;
                }
                if (EditorActivity.this.mIsCenterLine) {
                    EditorActivity editorActivity = EditorActivity.this;
                    editorActivity.mClockPosX = ((float) editorActivity.mWidth) / 2.0f;
                } else {
                    EditorActivity.this.mClockPosX = motionEvent.getX();
                }
                EditorActivity.this.mClockPosY = motionEvent.getY();
                EditorActivity.this.updateClock();
                return false;
            }
        });
        this.bgAdapter.setClickListener(new BgAdapter.ClickListener() {
            @Override
            public void setClick(int i) {
                EditorActivity.this.tinyDB.putInt("customBg", i);
                EditorActivity.this.tinyDB.putBoolean("isImage", false);
                EditorActivity.this.tinyDB.putBoolean("isCustomBg", true);
                EditorActivity.this.tinyDB.putInt("autoBgMode", AutoBackground.MODE_OFF);
                EditorActivity.this.updateClock();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnColor1:
                ColorPickerDialogBuilder.with(this).setTitle(getString(R.string.choose_color)).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12).setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i) {
                    }
                }).setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] numArr) {
                        EditorActivity.this.hideMenu();
                        EditorActivity.this.color1 = i;
                        EditorActivity.this.updateClock();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditorActivity.this.hideMenu();
                    }
                }).build().show();
                return;
            case R.id.btnColor2:
                ColorPickerDialogBuilder.with(this).setTitle(getString(R.string.choose_color)).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12).setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i) {
                    }
                }).setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] numArr) {
                        EditorActivity.this.hideMenu();
                        EditorActivity.this.color2 = i;
                        EditorActivity.this.updateClock();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditorActivity.this.hideMenu();
                    }
                }).build().show();
                return;
            case R.id.btn_ok:

                EditorActivity editorActivity = EditorActivity.this;
                EditorActivity.this.layoutBgPanel.setVisibility(View.GONE);
                EditorActivity.this.layoutSliders.setVisibility(View.GONE);
                EditorActivity.this.layoutColor.setVisibility(View.VISIBLE);
                EditorActivity.this.saveUserSettings();
                EditorActivity.isDone = true;
                Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
                intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", new ComponentName(EditorActivity.this, LiveClockWallpaper.class));
                EditorActivity.this.startActivity(intent);
                EditorActivity.this.finish();

                return;
            case R.id.icDone:
                this.tinyDB.putInt("textColor1", this.color1);
                this.tinyDB.putInt("textColor2", this.color2);
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                return;
            case R.id.ivBackground:
                this.layoutBgPanel.setVisibility(View.VISIBLE);
                this.layoutSliders.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                return;
            case R.id.ivCenter:
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                this.mIsCenterLine = !this.mIsCenterLine;
                this.mClockPosX = ((float) this.mWidth) / 2.0f;
                updateClock();
                return;
            case R.id.ivColor:
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                ColorPickerDialogBuilder.with(this).setTitle(getString(R.string.choose_color)).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12).setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i) {
                    }
                }).setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] numArr) {
                        EditorActivity.this.hideMenu();
                        EditorActivity.this.tinyDB.putBoolean("isImage", false);
                        EditorActivity.this.tinyDB.putBoolean("isCustomBg", false);
                        EditorActivity.this.tinyDB.putInt("bgColor", i);
                        EditorActivity.this.tinyDB.putInt("autoBgMode", AutoBackground.MODE_OFF);
                        EditorActivity.this.updateClock();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditorActivity.this.hideMenu();
                    }
                }).build().show();
                return;
            case R.id.ivGallery:
                galleryIntent();
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                return;
            case R.id.ivTextColor:
                this.layoutColor.setVisibility(View.VISIBLE);
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.GONE);
                return;
            case R.id.ivZoomIn:
            case R.id.ivZoomOut:
                this.layoutBgPanel.setVisibility(View.GONE);
                this.layoutColor.setVisibility(View.GONE);
                this.layoutSliders.setVisibility(View.VISIBLE);
                return;
            default:
                return;
        }
    }

    private void showAutoBgDialog() {
        final String[] modes = new String[]{getString(R.string.auto_bg_off),
                getString(R.string.auto_bg_daily), getString(R.string.auto_bg_day_night)};
        int current = this.tinyDB.getInt("autoBgMode");
        if (current < AutoBackground.MODE_OFF || current > AutoBackground.MODE_DAY_NIGHT) {
            current = AutoBackground.MODE_OFF;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.auto_bg)
                .setSingleChoiceItems(modes, current, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditorActivity.this.tinyDB.putInt("autoBgMode", which);
                        EditorActivity.this.updateClock();
                        Toast.makeText(EditorActivity.this,
                                EditorActivity.this.getString(R.string.auto_bg_current, modes[which]),
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        EditorActivity.this.hideMenu();
                    }
                })
                .show();
    }

    public void saveUserSettings() {
        float f = this.mClockPosX + 0.01f;
        this.mClockPosX = f;
        this.tinyDB.putFloat("prefClockPosX", f);
        this.tinyDB.putFloat("prefClockPosY", this.mClockPosY);
        this.tinyDB.putInt("prefSize", this.mClockSize);
        this.tinyDB.putInt("prefCardAlpha", this.mCardAlphaPct);
        this.tinyDB.putInt("prefTextScale", this.mTextScalePct);
        this.tinyDB.putInt("textClockPosition", this.textClockPosition);
        updateClock();
    }

    private void getUserSettings() {
        this.mClockPosX = this.tinyDB.getFloat("prefClockPosX", ((float) this.mWidth) / 2.0f);
        this.mClockPosY = this.tinyDB.getFloat("prefClockPosY", ((float) this.mHeight) / 2.0f);
        int i = this.tinyDB.getInt("prefSize");
        this.mClockSize = i;
        if (i == 0) {
            this.mClockSize = 500;
        }
        this.seekBar.setProgress(this.mClockSize);
        this.tvSizeValue.setText(String.valueOf(this.mClockSize));
        int cardAlpha = this.tinyDB.getInt("prefCardAlpha");
        if (cardAlpha < 0) {
            cardAlpha = 0;
        }
        if (cardAlpha > 100) {
            cardAlpha = 100;
        }
        this.mCardAlphaPct = cardAlpha;
        this.seekBarCardAlpha.setProgress(cardAlpha);
        this.tvCardAlphaValue.setText(cardAlpha + "%");
        int textScale = this.tinyDB.getInt("prefTextScale", 100);
        if (textScale < 50) {
            textScale = 50;
        }
        if (textScale > 150) {
            textScale = 150;
        }
        this.mTextScalePct = textScale;
        this.seekBarTextSize.setProgress(textScale - 50);
        this.tvTextSizeValue.setText(textScale + "%");
        this.textClockPosition = this.tinyDB.getInt("textClockPosition");
        if (this.tinyDB.getInt("clockType") == 0) {
            this.analogClock.setVisibility(View.VISIBLE);
            this.textClockPreview.setVisibility(View.GONE);
            this.smartClockPreview.setVisibility(View.GONE);
            this.ivTextColor.setVisibility(View.GONE);
            this.rowCardAlpha.setVisibility(View.GONE);
            this.rowTextSize.setVisibility(View.GONE);
        } else if (this.tinyDB.getInt("clockType") == 1) {
            this.analogClock.setVisibility(View.GONE);
            this.textClockPreview.setVisibility(View.GONE);
            this.smartClockPreview.setVisibility(View.VISIBLE);
            this.ivTextColor.setVisibility(View.GONE);
        } else if (this.tinyDB.getInt("clockType") == 2) {
            this.analogClock.setVisibility(View.GONE);
            this.textClockPreview.setVisibility(View.VISIBLE);
            this.smartClockPreview.setVisibility(View.GONE);
        }
        saveUserSettings();
    }

    public void updateClock() {
        if (this.tinyDB.getInt("clockType") == 0) {
            this.analogClock.setClock((Clocks) this.tinyDB.getObject("clocks", Clocks.class));
            this.analogClock.setClockSize((float) this.mClockSize);
            this.analogClock.setTouchEnable(true);
            this.analogClock.setPosition(this.mClockPosX, this.mClockPosY);
        } else if (this.tinyDB.getInt("clockType") == 1) {
            this.smartClockPreview.setTextClockPosition(this.textClockPosition);
            this.smartClockPreview.setCardAlpha((this.mCardAlphaPct * 255) / 100);
            this.smartClockPreview.setTextScale(((float) this.mTextScalePct) / 100.0f);
            this.smartClockPreview.config(this.mClockPosX, this.mClockPosY, (int) (((float) this.mClockSize) * 2.0f));
        } else if (this.tinyDB.getInt("clockType") == 2) {
            this.textClockPreview.setTextClockPosition(this.textClockPosition);
            this.textClockPreview.setColors(this.color1, this.color2);
            this.textClockPreview.setCardAlpha((this.mCardAlphaPct * 255) / 100);
            this.textClockPreview.setTextScale(((float) this.mTextScalePct) / 100.0f);
            this.textClockPreview.config(this.mClockPosX, this.mClockPosY, (int) (((float) this.mClockSize) * 2.0f));
        }
        this.mIvMainScreen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int autoBgMode = this.tinyDB.getInt("autoBgMode");
        if (autoBgMode == AutoBackground.MODE_DAILY) {
            this.mIvMainScreen.setImageResource(AutoBackground.resolveDaily());
        } else if (autoBgMode == AutoBackground.MODE_DAY_NIGHT) {
            this.mIvMainScreen.setImageResource(AutoBackground.resolveDayNight());
        } else if (this.tinyDB.getBoolean("isImage")) {
            Glide.with((FragmentActivity) this).load(new File(this.tinyDB.getString("ImageString"))).into(this.mIvMainScreen);
        } else if (this.tinyDB.getBoolean("isCustomBg")) {
            this.mIvMainScreen.setImageResource(this.tinyDB.getInt("customBg"));
        } else {
            this.mIvMainScreen.setImageResource(0);
            this.mIvMainScreen.setBackgroundColor(this.tinyDB.getInt("bgColor"));
        }
        if (this.ivAutoBg != null) {
            this.ivAutoBg.setAlpha(autoBgMode == AutoBackground.MODE_OFF ? 0.4f : 1.0f);
        }
    }

    private boolean checkPermissions() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            return true;
        }else{
            ArrayList arrayList = new ArrayList();
            String[] strArr = this.permissions;
            for (String str : strArr) {
                if (ContextCompat.checkSelfPermission(this, str) != 0) {
                    arrayList.add(str);
                }
            }
            if (arrayList.isEmpty()) {
                return true;
            }
            ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[arrayList.size()]), 100);
            return false;
        }

    }

    public void galleryIntent() {
        if (checkPermissions()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), 1);
        }
    }

    @Override

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr.length > 0 && iArr[0] == 0) {
            galleryIntent();
        }
    }

    @Override

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1 && i2 == -1 && intent != null) {
            String realPath = RealPathUtil.getRealPath(this, intent.getData());
            this.tinyDB.putBoolean("isImage", true);
            this.tinyDB.putBoolean("isCustomBg", false);
            this.tinyDB.putString("ImageString", String.valueOf(realPath));
            this.tinyDB.putInt("autoBgMode", AutoBackground.MODE_OFF);
            updateClock();
        }
    }
}
