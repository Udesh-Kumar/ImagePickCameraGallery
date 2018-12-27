package com.example.cc.imagepickthroughcam;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //    ImageView viewImage;
//    Button b;
//    private static final int GALLERY = 1, CAMERA = 2;

    // In Marshmello version need exlicitly ask for permission
    public static int REQUEST_CODE = 5;
    private static final int CAMERA_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    ImageView imageView;
    Button selectImage;
    Button apihit;
    String imagepath = "";
    String pathstore = "";
    String path = "";

    String userChoosenTask = "";
    Bitmap bitmap;

    private String getImageUrl = "";

    Uri imageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image);
        selectImage = (Button) findViewById(R.id.select_image);
        apihit = (Button) findViewById(R.id.hit_api);


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSroragePermissions();


            }
        });

        apihit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (imagepath.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Select your Image", Toast.LENGTH_SHORT).show();
                } else {
                    Api api = ApiClient.apiclient().create(Api.class);

                    RequestBody dcId = RequestBody.create(MediaType.parse("text/plain"), "1");

                    File file = new File(imagepath);                                                // image path yaani hamaari image padi h
                    RequestBody docterimg = RequestBody.create(MediaType.parse("image/*"), file);     //imagepath ko Request body me convert kiya
                    final MultipartBody.Part image = MultipartBody.Part.createFormData("documentImage", file.getName(), docterimg);  // Requestbody ko Multipart me


                    retrofit2.Call<ModelClass> call = api.postImage(image, dcId);

                    call.enqueue(new Callback<ModelClass>() {              //call.enque krte sameye Dekhna he kon si modal class me se utha rha he
                        @Override
                        public void onResponse(retrofit2.Call<ModelClass> call, Response<ModelClass> response) {
                            if (response.body() != null) {

                                if (response.body().getSuccess().equalsIgnoreCase("1")) {                //get success me aane se phele
                                    Toast.makeText(MainActivity.this, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();


                                } else {
                                    Toast.makeText(MainActivity.this, response.body().getMessage() + "", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Something wents wrong", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(retrofit2.Call<ModelClass> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "You have must select a photo", Toast.LENGTH_SHORT).show();

                        }
                    });


                }

            }
        });
    }


    private void selectGallery() {

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";

                    galleryIntent();


                } else if (items[which].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";

                    cameraIntent();


                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void galleryIntent() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), RESULT_LOAD_IMAGE);
    }


    private void cameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);


    }

    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());


            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        imageUri = getImageUri(MainActivity.this, bitmap);
        imagepath = getRealPathFromUri(imageUri);
        // pathstore.setPath(imagepath, MainActivity.this);
        imageView.setImageBitmap(bitmap);
    }

    private void onCaptureImageResult(Intent data) {

        try {
            bitmap = (Bitmap) data.getExtras().get("data");
            Log.e("EditProfile>>>", "image bitmap>>>>" + bitmap);
            imageUri = getImageUri(MainActivity.this, bitmap);
            imagepath = getRealPathFromUri(imageUri);
            // pathstore.setPath(imagepath, MainActivity.this);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Uri getImageUri(MainActivity youractivity, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        path = MediaStore.Images.Media.insertImage(youractivity.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromUri(Uri tempUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = this.getContentResolver().query(tempUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String ss = cursor.getString(column_index);
            Toast.makeText(this, "ss", Toast.LENGTH_SHORT).show();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE)
                onSelectFromGalleryResult(data);
            else if (requestCode == CAMERA_REQUEST)
                onCaptureImageResult(data);
        }
    }


    public void getSroragePermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE + Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {

            selectGallery();

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST:


                boolean camera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean gallery = grantResults[1] == PackageManager.PERMISSION_GRANTED;


                if (grantResults.length > 0 && camera && gallery) {
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    selectGallery();

                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Permissions");
                    builder.setMessage("Storage Permissions are Required");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //send to settings
                            Toast.makeText(MainActivity.this, "Go to Settings to Grant the Storage Permissions", Toast.LENGTH_LONG).show();
                            boolean sentToSettings = true;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, RESULT_LOAD_IMAGE + CAMERA_REQUEST);
                        }
                    })
                            .create()
                            .show();

                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }


}

