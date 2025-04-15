package org.astral.findmaimaiultra.utill;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtils {
    public static File getCacheDir(Context context, String fileName) {
        File cacheDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FindMaimaiUltra");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir, fileName);
    }
}
