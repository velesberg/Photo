package eti.inTouch.data;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.hide();

        // проверка на разрешения для камеры
        isCamerasAvaiable();
        // проверка на разрешения на запись
        isStorageAvaiable();


    }

    // кнопка Далее
    public void onButtonNextClick(View view) {
        Intent intent = new Intent(MainActivity.this, photoPoint.class);
        startActivity(intent);
    }

    public boolean isCamerasAvaiable(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 102);

            return false;
        }

        return true;
    }

    public boolean isStorageAvaiable(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 103);

            return false;
        }

        return true;
    }

    // кнопка забыли пароь
    public void onButtonRememberClick(View view) {
        Intent intent = new Intent(MainActivity.this, photoViewer.class);
        MainActivity.this.startActivity(intent);
    }
}

