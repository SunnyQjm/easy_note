package cn.yml.note.utils;

import android.os.Environment;
import cn.yml.note.App;


import java.io.File;


/**
 * Created by tanshunwang on 2016/9/21 0021.
 */
public class FileUtils {


    /**
     * 获得应用的根目录
     *
     * @return
     */
    public static String getAppPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + App.APP_NAME + "/");//获取根目录
        } else {
            sdDir = new File(Environment.getDataDirectory() + "/" + App.APP_NAME + "/");
        }
        if (!sdDir.exists()) {
            sdDir.mkdirs();
        }
        return sdDir.toString();
    }

    public static String getCachePath() {
        String targetPath = getAppPath() + "/Cache/";
        File file = new File(targetPath);
        if (!file.exists())
            file.mkdirs();
        return targetPath;
    }

    public static String getRecordPath() {
        String targetPath = getAppPath() + "/Record/";
        File file = new File(targetPath);
        if (!file.exists())
            file.mkdirs();
        return targetPath;
    }

    public static String getDownloadPath() {
        String targetPath = getAppPath() + "/Download/";
        File file = new File(targetPath);
        if (!file.exists())
            file.mkdirs();
        return targetPath;
    }

    public static String getWifiDirectPath() {
        String path = getAppPath() + "/WifiDirectFile/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }


    /**
     * 获得SDCard的根目录
     *
     * @return
     */
    public static String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().getPath();
        } else {
            return null;
        }
    }


    public static String getPath() {
        String path = getAppPath() + "/header/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("54555");
        }
        return path;
    }


//    public static void openFile(final Context context, final String filePath) {
//        Intent intent = FileUtils.getOpenFileIntent(context, filePath);
//        if (intent != null)
//            context.startActivity(intent);
//        else
//            Toast.makeText(context, "该文件无法打开", Toast.LENGTH_SHORT).show();
//    }

//    /**
//     * 打开一个文件
//     *
//     * @param context
//     * @param filePath 文件的绝对路径
//     */
//    public static Intent getOpenFileIntent(Context context, final String filePath) {
//        try {
//            String ext = filePath.substring(filePath.lastIndexOf('.')).toLowerCase(Locale.US);
//            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//            String temp = ext.substring(1);
//            String mime = mimeTypeMap.getMimeTypeFromExtension(temp);
//
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            File file = new File(filePath);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                //添加这一句表示对目标应用临时授权该Uri所代表的文件
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setDataAndType(FileProvider.getUriForFile(context,
//                        MyFileProvider.VIRTUAL_NAME, file), mime);
//            } else {
//                intent.setDataAndType(Uri.fromFile(file), mime);
//            }
//            return intent;
//        } catch (Exception e) {
//            Logger.e(e, "文件无法打开");
//            return null;
//        }
//    }

}
