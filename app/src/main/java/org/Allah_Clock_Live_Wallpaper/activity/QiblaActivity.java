package org.Allah_Clock_Live_Wallpaper.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.ads.BannerAdController;
import org.Allah_Clock_Live_Wallpaper.utils.QiblaUtil;
import org.Allah_Clock_Live_Wallpaper.utils.TinyDB;
import org.Allah_Clock_Live_Wallpaper.utils.UiCompat;
import org.Allah_Clock_Live_Wallpaper.viewUtils.CompassView;

/**
 * Qibla compass.
 *
 * <p>Deliberately needs <b>no location permission</b>: the user picks their city from a
 * bundled table, the bearing to the Kaaba is computed with the great-circle formula and the
 * rotation-vector sensor supplies the device heading. Works offline, on every device.</p>
 */
public class QiblaActivity extends AppCompatActivity implements SensorEventListener {

    private static final String PREF_CITY_INDEX = "qiblaCityIndex";
    /** Low-pass factor: higher = snappier, lower = steadier. */
    private static final float SMOOTHING = 0.18f;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private CompassView compass;
    private TextView txtCity;
    private TextView txtBearing;
    private TextView txtDistance;
    private TextView txtSensorMissing;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private TinyDB tinyDB;
    private BannerAdController banner;

    private int cityIndex;
    private float smoothedAzimuth = -1f;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_qibla);
        UiCompat.applyEdgeToEdge(this);

        this.tinyDB = new TinyDB(this);
        this.cityIndex = this.tinyDB.getInt(PREF_CITY_INDEX);
        if (this.cityIndex < 0 || this.cityIndex >= QiblaUtil.CITY_NAMES.length) {
            this.cityIndex = 0;
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> finish());
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_qibla);

        this.compass = findViewById(R.id.compass);
        this.txtCity = findViewById(R.id.txtCity);
        this.txtBearing = findViewById(R.id.txtBearing);
        this.txtDistance = findViewById(R.id.txtDistance);
        this.txtSensorMissing = findViewById(R.id.txtSensorMissing);

        this.txtCity.setOnClickListener(view -> showCityPicker());

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (this.sensorManager != null) {
            this.rotationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        if (this.rotationSensor == null) {
            this.txtSensorMissing.setVisibility(View.VISIBLE);
        }

        applyCity();

        this.banner = new BannerAdController(this, (RelativeLayout) findViewById(R.id.bannerAd));
        this.banner.load();
    }

    private void applyCity() {
        double lat = QiblaUtil.CITY_COORDS[this.cityIndex][0];
        double lon = QiblaUtil.CITY_COORDS[this.cityIndex][1];
        float bearing = QiblaUtil.bearingFromNorth(lat, lon);
        int distance = QiblaUtil.distanceKm(lat, lon);

        this.compass.setQiblaBearing(bearing);
        this.txtCity.setText(QiblaUtil.CITY_NAMES[this.cityIndex]);
        this.txtBearing.setText(getString(R.string.qibla_bearing, Math.round(bearing)));
        this.txtDistance.setText(getString(R.string.qibla_distance, distance));
    }

    private void showCityPicker() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.qibla_city)
                .setSingleChoiceItems(QiblaUtil.CITY_NAMES, this.cityIndex, (dialog, which) -> {
                    this.cityIndex = which;
                    this.tinyDB.putInt(PREF_CITY_INDEX, which);
                    applyCity();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.sensorManager != null && this.rotationSensor != null) {
            this.sensorManager.registerListener(this, this.rotationSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
        if (banner != null) {
            banner.resume();
        }
    }

    @Override
    protected void onPause() {
        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }
        if (banner != null) {
            banner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (banner != null) {
            banner.destroy();
            banner = null;
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
            return;
        }
        try {
            SensorManager.getRotationMatrixFromVector(this.rotationMatrix, event.values);
            SensorManager.getOrientation(this.rotationMatrix, this.orientation);
            float target = (float) ((Math.toDegrees(this.orientation[0]) + 360.0) % 360.0);
            if (this.smoothedAzimuth < 0f) {
                this.smoothedAzimuth = target;
            } else {
                float delta = ((target - this.smoothedAzimuth + 540f) % 360f) - 180f;
                this.smoothedAzimuth = (this.smoothedAzimuth + delta * SMOOTHING + 360f) % 360f;
            }
            this.compass.setAzimuth(this.smoothedAzimuth);
        } catch (Throwable t) {
            // A malformed sensor event must never take the screen down.
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(this, R.string.qibla_calibrate, Toast.LENGTH_SHORT).show();
        }
    }
}
