package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.googlecode.tesseract.android.TessBaseAPI.PageSegMode.PSM_AUTO;
import static java.security.AccessController.getContext;

public final class MyTessOCR {

    private String datapath;
    private static TessBaseAPI mTess = new TessBaseAPI();
    Context context;

    public MyTessOCR(Context context) {
        this.context = context;
        datapath = context.getFilesDir()+ "/tesseract/";
        Log.d("datapath",datapath);
        checkFile(new File(datapath + "tessdata/"),context);
        mTess.setDebug(true);
        mTess.init(datapath, "eng");
        mTess.setPageSegMode(PSM_AUTO);
    }

    public void stopRecognition() {
        mTess.stop();
    }

    public String getOCRResult(Bitmap bitmap) {
        String whitelist = "0123456789";
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whitelist);
        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();
        return result;
    }

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }

    public void checkFile(File dir,Context context) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles(datapath,context);
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(datapath,context);
            }
        }
    }

    public void copyFiles(String datapath,Context context) {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = context.getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();
            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}