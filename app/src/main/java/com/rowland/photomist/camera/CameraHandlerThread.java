package com.rowland.photomist.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.hardware.Camera;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.rowland.photomist.ui.activities.PhotoActivity;
import com.rowland.photomist.ui.fragments.PhotoFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rowland on 4/20/2017.
 */

public class CameraHandlerThread extends HandlerThread implements Camera.AutoFocusCallback {

    // The threading identifier
    private static String THREAD_TAG = "CameraHandlerThread";
    // Class logging Identifier
    private final String LOG_TAG = CameraHandlerThread.class.getSimpleName();
    // Actions thread will handle
    private static final int TAKE_PHOTO = 0;
    private static final int PREVIEW_PHOTO = 1;

    // Soft reference
    private WeakReference<PhotoFragment> mWeakReferenceCameraPreviewFragment = null;
    // The thread handler
    private Handler mHandler = null;

    public CameraHandlerThread(PhotoFragment photoFragment) {
        super(THREAD_TAG);
        // This is a call to begin the thread
        start();
        mWeakReferenceCameraPreviewFragment = new WeakReference<>(photoFragment);
        mWeakReferenceCameraPreviewFragment.get().getPreviewSurface().setAutoFocusCallback(this);
        mHandler = new Handler(getLooper());
    }

    public void initializePhotoTaking() {
        // Where all the magic happens
        mHandler = new Handler(getLooper(), new Handler.Callback() {
            //
            @Override
            public boolean handleMessage(final Message msg) {
                final Camera camera = (Camera) msg.obj;
                if (msg.what == TAKE_PHOTO) {
                    // Add to ThreadPool
                    CameraThreadPool.post(new Runnable() {

                        @Override
                        public void run() {
                            camera.takePicture(null, null, new Camera.PictureCallback() {

                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotoMist");
                                    if (!mediaStorageDir.exists()) {

                                        if (!mediaStorageDir.mkdirs()) {

                                        }
                                    }
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                    File pictureFile = new File(mediaStorageDir.getPath() + File.separator +
                                            "I" + timeStamp + ".jpg");
//

                                    try {
                                        FileOutputStream fos = new FileOutputStream(pictureFile);
                                        fos.write(data);
                                        fos.close();

                                    } catch (IOException e) {

                                    }
                                }
                            });
                        }

                    });
                }
                return true;
            }

        });
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // Trigger callback
        ((PhotoActivity) mWeakReferenceCameraPreviewFragment.get().getActivity()).onPhotoTakeComplete();

        //
        if (mWeakReferenceCameraPreviewFragment.get() != null) {
            // Start our thread
            mHandler.obtainMessage(TAKE_PHOTO, camera).sendToTarget();
        }

        Log.d(LOG_TAG, "onAutoFocus Called");
    }
}
