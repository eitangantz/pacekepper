package com.pacekeeper.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.pacekeeper.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.os.Environment.DIRECTORY_PICTURES;

public class FileUtils {
    public static String saveImage(Bitmap bitmap) {
        String file_path = App.getInstance().getExternalFilesDir(DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        String fileName = UUID.randomUUID().toString() + ".png";
        File file = new File(dir,  fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getImage(String imageName) {
        String file_path = App.getInstance().getExternalFilesDir(DIRECTORY_PICTURES).getAbsolutePath();
        return Drawable.createFromPath(file_path + "/" + imageName);
    }
}
