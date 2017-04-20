package com.rowland.photomist.ui.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rowland.photomist.R;
import com.rowland.photomist.managers.BeepManager;
import com.rowland.photomist.ui.BaseToolBarActivity;
import com.rowland.photomist.ui.fragments.PhotoFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends BaseToolBarActivity {

    // Logging Identifier for class
    private final String LOG_TAG = PhotoActivity.class.getSimpleName();

    // ButterKnife injected views
    // View to display if permissions are denied
    @Bind(R.id.fragment_container)
    FrameLayout mPhotoFragmentView;
    // Button to retry permissions
    @Bind(R.id.permissions_button_view)
    Button mPermsButtonRetryView;
    // The View to display if permissions are denied
    @Bind(R.id.permissions_rationale_container)
    LinearLayout mPermsRationaleView;
    // Not needed, but we will use a transparent Toolbar to keep thinhs simple
    @Bind(R.id.transparent_toolbar)
    Toolbar mTransparentToolBar;

    // Media use
    private BeepManager mBeepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        // Inflate all views
        ButterKnife.bind(this);
        // Set the Toolbar
        setToolbar(mTransparentToolBar, false, false, 0);
        setToolbarTransparent(true);

        // Restoration from a previous state, just return
        if (savedInstanceState != null) {
            return;
        }
        // Check for App permissions
        else {
            checkPermissions();
        }

        mPermsButtonRetryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });
    }

    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                //
                if (report.areAllPermissionsGranted()) {
                    showPermissionRetry(false);
                    // Show the photo Fragment withSurfaceView
                    showPhotoFragment(null);
                } else {
                    showPermissionRetry(true);
                    for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                        showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                //
                showPermissionRationale(token);
            }
        }).onSameThread().check();
    }

    private void showPermissionRationale(PermissionToken token) {
        token.continuePermissionRequest();
        showPermissionRetry(true);
    }

    private void showPermissionDenied(String permissionName, boolean permanentlyDenied) {
        Toast.makeText(this, permissionName + " is needed to function properly", Toast.LENGTH_SHORT).show();
    }

    private void showPermissionRetry(boolean isShowRetry) {
        if (isShowRetry) {
            // Permissions given, toggle visibility off
            mPermsRationaleView.setVisibility(View.VISIBLE);
            mPhotoFragmentView.setVisibility(View.GONE);
        } else {
            // Permissions given, toggle visibility off
            mPermsRationaleView.setVisibility(View.GONE);
            mPhotoFragmentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialize the beep manager
        mBeepManager = new BeepManager(this);
    }

    // Called when the activity is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        if (mBeepManager != null) {
            mBeepManager.close();
            mBeepManager = null;
        }
    }

    // Called to destroy this fragment
    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    // Insert the Fragment
    private void showPhotoFragment(Bundle args) {
        // Create new photo fragment
        PhotoFragment photoFragment = PhotoFragment.newInstance(args);
        // Acquire the Fragment manger
        FragmentManager fm = getSupportFragmentManager();
        // Begin the transaction
        FragmentTransaction ft = fm.beginTransaction();
        // Prefer replace() over add() see <a>https://github.com/RowlandOti/PopularMovies/issues/1</a>
        ft.replace(R.id.fragment_container, photoFragment);
        // Known bug with commit(), see - <a>http://stackoverflow.com/a/10261438/1464571</a>
        ft.commitAllowingStateLoss();
    }
}
