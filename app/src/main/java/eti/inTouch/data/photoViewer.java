package eti.inTouch.data;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class photoViewer extends AppCompatActivity {

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);


       // final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.buttonReplace);

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "eti.shara");
        File mediaStorageDir = new File( Sams.getAppContext().getExternalFilesDir(null), "shara");

        try{

            TouchImageView photo = (TouchImageView) findViewById(R.id.item_Photo);

            Bitmap bmImg = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator + "1.jpg");
            if (bmImg != null) {
                assert photo != null;
                photo.setImageBitmap(bmImg);
            }
        }
        catch (Exception e){
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
