//package org.pretty.callback;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//
//
//
///**
// * Created by Administrator on 2017/3/10.
// */
//
//public abstract class BitmapCallBack extends DefaultCallBack {
//
//    @Override
//    public void onSucceed(byte[] content) {
//        Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
//        if (bitmap!=null){
//            onSucceed(bitmap);
//        }else {
//            onError(new ErrorCode("bitmap is null"));
//        }
//
//    }
//
//    public abstract void onSucceed(Bitmap bitmap);
//
//}
