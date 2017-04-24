package com.rowland.photomist.ui.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rowland.photomist.camera.CameraHandlerThread;
import com.rowland.photomist.utilities.CameraUtility;
import com.rowland.photomist.utilities.DeviceUtility;

import java.io.IOException;
import java.util.List;

/**
 * Created by Rowland on 4/19/2017.
 */

public class CameraPreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraPreviewSurfaceView.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private Camera.Size mPictureSize;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<Camera.Size> mSupportedPictureSizes;
    private int mOrientation = 0;
    private CameraHandlerThread previewCallback;

    public CameraPreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraPreviewSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraPreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        // Acquire the drawing surface
        mSurfaceView = new SurfaceView(context);
        mSurfaceView = this;
        // Acquire holder of our display surface
        mHolder = mSurfaceView.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder.addCallback(this);
        // Deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = CameraUtility.getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            mPictureSize = CameraUtility.getOptimalPreviewSize(mSupportedPictureSizes, width, height);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Try to set the display surface holder on the camera
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.d(LOG_TAG, "Error setting camera preview: " + exception.getMessage());
        }
        Log.d(LOG_TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // Now that the size is known, set up the camera parameters and begin the preview.
        try {
            // Acquire camera parameters
            Camera.Parameters parameters = mCamera.getParameters();
            // Take care of events such as rotation
            fixOrientation(parameters);
            // Set some camera parameters
            if(CameraUtility.isAutoFocusSupported(mCamera)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if(CameraUtility.isFlashSupported(mCamera)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPictureSize.width, mPictureSize.height);

            requestLayout();
            if (mCamera != null) {
                // Set the parameters on the camera and start the preview
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            }
        } catch (Exception exception) {
            Log.d(LOG_TAG, "Error starting camera preview: " + exception.getMessage());
        }
        Log.d(LOG_TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();

            mHolder.removeCallback(this);
            destroyDrawingCache();

            mCamera = null;
        }
        Log.d(LOG_TAG, "surfaceDestroyed");
    }

    // A method to take care of the events of mOrientation changes on deveice rotation
    private void fixOrientation(Camera.Parameters parameters) {
        // Acquire a Display object
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        // The preview is rotated on devices, so we have to straighten it.
        if (display.getRotation() == Surface.ROTATION_0) {
            mOrientation = 90;
            mCamera.setDisplayOrientation(mOrientation);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            mOrientation = 0;
            mCamera.setDisplayOrientation(mOrientation);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            mOrientation = 0;
            mCamera.setDisplayOrientation(mOrientation);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            mOrientation = 180;
            mCamera.setDisplayOrientation(mOrientation);
        }

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
    }

    // A method that will be used to set the first camera to be used
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
            toggleCameraFlash();
            requestLayout();
        }
    }

    public void toggleCameraFlash() {
        // Check for flash in Camera
        if (CameraUtility.isFlashSupported(mCamera)) {
            // Acquire Camera parameters
            Camera.Parameters parameters = mCamera.getParameters();
            // What flash mode are we in
            String flashMode = mCamera.getParameters().getFlashMode();
            if (flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                // Enable Flash
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else if (flashMode == Camera.Parameters.FLASH_MODE_ON) {
                // Disable Flash
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                // Auto Flash
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            // Apply the changes
            mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocusCallback(Camera.AutoFocusCallback autoFocusCallback) {
        mCamera.autoFocus(autoFocusCallback);
    }
}
