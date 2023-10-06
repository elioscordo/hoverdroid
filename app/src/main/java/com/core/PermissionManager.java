package com.core;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager implements TaskObserved{
    public static final String EVENT_PERMISSION_GRANTED = "PERMISSION_GRANTED";
    public static final String EVENT_PERMISSION_DENIED =  "PERMISSION_DENIED";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int ACCESS_FINE_LOCATION_CODE = 3;
    public TaskObserver observer;

    public  boolean checkCameraPermission(Activity activity) {
        int cameraPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    public  boolean checkStoragePermission(Activity activity) {
        int storagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    public  boolean checkFineLocationPermission(Activity activity) {
        int storagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }


    public  void requestFineLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
    }

    public  void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }


    public  void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
    }

    // Handle permission request results in your Activity
    public  void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    observer.onResults(EVENT_PERMISSION_GRANTED, CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    observer.onResults(EVENT_PERMISSION_DENIED, CAMERA_PERMISSION_REQUEST_CODE);
                }
                break;

            case STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    observer.onResults(EVENT_PERMISSION_GRANTED, STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    observer.onResults(EVENT_PERMISSION_DENIED, STORAGE_PERMISSION_REQUEST_CODE);
                }
                break;
            case ACCESS_FINE_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    observer.onResults(EVENT_PERMISSION_GRANTED, ACCESS_FINE_LOCATION_CODE);
                } else {
                    observer.onResults(EVENT_PERMISSION_DENIED, ACCESS_FINE_LOCATION_CODE);
                }
                break;
        }
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }
}
