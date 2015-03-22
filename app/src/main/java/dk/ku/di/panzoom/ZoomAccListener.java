package dk.ku.di.panzoom;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.ToggleButton;

/**
 * @auther Mikkel Oscar Lyderik
 */
public class ZoomAccListener implements SensorEventListener {

    private float y;

    private float defaultY = 0;
    // Zoom is detected on changes in the y axis of the accelerometer. DIVERSION defines how much
    // the current y value must differ from defaultY before the zooming movement is detected.
    private final float IN_DIVERSION = 1.8f;
    private final float OUT_DIVERSION = 1.5f;

    private final float SCALE = 1.1f;
    private final int INTERVAL = 50; // number of milliseconds a position must be held to take effect.
    private long time = 0;

    private PanZoomImageView.ZOOM newDir;
    private PanZoomImageView.ZOOM currDir = PanZoomImageView.ZOOM.NONE;

    private PanZoomImageView imgView;
    private ToggleButton btn;

    private boolean active = false;

    public ZoomAccListener(View view, View toggleButton) {
        super();
        this.imgView = (PanZoomImageView) view;
        this.btn = (ToggleButton) toggleButton;
    }

    public void enable() {
        this.active = true;
        btn.setChecked(true);

        // record default Y state
        defaultY = y;
    }

    public void disable() {
        this.active = false;
        btn.setChecked(false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // record the Y value all the time so we can set it as default when zooming is enabled
        y = event.values[1];


        if (active) {
            float[] m = new float[9];
            imgView.getImageMatrix().getValues(m);
            PointF imgScale = new PointF(m[Matrix.MSCALE_X], m[Matrix.MSCALE_Y]);

            // find direction
            if (y < (defaultY-IN_DIVERSION)) { // ZOOM IN
                newDir = PanZoomImageView.ZOOM.IN;
            } else if (y > (defaultY + OUT_DIVERSION)) {
                newDir = PanZoomImageView.ZOOM.OUT;
            } else {
                newDir = PanZoomImageView.ZOOM.NONE;
            }

            if (currDir == newDir && currDir != PanZoomImageView.ZOOM.NONE) {
                if (System.currentTimeMillis() - time >= INTERVAL) { // ZOOM

                    float scale = 1f;

                    if (currDir == PanZoomImageView.ZOOM.IN) {
                        scale = SCALE;
                    } else {
                        scale = 1 / SCALE;
                    }

                    // zoom
                    imgView.zoom(scale);

                    // start new time interval
                    time = System.currentTimeMillis();
                }
            } else {
                currDir = newDir;
                time = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
