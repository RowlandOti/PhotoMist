package com.rowland.photomist.ui.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rowland.photomist.R;
import com.rowland.photomist.ui.views.CameraPreviewSurfaceView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment {

    // A Camera object
    private Camera mCamera;
    // Total hardware cameras
    private int mNumberOfCameras;
    // The Camera in use
    private int mCameraCurrentlyLocked;
    // The first back facing camera
    private int defaultBackFacingCameraId;
    // The first front facing camera
    private int defaultFrontFacingCameraId;

    // The surface view
    @Bind(R.id.camera_preview_surfaceview)
    CameraPreviewSurfaceView mPreviewSurface;

    public PhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param Bundle args.
     * @return A new instance of fragment PhotoFragment.
     */
    public static PhotoFragment newInstance(Bundle args) {
        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        // As we iterate, just pick the first result of each type
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            // Acquire the back facing camera
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultBackFacingCameraId = i;
            }
            // Acquire the front facing camera
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                defaultFrontFacingCameraId = i;
            }
        }
    }

    // Called to instantiate the fragment's view hierarchy
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        // Inflate all views
        ButterKnife.bind(this, rootView);
        // Return the view for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ToDo: In future include feature for switching cameras
        mCameraCurrentlyLocked = defaultBackFacingCameraId;
        // Open the default back facing camera.
        openCameraNew();
        // Set the camera to use
        mPreviewSurface.setCamera(mCamera);
        /*if (mPreviewSurface.getParent() == null) {
            mPreviewContainer.addView(mPreviewSurface);
        }*/
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        // The Camera object is a shared resource, release it
        if (mCamera != null) {
            mPreviewSurface.setCamera(null);
            mCamera.autoFocus(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            //mPreviewContainer.removeView(mPreviewSurface);
        }
    }

    public void openCameraNew() {
        mCamera = Camera.open();
    }

}
