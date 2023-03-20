package cat.dam.andy.picturestocloud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Members

    static final int REQUEST_PICTURE_CAPTURE = 1;
    private static final String TAG = "PicturesToCloud";
    private ImageView iv_image;
    private Button btn_picture, btn_showGallery, btn_saveToCloud;
    private Uri uriImage;
    private FirebaseStorage firebaseStorage;
    private String idDevice;
    private FirebaseAuth mAuth;
    private PermissionManager permissionManager;
    private final ArrayList<PermissionData> permissionsRequired=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initPermissions();
        initListeners();
        initDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseStorage = null;
        mAuth = null;
    }


    private void initViews() {
        iv_image = findViewById(R.id.iv_image);
        btn_picture = findViewById(R.id.btn_picture);
        btn_showGallery = findViewById(R.id.btn_saveToGallery);
        btn_saveToCloud = findViewById(R.id.btn_saveToCloud);
        // If don't camera, disable the button for doing photos
        btn_picture.setEnabled(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));

    }

    private void initPermissions() {
        //TO DO: CONFIGURE ALL NECESSARY PERMISSIONS
        //BEGIN
        permissionsRequired.add(new PermissionData(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.writeExternalStoragePermissionNeeded),
                "",
                getString(R.string.writeExternalStoragePermissionThanks),
                getString(R.string.writeExternalStoragePermissionSettings)));

        //END
        //DON'T DELETE == call permission manager ==
        permissionManager= new PermissionManager(this, permissionsRequired);

    }

    private void initListeners() {
        btn_showGallery.setOnClickListener(v -> {
            if (!permissionManager.hasAllNeededPermissions(this, permissionsRequired))
            { //Si manquen permisos els demanem
                permissionManager.askForPermissions(this, permissionManager.getRejectedPermissions(this, permissionsRequired));
            } else {
                //Si ja tenim tots els permisos, obrim la galeria
                openGallery();
            }
        });
        btn_picture.setOnClickListener(v -> {
            if (!permissionManager.hasAllNeededPermissions(this, permissionsRequired))
            { //Si manquen permisos els demanem
                permissionManager.askForPermissions(this, permissionManager.getRejectedPermissions(this, permissionsRequired));
            } else {
                //Si ja tenim tots els permisos, fem la foto
                takePicture();
            }
        });
        btn_saveToCloud.setOnClickListener(v -> {
            if (!permissionManager.hasAllNeededPermissions(this, permissionsRequired))
            { //Si manquen permisos els demanem
                permissionManager.askForPermissions(this, permissionManager.getRejectedPermissions(this, permissionsRequired));
            } else {
                //Si ja tenim tots els permisos, desem al núvol
                saveToCloud();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void initDatabase() {
        idDevice = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseApp.initializeApp(MainActivity.this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // TODO:
        // Encourage to disable anonymous acces (only enable autorizated users) on permissions of Firebase/Storage
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        firebaseStorage = FirebaseStorage.getInstance();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                    }
                });
    }


    @SuppressLint("QueryPermissionsNeeded")
    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (intent.resolveActivity(getPackageManager()) != null) {
                activityResultLauncherGallery.launch(Intent.createChooser(intent, getString(R.string.select_picture)));
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.no_gallery_access), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            uriImage=null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, getString(R.string.picture_creation_error),
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, getString(R.string.picture_title));
                values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.picture_time) + " "+ System.currentTimeMillis());
                uriImage = FileProvider.getUriForFile(this,
                        this.getPackageName()+ ".provider", //(use your app signature + ".provider" )
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
                activityResultLauncherPhoto.launch(intent);
            } else {
                Toast.makeText(MainActivity.this,getString(R.string.picture_creation_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.camera_access_error),
                    Toast.LENGTH_SHORT).show();
        }
    }



    // Save image to cloud
    private void saveToCloud() {
        if (uriImage == null) {
            createToast(getResources().getString(R.string.no_image_selected));
        }
        else {
            final String cloudFilePath = idDevice + "_" + uriImage.getLastPathSegment();
            // TODO:
            // Encourage to disable anonymous acces (only enable autorizated users) on permissions of Firebase/Storage
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageRef = firebaseStorage.getReference();
            StorageReference uploadeRef = storageRef.child(cloudFilePath);
            uploadeRef.putFile(uriImage).
                    addOnFailureListener(exception ->
                            Log.e(TAG, getResources().getString(R.string.save_to_cloud_error)))
                    .addOnSuccessListener(taskSnapshot ->
                            Log.d(TAG, getResources().getString(R.string.save_to_cloud_done)));
                            createToast(getResources().getString(R.string.save_at_cloud_correct));
        }
    }


    private final ActivityResultLauncher<Intent> activityResultLauncherGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //here we will handle the result of our intent
                uriImage=null;
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //image picked
                    //get uri of image
                    Intent data = result.getData();
                    if (data != null) {
                        uriImage = data.getData();
                        System.out.println("galeria: "+uriImage);
                        iv_image.setImageURI(uriImage);
                    }
                } else {
                    //cancelled
                    Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private final ActivityResultLauncher<Intent> activityResultLauncherPhoto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //here we will handle the result of our intent
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
                    iv_image.setImageURI(uriImage); //Amb paràmetre EXIF podem canviar orientació (per defecte horiz en versions android antigues)
                    refreshGallery();//refresca gallery per veure nou fitxer
                        /* Intent data = result.getData(); //si volguessim només la miniatura
                        uriImage = data.getData();
                        iv_imatge.setImageURI(uriImage);*/
                } else {
                    //cancelled
                    Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                }
            }
    );



    private File createImageFile() throws IOException {
        boolean wasSuccessful; //just for testing mkdirs
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        // File storageDir = getFilesDir();//no es veurà a la galeria
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES+File.separator+this.getPackageName());//No es veurà a la galeria
        File storageDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+File.separator+this.getPackageName());
        //NOTE: MANAGE_EXTERNAL_STORAGE is a special permission only allowed for few apps like Antivirus, file manager, etc. You have to justify the reason while publishing the app to PlayStore.
        if (!storageDir.exists()) {
            wasSuccessful =storageDir.mkdir();
        }
        else {
            wasSuccessful =storageDir.mkdirs();
        }
        if (wasSuccessful) {
            System.out.println("storageDir: " + storageDir);
        } else {
            System.out.println("storageDir: " + storageDir + " was not created");
        }
        // Save a file: path for use with ACTION_VIEW intents
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        uriImage = Uri.fromFile(image);
        System.out.println("file: "+uriImage);
        return image;
    }


    private void refreshGallery() {
        //Cal refrescar per poder veure la foto creada a la galeria
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uriImage);
        this.sendBroadcast(mediaScanIntent);
    }

    private void createToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
