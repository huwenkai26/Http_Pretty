//package org.pretty.image;
//
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.text.TextUtils;
//import android.widget.ImageView;
//
//import com.hwk.callback.BitmapCallBack;
//import com.hwk.callback.ErrorCode;
//import com.hwk.http.HttpUtils;
//import com.hwk.utils.Debug;
//
//
///**
// * Created by hwk on 2017/4/18.
// */
//
//public class ImageLoader {
//
//    private static ImageCacheUtil imageCacheUtil;
//    private static ImageLoadListener mImageLoadListener;
//
//    public static void loadImage(Context context, ImageView imageView, String url) {
//        loadImage(context,imageView,url,null);
//    }
//    public static void loadImage(Context context, ImageView imageView, String url,ImageLoadListener listener) {
//    	if (TextUtils.isEmpty(url)){
//    		Debug.e("loadImage url null or empty");
//    		return;
//    	}
//        mImageLoadListener=listener;
//
//        if (imageCacheUtil==null){
//            imageCacheUtil = new ImageCacheUtil(context);
//        }
//
//        if (mImageLoadListener!=null){
//            mImageLoadListener.start();
//        }
//
//        Bitmap bitmap = imageCacheUtil.getBitmap(url);
//        if (bitmap != null) {
//        	if(imageView!=null){
//                imageView.setImageBitmap(bitmap);
//        	}
//            if (mImageLoadListener!=null){
//                mImageLoadListener.complete();
//            }
//        } else {
//            getBitmapFromNet(context,imageView,url);
//        }
//    }
//
//    private static void getBitmapFromNet(Context context, final ImageView imageView, final String url){
//
//        HttpUtils.get().url(url).build(context).execute(new BitmapCallBack() {
//            @Override
//            public void onSucceed(Bitmap bitmap) {
//                imageCacheUtil.putBitmap(url,bitmap);
//                if(imageView!=null){
//                	imageView.setImageBitmap(bitmap);
//                }
//                if (mImageLoadListener!=null){
//                    mImageLoadListener.complete();
//                }
//            }
//
//            @Override
//            public void onError(ErrorCode errorCode) {
//                Debug.e(errorCode.toString());
//                if (mImageLoadListener!=null){
//                    mImageLoadListener.error(errorCode.toString());
//                }
//            }
//        });
//    }
//
//
//}
