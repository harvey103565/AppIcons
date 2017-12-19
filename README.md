# AppIcons
Put all app icons in app tray into files.

# Howto:
 Install apk and push the button. :)


# Get PackageManager
```
    PackageManager packageManager = IconsActivity.this.getPackageManager();
```

# Query Apps which has icon in app tray
```
    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
```

# Get App Icon and Name(label):
```
    ResolveInfo info = resolveInfos.get(i);
    PackageManager packageManager = IconsActivity.this.getPackageManager();
    Drawable drawable = info.loadIcon(packageManager);
    String label = info.activityInfo.loadLabel(packageManager).toString();
```

# Convert Drawable to Bitmap:
```
    int w = drawable.getIntrinsicWidth();
    int h = drawable.getIntrinsicHeight();

    Bitmap bitmap = Bitmap.createBitmap(h, h, Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, w, h);
    drawable.draw(canvas);
```

# And then save Bitmap to file
```
    // Assuming that you already have a file to store the image.
    OutputStream outputStream = new FileOutputStream(file);
    try {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    } finally {
        outputStream.flush();
        outputStream.close();
    }
```
