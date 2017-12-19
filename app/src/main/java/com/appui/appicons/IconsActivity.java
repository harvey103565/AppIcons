package com.appui.appicons;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class IconsActivity extends AppCompatActivity {

    @BindView(R.id.summary_text)
    TextView mSummaryText;

    private ResolveInfo[] mAllResolves;

    private int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons);
        ButterKnife.bind(this);

        Single.just(this)
                .map(new Function<IconsActivity, PackageManager>() {
                    @Override
                    public PackageManager apply(IconsActivity iconsActivity) throws Exception {
                        return iconsActivity.getPackageManager();
                    }
                })
                .map(new Function<PackageManager, List<ResolveInfo>>() {
                    @Override
                    public List<ResolveInfo> apply(PackageManager packageManager) {
                        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                        return packageManager.queryIntentActivities(mainIntent, 0);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<ResolveInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<ResolveInfo> resolveInfos) {
                        int packagenum = resolveInfos.size();

                        mAllResolves = resolveInfos.toArray(new ResolveInfo[packagenum]);

                        String summary = getResources().getString(R.string.apps_loaded, packagenum);
                        mSummaryText.setText(summary);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mSummaryText.setText(R.string.load_app_failure);
                    }
                });
    }

    @OnClick(R.id.save_button)
    public void saveDrawables(View view) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);

                // MY_PERMISSIONS_REQUEST_WRITE_STORAGE is an app-defined int constant.
                // The callback method gets the result of the request.
            }
        } else {
            saveAppIconsToFile();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        saveAppIconsToFile();
    }

    public void saveAppIconsToFile() {
        Observable.fromArray(mAllResolves)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResolveInfo info) {
                        PackageManager packageManager = IconsActivity.this.getPackageManager();
                        Drawable drawable = info.loadIcon(packageManager);
                        String label = info.activityInfo.loadLabel(packageManager).toString();
                        saveDrawable(drawable, label);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mSummaryText.setText(R.string.save_fail);
                    }

                    @Override
                    public void onComplete() {
                        mSummaryText.setText(R.string.save_complete);
                    }
                });
    }


    public Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

//        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                : Bitmap.Config.RGB_565;
//        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        Bitmap bitmap = Bitmap.createBitmap(h, h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    private void saveDrawable(Drawable drawable, String name) {
        String path = newIconDirName();

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Bitmap bitmap = drawableToBitmap(drawable);

        File file = new File(path, name + ".png");
        try {
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } finally {
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String newIconDirName() {
        StringBuffer pathbuilder = new StringBuffer("");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            pathbuilder.append(Environment.getExternalStorageDirectory().getPath());
        } else {
            pathbuilder.append(Environment.DIRECTORY_PICTURES);
        }

        pathbuilder.append(File.separator)
                .append("Pictures")
                .append(File.separator)
                .append("AppIcons");

        return pathbuilder.toString();
    }
}
