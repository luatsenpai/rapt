package com.joiplay.joiplay.renpy;

import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.renpy.android.PythonSDLActivity;

public class PermissionActivity extends AppCompatActivity {

    private TextView progView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.permissionactivity_layout);
        progView = findViewById(R.id.progText);
        
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 12);
        } else {
            if (getIntent() != null && getIntent().getExtras() != null){
                Intent i = new Intent(PermissionActivity.this,PythonSDLActivity.class);
                i.putExtras(getIntent().getExtras());
                startActivity(i);
            } else {
                PermissionActivity.this.progView.setText(R.string.got_no_data);
            }
        }

    }

    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12 && ContextCompat.checkSelfPermission(this,"android.permission.WRITE_EXTERNAL_STORAGE") == 0){
            if (getIntent() != null && getIntent().getExtras() != null){
                Intent i = new Intent(PermissionActivity.this,PythonSDLActivity.class);
                i.putExtras(getIntent().getExtras());
                startActivity(i);
            } else {
                PermissionActivity.this.progView.setText(R.string.got_no_data);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 12);
        }
    }
}
