package med.kamili.rachid.practiceapp.managers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class PermissionManager {

    public static final String TAG = PermissionManager.class.getSimpleName() + "_TAG";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10;
    Context context;
    IPermissionManager listener;

    public PermissionManager(Context context) {
        this.context = context;
        this.listener = (IPermissionManager) context;
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: Permission is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.CAMERA)) {
                Log.d(TAG, "onCreate: Show an explanation to the user");
                getExplanationDialog().show();
            } else {
                requestPermission();
            }
        } else {
            listener.onPermissionResult(true);
            Log.d(TAG, "onCreate: Permission has already been granted");
        }
    }

    private void requestPermission() {
        Log.d(TAG, "onCreate: No explanation needed; request the permission");
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);
    }

    private AlertDialog getExplanationDialog() {

        return new AlertDialog.Builder(context)
                .setTitle("Need the permission")
                .setMessage("Can you please allow this permission? I need it.")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(context,
                                "Features disabled",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        requestPermission();
                    }
                }).create();
    }

    public void checkResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listener.onPermissionResult(true);
                } else {
                    listener.onPermissionResult(false);
                }
                return;
            }
        }
    }

    public interface IPermissionManager {
        void onPermissionResult(boolean isGranted);
    }
}
