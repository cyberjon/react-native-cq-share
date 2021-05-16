package com.example.cukiy.cqshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class CQShareManager extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public CQShareManager(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "CQShare";
    }

    @ReactMethod
    public void sharePictureWithOptions(ReadableMap options) {


        String title = options.hasKey("title") ? options.getString("title") : null;
        ReadableArray remoteImages = options.hasKey("remoteImages") ? options.getArray("remoteImages") : null;
        ReadableArray localImages = options.hasKey("localImages") ? options.getArray("localImages") : null;
        String description = options.hasKey("description") ? options.getString("description") : null;

        if (title != null){

            Intent textIntent = new Intent(Intent.ACTION_SEND);
            textIntent.setType("text/plain");
            textIntent.putExtra(Intent.EXTRA_TEXT, title);
            getCurrentActivity().startActivity(Intent.createChooser(textIntent, "Text"));

        } else if (remoteImages != null || localImages != null) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            if (description != null) {
                intent.putExtra("Kdescription", description);
            }

            ArrayList<Uri> images = new ArrayList<Uri> ();

            if (remoteImages != null) {
                ArrayList<String> urls = new ArrayList<String>();
                for (int i=0; i<remoteImages.size(); i++ ) {
                    urls.add(remoteImages.getString(i));
                }
                images = saveImageToAlbum(this.reactContext,urls);
            }

            if (localImages != null) {
                for (int i=0; i<localImages.size(); i++ ) {
                    images.add(Uri.parse(localImages.getString(i)));
                }
            }

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,images);
	        getCurrentActivity().startActivity(Intent.createChooser(intent, "Image"));
        }
    }

    public static ArrayList<Uri> saveImageToAlbum(ReactApplicationContext reactContext, ArrayList<String> urls) {

        ArrayList<Uri> uris = new ArrayList<Uri>();

        for (String url : urls) {

            String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "shareImgs";
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = handleFileName(url) + ".jpg";
            File file = new File(appDir, fileName);

            if(file.exists()) {
                uris.add(Uri.fromFile(file));
            } else {
                URL myFileUrl = null;
                Bitmap bitmap = null;
                try {
                    myFileUrl = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                        fos.flush();
                        fos.close();

                        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file); 
                        //Uri.parse(file.getPath()); //Uri.fromFile(file);
                        reactContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        if (isSuccess) {
                            uris.add(uri);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return uris;
    }


    public static String handleFileName(String url) {
        url = url.substring(url.lastIndexOf("/"),url.length() - 4);
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = url.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }
}
