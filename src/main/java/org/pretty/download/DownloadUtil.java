//package org.pretty.download;
//
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Message;
//
//import java.io.File;
//
///**
// * Created by Administrator on 2017/4/21/021.
// * 将下载方法封装在此类
// * 提供下载，暂停，删除，以及重置的方法
// */
//public class DownloadUtil {
//
//    private final String videoFilePath;
//    private DownloadHttpTool mDownloadHttpTool;
//    private OnDownloadListener onDownloadListener;
//    private int fileSize;
//    private int downloadedSize = 0;
//
//    @SuppressLint("HandlerLeak")
//    private Handler mHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            // TODO Auto-generated method stub
//            super.handleMessage(msg);
//            int length = msg.arg1;
//            synchronized (this) {//加锁保证已下载的正确性
//                downloadedSize += length;
//            }
//            if (onDownloadListener != null) {
//                onDownloadListener.onProgress(downloadedSize, fileSize);
//            }
//            if (msg.what == -1) {
//                String errorMsg = (String) msg.obj;
//                if (onDownloadListener != null && errorMsg != null) {
//                    onDownloadListener.onError(errorMsg);
//                }
//                delete();//删除换成的文件
//            }
//            if (downloadedSize >= fileSize) {
//                mDownloadHttpTool.complete();
//                if (onDownloadListener != null) {
//                    onDownloadListener.onComplete(videoFilePath);
//                }
//            }
//        }
//
//    };
//
//    public DownloadUtil(int threadCount, String filePath, String fileName, String urlString, Context context) {
//        videoFilePath = filePath + File.separator + fileName;
//        mDownloadHttpTool = new DownloadHttpTool(threadCount, urlString,
//                filePath, fileName, 4, context, mHandler);
//    }
//
//    //下载之前首先异步线程调用ready方法获得文件大小信息，之后调用开始方法
//    public void start() {
//        new AsyncTask<Object, Object, Boolean>() {
//
//            @Override
//            protected Boolean doInBackground(Object... arg0) {
//                //下载初始化
//                return mDownloadHttpTool.ready();
//            }
//
//            @Override
//            protected void onPostExecute(Boolean result) {
//                super.onPostExecute(result);
//                if (result) {
//                    fileSize = mDownloadHttpTool.getFileSize();
//                    if (fileSize > 0){
//                        downloadedSize = mDownloadHttpTool.getCompleteSize();
//                        //开始请求下载
//                        if (onDownloadListener != null) {
//                            onDownloadListener.onStart(fileSize);
//                        }
//                        mDownloadHttpTool.start();
//                    }else {
//                        //download init error
//                        if (onDownloadListener != null) {
//                            onDownloadListener.onError("download init error:connection.getContentLength() == -1");
//                        }
//                    }
//
//
//
//                } else {
//                    //文件已存在
//                    if (onDownloadListener != null) {
//                        onDownloadListener.onComplete(videoFilePath);
//                    }
//                }
//            }
//        }.execute();
//    }
//
//    public void pause() {
//        mDownloadHttpTool.pause();
//        if (onDownloadListener != null) {
//            onDownloadListener.onPause(downloadedSize, fileSize);
//        }
//    }
//
//    public void delete() {
//        mDownloadHttpTool.delete();
//    }
//
//    public void reset() {
//        mDownloadHttpTool.delete();
//        start();
//    }
//
//    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
//        this.onDownloadListener = onDownloadListener;
//    }
//
//    //下载回调接口
//    public interface OnDownloadListener {
//        void onStart(int fileSize);
//
//        void onPause(int completeSize, int totalSize);
//
//        void onProgress(int progress, int totalSize);//记录当前所有线程下总和
//
//        void onComplete(String filePath);
//
//        void onError(String msg);
//    }
//}