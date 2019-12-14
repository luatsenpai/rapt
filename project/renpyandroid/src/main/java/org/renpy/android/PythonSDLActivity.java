package org.renpy.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.libsdl.app.SDLActivity;
import org.renpy.iap.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PythonSDLActivity extends SDLActivity {

    /**
     * This exists so python code can access this activity.
     */
    public static PythonSDLActivity mActivity = null;

    /**
     * The layout that contains the SDL view. VideoPlayer uses this to add
     * its own view on on top of the SDL view.
     */
    public FrameLayout mFrameLayout;


    /**
     * A layout that contains mLayout. This is a 3x3 grid, with the layout
     * in the center. The idea is that if someone wants to show an ad, they
     * can stick it in one of the other cells..
     */
    public LinearLayout mVbox;

    public Integer xKeyCode  = KeyEvent.KEYCODE_X;
    public Integer yKeyCode  = KeyEvent.KEYCODE_Y;
    public RelativeLayout joystickLay;
    public JoystickButton joystickBtn;
    public ImageButton upBtn;
    public ImageButton downBtn;
    public ImageButton leftBtn;
    public ImageButton rightBtn;
    public ImageButton closeBtn;
    public ImageButton rotateBtn;
    public ImageButton keyboardBtn;
    public Button aBtn;
    public Button bBtn;
    public Button xBtn;
    public Button yBtn;
    public BaseInputConnection inputConnection;

    ResourceManager resourceManager;


    protected String[] getLibraries() {
        return new String[] {
            "png16",
            "SDL2",
            "SDL2_image",
            "SDL2_ttf",
            "SDL2_gfx",
            "SDL2_mixer",
            "python2.7",
            "pymodules",
            "main",
        };
    }


    // GUI code. /////////////////////////////////////////////////////////////


    public void addView(View view, int index) {
        mVbox.addView(view, index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.0));
    }

    public void removeView(View view) {
        mVbox.removeView(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setContentView(View view) {
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.addView(view);

        mVbox = new LinearLayout(this);
        mVbox.setOrientation(LinearLayout.VERTICAL);
        mVbox.addView(mFrameLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 1.0));

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        getLayoutInflater().inflate(R.layout.main_layout,frameLayout);
        mFrameLayout.addView(frameLayout);
        super.setContentView(mVbox);

        joystickLay = findViewById(R.id.joystickLay);
        joystickBtn = findViewById(R.id.joystickBtn);
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        closeBtn = findViewById(R.id.closeBtn);
        rotateBtn = findViewById(R.id.rotateBtn);
        keyboardBtn = findViewById(R.id.keyboardBtn);
        aBtn = findViewById(R.id.aBtn);
        bBtn = findViewById(R.id.bBtn);
        xBtn = findViewById(R.id.xBtn);
        yBtn = findViewById(R.id.yBtn);
        joystickLay.setVisibility(View.INVISIBLE);
        xKeyCode = getIntent().getIntExtra("xKeyCode",xKeyCode);
        yKeyCode = getIntent().getIntExtra("yKeyCode",yKeyCode);
        int btnOpacity = 100;
        if (getIntent().hasExtra("btnOpacity")){
            btnOpacity = Integer.parseInt(getIntent().getStringExtra("btnOpacity"));
        }
        joystickBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        upBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        downBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        leftBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        rightBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        closeBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        rotateBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        keyboardBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        aBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        bBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        xBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        yBtn.getBackground().setAlpha(Math.round(btnOpacity * 2.25f));
        inputConnection = new BaseInputConnection(view.getRootView(),true);
        joystickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (joystickLay.getVisibility() == View.INVISIBLE){
                    Log.d("WebView","Show Joystick");
                    joystickLay.setVisibility(View.VISIBLE);
                    joystickLay.bringToFront();
                    joystickLay.invalidate();
                    joystickBtn.bringToFront();
                    joystickBtn.invalidate();
                } else {
                    joystickLay.setVisibility(View.INVISIBLE);
                    Log.d("WebView","Hide Joystick");
                }
            }
        });

        upBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_DPAD_UP);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_DPAD_UP);
                    return true;
                }
                return false;
            }
        });



        downBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_DPAD_DOWN);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_DPAD_DOWN);
                    return true;
                }
                return false;
            }
        });

        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_DPAD_LEFT);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_DPAD_LEFT);
                    return true;
                }
                return false;
            }
        });

        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                    return true;
                }
                return false;
            }
        });

        aBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ENTER);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ENTER);
                    return true;
                }
                return false;
            }
        });

        bBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ESCAPE);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ESCAPE);
                    return true;
                }
                return false;
            }
        });

        xBtn.setText(KeyEvent.keyCodeToString(xKeyCode).replace("KEYCODE_",""));

        xBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(xKeyCode);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(xKeyCode);
                    return true;
                }
                return false;
            }
        });

        yBtn.setText(KeyEvent.keyCodeToString(yKeyCode).replace("KEYCODE_",""));

        yBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    SDLActivity.onNativeKeyDown(yKeyCode);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP){
                    SDLActivity.onNativeKeyUp(yKeyCode);
                    return true;
                }
                return false;
            }
        });

        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (getRequestedOrientation()){
                    case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        break;
                    case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        break;

                }
            }
        });

        keyboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isAcceptingText()){
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                } else {
                    imm.toggleSoftInputFromWindow(view.getWindowToken(),0,0);
                }
            }
        });


        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();
            }
        });
    }


    private void setupMainWindowDisplayMode() {
        View decorView = setSystemUiVisibilityMode();
        decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setSystemUiVisibilityMode(); // Needed to avoid exiting immersive_sticky when keyboard is displayed
            }
        });
    }

    private View setSystemUiVisibilityMode() {
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            int options;
            options =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            decorView.setSystemUiVisibility(options);

        }

        return decorView;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            setupMainWindowDisplayMode();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupMainWindowDisplayMode();
    }

    // Code to unpack python and get things running ///////////////////////////

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    /**
     * This determines if unpacking one the zip files included in
     * the .apk is necessary. If it is, the zip file is unpacked.
     */
    public void unpackData(final String resource, File target) {

        /**
         * Delete main.pyo unconditionally. This fixes a problem where we have
         * a main.py newer than main.pyo, but start.c won't run it.
         */
        new File(target, "main.pyo").delete();

        boolean shouldUnpack = false;

        // The version of data in memory and on disk.
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        // If no version, no unpacking is necessary.
        if (data_version != null) {

            try {
                byte buf[] = new byte[64];
                InputStream is = new FileInputStream(disk_version_fn);
                int len = is.read(buf);
                disk_version = new String(buf, 0, len);
                is.close();
            } catch (Exception e) {
                disk_version = "";
            }

            if (! data_version.equals(disk_version)) {
                shouldUnpack = true;
            }
        }


        // If the disk data is out of date, extract it and write the
        // version file.
        if (shouldUnpack) {
            Log.v("python", "Extracting " + resource + " assets.");

            // Delete old libraries & renpy files.
            recursiveDelete(new File(target, "lib"));
            recursiveDelete(new File(target, "renpy"));

            target.mkdirs();

            AssetExtract ae = new AssetExtract(this);
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                toastError("Could not extract " + resource + " data.");
            }

            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
            }
        }

    }

    /**
     * Show an error using a toast. (Only makes sense from non-UI
     * threads.)
     */
    public void toastError(final String msg) {

        final Activity thisActivity = this;

        runOnUiThread(new Runnable () {
            public void run() {
                Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
            }
        });

        // Wait to show the error.
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public native void nativeSetEnv(String variable, String value);

    public void preparePython() {
        Log.v("python", "Starting preparePython.");

        mActivity = this;

        resourceManager = new ResourceManager(this);

        File gameDir = new File("");
        if (getIntent() != null && getIntent().hasExtra("gameFolder")){
            gameDir = new  File(getIntent().getStringExtra("gameFolder"));
        }

        File oldExternalStorage = gameDir;
        File externalStorage = gameDir;
        if (!externalStorage.exists()){
            externalStorage.mkdirs();
        }

        File path;

        if (externalStorage == null) {
            externalStorage = oldExternalStorage;
        }

        if (resourceManager.getString("public_version") != null) {
            path = externalStorage;
        } else {
            path = getFilesDir();
        }

        unpackData("private", getFilesDir());
        // unpackData("public", externalStorage);

        nativeSetEnv("ANDROID_ARGUMENT", path.getAbsolutePath());
        nativeSetEnv("ANDROID_PRIVATE", getFilesDir().getAbsolutePath());
        nativeSetEnv("ANDROID_PUBLIC",  externalStorage.getAbsolutePath());
        nativeSetEnv("ANDROID_OLD_PUBLIC", oldExternalStorage.getAbsolutePath());

        if (getIntent().hasExtra("renpy_developer")){
            if (getIntent().getBooleanExtra("renpy_developer",false)){
                nativeSetEnv("JOIPLAY_DEVELOPER", "1");
            } else {
                nativeSetEnv("JOIPLAY_DEVELOPER", "0");
            }
        }

        if (getIntent().hasExtra("renpy_hw_video")){
            if (getIntent().getBooleanExtra("renpy_hw_video",false)){
                nativeSetEnv("JOIPLAY_HW_VIDEO", "1");
            } else {
                nativeSetEnv("JOIPLAY_HW_VIDEO", "0");
            }
        }

        if (getIntent().hasExtra("renpy_autosave")){
            if (getIntent().getBooleanExtra("renpy_autosave",false)){
                nativeSetEnv("JOIPLAY_AUTOSAVE", "1");
            } else {
                nativeSetEnv("JOIPLAY_AUTOSAVE", "0");
            }
        }

        // Figure out the APK path.
        String apkFilePath;
        ApplicationInfo appInfo;
        PackageManager packMgmr = getApplication().getPackageManager();

        try {
            appInfo = packMgmr.getApplicationInfo(getPackageName(), 0);
            apkFilePath = appInfo.sourceDir;
        } catch (NameNotFoundException e) {
            apkFilePath = "";
        }

        nativeSetEnv("ANDROID_APK", apkFilePath);

        String expansionFile = getIntent().getStringExtra("expansionFile");

        if (expansionFile != null) {
            nativeSetEnv("ANDROID_EXPANSION", expansionFile);
        }

        nativeSetEnv("PYTHONOPTIMIZE", "2");
        nativeSetEnv("PYTHONHOME", getFilesDir().getAbsolutePath());
        nativeSetEnv("PYTHONPATH", path.getAbsolutePath() + ":" + getFilesDir().getAbsolutePath() + "/lib");

        Log.v("python", "Finished preparePython.");

    }

    // Code to support devicePurchase. /////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Store.create(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Store.getStore().destroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Store.getStore().onActivityResult(requestCode, resultCode, intent)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Code to support public APIs. ////////////////////////////////////////////

    public void openUrl(String url) {
        Log.i("python", "Opening URL: " + url);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void vibrate(double s) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate((int) (1000 * s));
        }
    }

    public int getDPI() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    public PowerManager.WakeLock wakeLock = null;

    public void setWakeLock(boolean active) {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, ":Screen On");
            wakeLock.setReferenceCounted(false);
        }

        if (active) {
            wakeLock.acquire();
        } else {
            wakeLock.release();
        }
    }

}
