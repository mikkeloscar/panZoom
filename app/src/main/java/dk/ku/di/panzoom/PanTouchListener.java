package dk.ku.di.panzoom;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * @auther Mikkel Oscar Lyderik
 */
public class PanTouchListener implements View.OnTouchListener{

    PointF start = new PointF();
    PointF pos = new PointF();

    private PanZoomImageView imgView;
    private ZoomAccListener zoomListener;

    public PanTouchListener(View view, ZoomAccListener zoomListener) {
        super();
        this.imgView = (PanZoomImageView) view;
        this.zoomListener = zoomListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        pos.set(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // disable zooming
                zoomListener.disable();
                start.set(pos);
                break;
            case MotionEvent.ACTION_UP:
                // enable zooming
                zoomListener.enable();
                break;
            case MotionEvent.ACTION_MOVE:
                    PointF delta = new PointF(pos.x - start.x, pos.y - start.y);

                    imgView.pan(delta.x, delta.y);

                    // update start
                    start.set(pos);
                break;
        }

        return true;
    }
}
