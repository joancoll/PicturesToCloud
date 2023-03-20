package cat.dam.andy.picturestocloud;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionManager extends AppCompatActivity {
    public ActivityResultLauncher activityResultLauncher;

    // Constructor
    public PermissionManager(Context context, ArrayList<PermissionData> permissionsRequired) {
        // Members
        initPermissionLauncher(context, permissionsRequired);
    }

    private void initPermissionLauncher(Context context, ArrayList<PermissionData> permissionsRequired) {
        //Inicialitza el launcher per demanar permisos
        activityResultLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Check if all permissions are granted
                    if (permissions.containsValue(false)) {
                        // Check every permission
                        int position = 0;
                        for (String permission : permissions.keySet()) {
                            for (int i = 0; i < permissionsRequired.size(); i++) {
                                if (permissionsRequired.get(i).getPermission().equals(permission)) {
                                    position = i;
                                    break;
                                }
                            }
                            if (Boolean.TRUE.equals(permissions.get(permission))) {
                                // Permission granted
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.permissionGranted)
                                        .setMessage(permissionsRequired.get(position).getPermissionGrantedMessage())
                                        .setCancelable(true)
                                        .setPositiveButton("Ok", (dialogInterface, c) -> dialogInterface.dismiss())
                                        .create()
                                        .show();
                            } else
                            if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, permission)) {
                                // Permission denied
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.permissionDenied)
                                        .setMessage(permissionsRequired.get(position).getpermissionExplanation())
                                        .setCancelable(true)
                                        .setPositiveButton("Ok", (dialogInterface, c) -> dialogInterface.dismiss())
                                        .create()
                                        .show();
                            } else {
                                // Permission denied permanently
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.permissionPermDenied)
                                        .setMessage( permissionsRequired.get(position).getpermissionPermanentDeniedMessage())
                                        .setCancelable(true)
                                        .setPositiveButton("Ok", (dialogInterface, c) -> {
                                            //*************************************************
                                            // if user denied permanently the permissions,
                                            //  he should go to settings to granted the permissions
                                            //*************************************************
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
                                        })
                                        .setNegativeButton("Cancel", (dialogInterface, c) -> dialogInterface.dismiss())
                                        .create()
                                        .show();
                            }


                        }
                    }
                }
        );
    }


    public boolean hasAllNeededPermissions(Context context, ArrayList<PermissionData> permissions) {
        //comprova que tingui els permisos necessaris
        for (PermissionData permission : permissions) {
            if (!hasPermission(context, permission.getPermission())) {
                return false;//retorna false si no te tots els permisos concedits
            }
        }
        return true;//retorna true si tots els permisos estan concedits
    }

    public ArrayList<PermissionData> getRejectedPermissions(Context context, ArrayList<PermissionData> permissions) {
        //retorna només els permisos rebutjats
        ArrayList<PermissionData> permissionsRejected = new ArrayList<>();
        for (PermissionData permission : permissions) {
            if (!hasPermission(context, permission.getPermission())) {
                permissionsRejected.add(permission);
            }
        }
        return permissionsRejected;
    }

    private boolean hasPermission(Context context, String permission) {
        return(ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        //return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED; //no funciona sense context en classe/fragment
    }

    public void askForPermissions(Context context, ArrayList<PermissionData> permissions) {
        //Demana tots els permisos necessaris
        String[] permissionsNamesRejected = new String[permissions.size()];
        for (int i=0; i<permissions.size(); i++) {
            permissionsNamesRejected[i]=permissions.get(i).getPermission();
        }
        activityResultLauncher.launch(permissionsNamesRejected);
    }
    public void askOnePermission(Context context, PermissionData permission) {
        //Demana permís necessari
        activityResultLauncher.launch(new String[]{permission.getPermission()});
    }
}


