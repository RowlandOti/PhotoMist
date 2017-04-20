package com.rowland.photomist.ui.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

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
    // The default camera in use
    private int mCameraCurrentlyLocked;
    // The first back facing camera
    private int defaultBackFacingCameraId;
    // The first front facing camera
    private int defaultFrontFacingCameraId;

    // The surface view
    @Bind(R.id.camera_preview_surfaceview)
    CameraPreviewSurfaceView mPreviewSurface;
    // ImageView to take photo
    @Bind(R.id.photo_settings_group)
    ImageView mTakePhotoIImageView;

    // RadioGroup for settings
    @Bind(R.id.photo_settings_group)
    RadioGroup mSettingsRadioGroup;

    private void toggleCamera(int cameraId) {
        // Set chosen camera
        mCameraCurrentlyLocked = cameraId;
        mPreviewSurface.surfaceDestroyed(mPreviewSurface.getHolder());

        RelativeLayout previewParent = (RelativeLayout) getActivity().findViewById(R.id.preview_container);
        previewParent.removeView(mPreviewSurface);

        mPreviewSurface = new CameraPreviewSurfaceView(getActivity());
        mPreviewSurface.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        previewParent.addView(mPreviewSurface, 0);

        // Open the default back facing camera.
        mCamera = Camera.open(mCameraCurrentlyLocked);
        // Set the camera to use
        mPreviewSurface.setCamera(mCamera);
    }

    public PhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSettingsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.capture_mode_camera_flash:
                        mPreviewSurface.toggleCameraFlash();
                        break;
                    case R.id.capture_mode_back_camera:
                        toggleCamera(defaultBackFacingCameraId);
                        break;
                    case R.id.capture_mode_front_camera:
                        toggleCamera(defaultFrontFacingCameraId);

                }
            }
        });

        mTakePhotoIImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // ToDo: In future include feature for switching cameras
        mCameraCurrentlyLocked = defaultBackFacingCameraId;
        // Open the default back facing camera.
        mCamera = Camera.open(mCameraCurrentlyLocked);
        // Set the camera to use
        mPreviewSurface.setCamera(mCamera);
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
        }
    }
}
