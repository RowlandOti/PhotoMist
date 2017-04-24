package com.rowland.photomist.camera;

import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

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

    // Actions thread will handle
    private static final int TAKE_PHOTO = 0;
    private static final int PREVIEW_PHOTO = 1;
    // The threading identifier
    private static String THREAD_TAG = "CameraHandlerThread";
    // Class logging Identifier
    private final String LOG_TAG = CameraHandlerThread.class.getSimpleName();
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
        Log.d(LOG_TAG, "TRIGGER TAKING PHOTO...");
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
                                    // Trigger callback
                                    ((PhotoActivity) mWeakReferenceCameraPreviewFragment.get().getActivity()).onPhotoTakeComplete();
                                    saveFile(data);
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
        // Start our thread
        mHandler.obtainMessage(TAKE_PHOTO, camera).sendToTarget();
        Log.d(LOG_TAG, "TAKING PHOTO...");
    }

    private void saveFile(byte[] data) {
        // Setup the storage directory
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotoMist");
        if (!storageDir.exists()) {
            // Create PhotoMist directory
            storageDir.mkdirs();
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "PH_" + timeStamp;
        File photoFile = new File(storageDir.getPath() + File.separator + imageFileName + ".jpg");
        // Save file in the SD Card
        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            fos.write(data);
            fos.close();
            Log.d(LOG_TAG, "SAVING PHOTO SUCCESSFUL..." + photoFile);

        } catch (IOException e) {
            Log.d(LOG_TAG, "SAVING PHOTO FAILED...: " + e.toString());
        }
    }
}
