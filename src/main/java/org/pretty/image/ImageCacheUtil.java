//package org.pretty.image;
//
//import java.io.IOException;
//import java.io.OutputStream;
//
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.LruCache;
//
//import com.hwk.utils.CommonUtil;
//import com.hwk.utils.Debug;
//import com.hwk.utils.FileUtil;
//
///**
// * 图片缓存帮助类
// * 包含内存缓存LruCache和磁盘缓存DiskLruCache
// */
//@SuppressLint("NewApi")
//public class ImageCacheUtil {
//
//
//    //缓存类
//    private static LruCache<String, Bitmap> mLruCache;
//    private static DiskLruCache mDiskLruCache;
//
//    //磁盘缓存大小
//    private static final int DISK_MAX_SIZE = 30 * 1024 * 1024;
//
//    @SuppressLint("NewApi")
//	public ImageCacheUtil(Context context) {
//        // 获取应用可占内存的1/8作为缓存
//        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 6);
//        // 实例化LruCaceh对象
//        mLruCache = new LruCache<String, Bitmap>(maxSize) {
//            @Override
//            protected int sizeOf(String key, Bitmap bitmap) {
//                return bitmap.getRowBytes() * bitmap.getHeight();
//            }
//        };
//        try {
//            // 获取DiskLruCahce对象
//            mDiskLruCache = DiskLruCache.open(FileUtil.getDiskCacheDir(context, "ImageCache"), getAppVersion(context), 1, DISK_MAX_SIZE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 从缓存（内存缓存，磁盘缓存）中获取Bitmap
//     */
//    public Bitmap getBitmap(String url) {
//        if (mLruCache.get(url) != null) {
//            // 从LruCache缓存中取
//            Debug.video("read bitmap to memory url:" + url);
//            return mLruCache.get(url);
//        } else {
//            String key = CommonUtil.Md5Encoder(url);
//            try {
//                if (mDiskLruCache.get(key) != null) {
//                    // 从DiskLruCahce取
//                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
//                    Bitmap bitmap = null;
//                    if (snapshot != null) {
//                        bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
//                        // 存入LruCache缓存
//                        mLruCache.put(url, bitmap);
//                        Debug.video("read bitmap to local url:" + url);
//                    }
//                    return bitmap;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 存入缓存（内存缓存，磁盘缓存）
//     */
//    public void putBitmap(String url, Bitmap bitmap) {
//        // 存入LruCache缓存
//        mLruCache.put(url, bitmap);
//        Debug.video("write bitmap to memory url:" + url);
//        // 判断是否存在DiskLruCache缓存，若没有存入
//        String key = CommonUtil.Md5Encoder(url);
//        try {
//            if (mDiskLruCache.get(key) == null) {
//                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
//                if (editor != null) {
//                    OutputStream outputStream = editor.newOutputStream(0);
//                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
//                        editor.commit();
//                    } else {
//                        editor.abort();
//                    }
//                }
//                mDiskLruCache.flush();
//                Debug.video("write bitmap to local url:" + url);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//
//    /**
//     * 获取应用版本号
//     *
//     * @param context
//     * @return
//     */
//    private int getAppVersion(Context context) {
//        try {
//            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return info.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return 1;
//    }
//
//}