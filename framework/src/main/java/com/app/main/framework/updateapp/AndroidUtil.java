package com.app.main.framework.updateapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import com.app.main.framework.baseutil.LibLoader;

import java.io.File;

public class AndroidUtil {

    public static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] permissionManifest = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.REQUEST_INSTALL_PACKAGES};

    /**
     * 判断是否有安装的权限
     */
    @SuppressLint("WrongConstant")
    public static boolean hasInstallPermission() {
        //小于23，不需要权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (int i = 0; i < permissionManifest.length; i++) {
            String permission = permissionManifest[i];
            if (PermissionChecker.checkSelfPermission(LibLoader.getApplication(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请安装的权限
     * @param activity
     * @return
     */
    public static void applyInstallPermission(@NonNull Activity activity){
        ActivityCompat.requestPermissions(activity, permissionManifest, PERMISSION_REQUEST_CODE);
    }


    /**
     * 安装应用，需要适配7.0文件
     * https://blog.csdn.net/yangjunjin/article/details/94182050
     * @param activity
     * @param authority
     * @param path
     */
    public static void installApk(Activity activity, String authority, String path) {
        try {
            Intent intent = new Intent();
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (checkIsHuaWeiRom() || checkIsSamSungRom())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //String authority = appid + ".provider";
                Uri fileUri = FileProvider.getUriForFile(LibLoader.getApplication(), authority, file);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            activity.startActivityForResult(intent, 0x007);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否华为
     */
    public static boolean checkIsHuaWeiRom() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    /**
     * 判断是否是三星
     *
     * @return
     */
    public static boolean checkIsSamSungRom() {
        return Build.MANUFACTURER.contains("samsung");
    }

}
