package dk.ku.di.panzoom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * @auther Mikkel Oscar Lyderik
 */
public class PanZoomImageView extends ImageView {
    // Matrix used for transforming the image
    public Matrix matrix = new Matrix();

    public enum MODE {
        PAN, ZOOM
    }

    private MODE mode = MODE.PAN;

    public enum ZOOM {
        NONE, IN, OUT
    }

    public float minScale;
    public float maxScale = 2f; // limit to a scale of x2

    float[] m = new float[9];

    public PanZoomImageView(Context context, AttributeSet attr) {
        super(context, attr);
        matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // calculate min scale, we don't wanna make the picture smaller than the screen width
        minScale = (float) getWidth() / getDrawable().getIntrinsicWidth();

        // crop and align image in center
        centerCrop();
    }

    // based on: https://gist.github.com/lesleh/7080955
    // used to centerCrop the image on load
    private void centerCrop() {
        float originalImageWidth = (float)getDrawable().getIntrinsicWidth();
        float originalImageHeight = (float)getDrawable().getIntrinsicHeight();

        float usedScaleFactor = 1;

        if((originalImageWidth > getWidth()) || (originalImageHeight > getHeight())) {
            // If frame is bigger than image
            // => Crop it, keep aspect ratio and position it at the bottom and center horizontally

            float fitHorizontallyScaleFactor = getWidth()/originalImageWidth;
            float fitVerticallyScaleFactor = getHeight()/originalImageHeight;

            usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
        }

        float newImageWidth = originalImageWidth * usedScaleFactor;
        float newImageHeight = originalImageHeight * usedScaleFactor;

        matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0); // Replaces the old matrix completely
        matrix.postTranslate((getWidth() - newImageWidth) /2, getHeight() - newImageHeight);
        setImageMatrix(matrix);
    }

    /**
     * Zoom image with the given scale
     *
     * @param scale amount to scale the image in the view
     */
    public void zoom(float scale) {
        float width, height, margin;

        getImageMatrix().getValues(m);
        float scaleX = m[Matrix.MSCALE_X];

        float newScale = scale * scaleX;

        if (newScale > maxScale) {
            scale = maxScale / scaleX;
        } else if (newScale < minScale) {
            scale = minScale / scaleX;
        }

        matrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);

        matrix.getValues(m);
        PointF trans = new PointF(m[Matrix.MTRANS_X], m[Matrix.MTRANS_Y]);

        width = m[Matrix.MSCALE_X] * getDrawable().getIntrinsicWidth();

        // if the image is scaled down to screen width, align to center
        // landscape image
        if (trans.x > 0) {
            matrix.postTranslate(-trans.x, 0);
        } else if (trans.x + width < getWidth()) {
            matrix.postTranslate(getWidth() - (trans.x + width), 0);
        }

        matrix.getValues(m);

        width = m[Matrix.MSCALE_X] * getDrawable().getIntrinsicWidth();
        height = m[Matrix.MSCALE_Y] * getDrawable().getIntrinsicHeight();

        if (width == getWidth()) {
            margin = (getHeight() - height) / 2;
            matrix.postTranslate(0, margin - trans.y);
        }

        setImageMatrix(matrix);
        invalidate();
    }

    /**
     * Pan image
     *
     * @param dx pan distance in the x axis
     * @param dy pan distance in the y axis
     */
    public void pan(float dx, float dy) {
        getImageMatrix().getValues(m);
        PointF scale = new PointF(m[Matrix.MSCALE_X], m[Matrix.MSCALE_Y]);
        PointF trans = new PointF(m[Matrix.MTRANS_X], m[Matrix.MTRANS_Y]);

        float width = scale.x * getDrawable().getIntrinsicWidth();
        float height = scale.y * getDrawable().getIntrinsicHeight();

        // if the image is bigger than the screen then the panning must stop if an
        // image edge hits the corresponding screen edge.
        // We don't wanna pan the image of the screen

        // if the image is smaller than the screen either in width or height then we
        // wont the image to stay centered on the screen and only pan in the
        // direction that is bigger than the screen

        // handle alignment for the vertical axis
        if (height <= getHeight()) {
            float margin = (getHeight() - height) / 2;
            dy = margin - trans.y;
        } else { // make sure it isn't panned past the top or bottom
            if (trans.y + dy > 0) {
                dy = - trans.y;
            } else if (trans.y + dy + height < getHeight()) {
                dy = getHeight() - trans.y - height;
            }
        }

        // handle alignment for the horizontal axis
        if (width <= getWidth()) {
            float margin = (getWidth() - width) / 2;
            dx = margin - trans.x;
        } else { // stop panning when the the edge of the image hits the edge of the screen
            if (trans.x + dx > 0) {
                dx = -trans.x;
            } else if (trans.x + dx + width < getWidth()) {
                dx = getWidth() - trans.x - width;
            }
        }

        matrix.postTranslate(dx, dy);

        setImageMatrix(matrix);
        invalidate();
    }

    /**
     * set the current mode (PAN/ZOOM)
     *
     * @param mode mode is either PAN or ZOOM
     */
    public void setMode(MODE mode) {
        this.mode = mode;
    }
}
