package dk.ku.di.panzoom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;


public class MainActivity extends ActionBarActivity {

    private SensorManager sensorManager;
    private ZoomAccListener zoomListener;
    private PanTouchListener panListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PanZoomImageView image = (PanZoomImageView) findViewById(R.id.panZoomImage);

        ToggleButton zoomToggle = (ToggleButton) findViewById(R.id.toggleZoom);

        // handle accelerometer
        zoomListener = new ZoomAccListener(image, zoomToggle);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(zoomListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        // handle touch
        panListener = new PanTouchListener(image, zoomListener);
        image.setOnTouchListener(panListener);
    }

    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            // enable ZOOM mode
            zoomListener.enable();

        } else {
            // disable ZOOM mode
            zoomListener.disable();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
