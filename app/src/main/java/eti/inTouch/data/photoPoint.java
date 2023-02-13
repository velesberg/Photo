package eti.inTouch.data;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class photoPoint extends AppCompatActivity {

    private Uri outputFileUri;
    private File file;
    private String magic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_point);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setTitle(getString(R.string.photo_store));
        actionBar.setSubtitle(getString(R.string.take_photo_store));

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        boolean result = false;

        if (file == null)
            return;

        Intent intent = new Intent(photoPoint.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void startPhoto(View view){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        PackageManager packageManager = photoPoint.this.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(intent, 0);
        intent.setPackage(listCam.get(0).activityInfo.packageName);

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "eti.shara");
        File mediaStorageDir = new File( Sams.getAppContext().getExternalFilesDir(null), "shara");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return;
            }
        }
        file =  new File(mediaStorageDir.getPath() + File.separator + "1.jpg");


        if (Build.VERSION.SDK_INT >= 24)
            outputFileUri = FileProvider.getUriForFile(photoPoint.this, "com.eti.fileprovider", file);
        else
            outputFileUri = Uri.fromFile(file);
        //

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(photoPoint.this, MainActivity.class);
        startActivity(intent);
    }
}
