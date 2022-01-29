package com.sales.numax.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sales.numax.R;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Dealer;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.ImageUtil;
import com.sales.numax.utility.Messages;
import com.sales.numax.utility.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.Permission;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.LENGTH_LONG;

public class UploadPhoto extends AppCompatActivity {
    View parentLayout;
    FloatingActionButton fabImageSelection;
    @BindView(R.id.imgPicture)
    ImageView imgPicture;

    @BindView(R.id.text_dealer_shop)
    TextView text_dealer_shop;

    @BindView(R.id.button_upload)
    TextView button_upload;

    String mOutputFilePath;

    String _FOLDER_PATH = "Nutra/Photo";
    public static final int CAMERA = 0;
    public static final int STORAGE = 125;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_photo);
        setSupportActionBar(Global.PrepareToolBar(this, true, "Upload Photo"));
        ButterKnife.bind(this);

        text_dealer_shop.setText((Global.SHOP_NAME));
        InitializeControls();
        CheckFolderPermission();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void CheckFolderPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            CreateFolders();
                        } else {
                            fabImageSelection.setVisibility(View.GONE);
                            new MaterialDialog.Builder(UploadPhoto.this)
                                    .positiveText("OK")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            onBackPressed();
                                        }
                                    })
                                    .title("Picture Upload")
                                    .content("Permission required to upload picture!!!").show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void InitializeControls() {
        parentLayout = findViewById(android.R.id.content);
        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        text_dealer_shop.setText(Global.SHOP_NAME);
        BottomNavigationView bottonNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.btnSave:
                    if (!Global.DEALER_KEY.isEmpty()) {
                        if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                            SaveRecord();
                        }
                    } else {
                        Messages.ShowToast(getApplicationContext(), "Donor not yet added");
                    }
                    break;
                case R.id.btnRemove:
                    if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                        if (Global.SELECTED_DEALER != null) {
                            if (Global.SELECTED_DEALER.getUrl().isEmpty() && Global.SELECTED_DEALER.getUrl() != "NA") {
                                RemoveImage(Global.SELECTED_DEALER.getUrl());
                            } else {
                                imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
                            }
                        } else {
                            imgPicture.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
                        }
                    }
                    break;
                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });
        if (Global.SELECTED_DEALER != null) {
            Dealer mDealer = Global.SELECTED_DEALER;
            text_dealer_shop.setText(mDealer.getShop());
            if (mDealer.getUrl() != null && !mDealer.getUrl().isEmpty() && !mDealer.getUrl().equals("NA")) {
                String mImageUrl = "";
                if (!TextUtils.isEmpty(mDealer.getUrl())) {
                    mImageUrl = mDealer.getUrl();
                }

                final int radius = 5;
                final int margin = 5;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                Picasso.with(getApplicationContext()).load(mImageUrl).placeholder(R.drawable.profile).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        String sImageUri = mDealer.getUrl();
                        Picasso.with(getApplicationContext()).load(sImageUri).placeholder(R.drawable.profile).transform(transformation).into(imgPicture);
                    }
                });
            }
        }
    }

    private void CreateFolders() {
        try {
            File exportDir = new File(getCacheDir(), _FOLDER_PATH);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), "There is a problem to create folder");
        }
    }
    public File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = getApplicationContext().getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), LENGTH_LONG).show();
            }
        }
        try {
            File file = getApplicationContext().getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error:" + e.getMessage(), LENGTH_LONG).show();
        }
        return new File("");
    }
    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(UploadPhoto.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

    private void RemoveImage(String url) {
        if (url != "NA") {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(getResources().getDrawable(R.drawable.logo));
            builder.setMessage("Remove Photo");
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                            final int which) {
                            final ProgressDialog dialogProgress = ProgressDialog.show(UploadPhoto.this, null, "Uploading photo..", true);
                            dialogProgress.show();

                            Uri uri = Uri.fromFile(new File(url));
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEALERS).child(Global.DEALER_KEY).child("url").setValue("NA");
                                    Messages.ShowToast(getApplicationContext(), "Photo removed successfully");
                                    dialogProgress.dismiss();
                                    Picasso.with(getApplicationContext()).load("NA").placeholder(R.drawable.noimage).networkPolicy(NetworkPolicy.OFFLINE).into(imgPicture, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getApplicationContext()).load("NA").placeholder(R.drawable.noimage).into(imgPicture);
                                        }
                                    });
                                }
                            });
                        }
                    });
            builder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                            final int which) {
                            dialog.dismiss();
                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void SaveRecord() {
        if (mOutputFilePath != null) {
            final ProgressDialog dialog = ProgressDialog.show(UploadPhoto.this, null, "Uploading photo..", true);
            dialog.show();

            Uri uri = Uri.fromFile(new File(mOutputFilePath));
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child(FirebaseTables.TBL_DEALERS).child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    if (taskSnapshot.getMetadata() != null) {

                        if (taskSnapshot.getMetadata().getReference() != null) {

                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (uri != null) {
                                        String photoStringLink = uri.toString();
                                        FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEALERS).child(Global.DEALER_KEY).child("url").setValue(photoStringLink)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (dialog != null) {
                                                            dialog.dismiss();
                                                        }
                                                        if (photoStringLink != null) {
                                                            Global.SELECTED_DEALER.setUrl(photoStringLink);
                                                            Intent intent = new Intent(UploadPhoto.this, DealerEntry.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "Unable to save picture", LENGTH_LONG).show();

                }
            });
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Gallery"};

        AlertDialog.Builder builderDialog = new AlertDialog.Builder(UploadPhoto.this);
        builderDialog.setTitle("Select Picture");
        builderDialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    try {

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(getBaseContext().getExternalCacheDir(), "temp.jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(intent, 1);

                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } else if (options[item].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Pictures")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), 3);
                }
            }
        });
        builderDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent iDashBoad = new Intent(UploadPhoto.this, DealerEntry.class);
        startActivity(iDashBoad);
        finish();
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            String mShop = text_dealer_shop.getText().toString().replaceAll(" ", "_");
            mOutputFilePath = ImageUtil.PrepareImage(getApplicationContext(), requestCode, resultCode, data, RESULT_OK, imgPicture, _FOLDER_PATH, mShop);
            Messages.ShowToast(getApplicationContext(),mOutputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Messages.ShowToast(getApplicationContext(), e.getMessage());
        }
    }

}