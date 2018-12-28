//package org.pretty.download;
//
//import android.content.Context;
//import android.os.Handler;
//import android.os.Message;
//
//import com.hwk.utils.Debug;
//
//import java.io.File;
//import java.io.InputStream;
//import java.io.RandomAccessFile;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * Created by Administrator on 2017/4/21/021.
// */
//
//public class DownloadHttpTool {
//
//    private int retryCount;
//
//    private int threadCount;//线程数量
//    private String downloadUrl;//URL地址
//    private Context mContext;
//    private Handler mHandler;
//    private List<DownloadInfo> downloadInfos;//保存下载信息的类
//
//    private String localPath;//目录
//    private String fileName;//文件名
//    private int fileSize;
//    private DownloadSqlTool sqlTool;//文件信息保存的数据库操作类
//    private int errorCount;
//
//    private enum Download_State {
//        Downloading, Pause, Ready//利用枚举表示下载的三种状态
//    }
//
//    private Download_State state = Download_State.Ready;//当前下载状态
//
//    private int globalComplete = 0;//所有线程下载的总数
//
//    public DownloadHttpTool(int threadCount, String urlString,
//                            String localPath, String fileName, int retryCount, Context context, Handler handler) {
//        this.threadCount = threadCount;
//        this.downloadUrl = urlString;
//        this.localPath = localPath;
//        this.mContext = context;
//        this.mHandler = handler;
//        this.fileName = fileName;
//        this.retryCount = retryCount;
//        sqlTool = new DownloadSqlTool(mContext);
//    }
//
//    //在开始下载之前需要调用ready方法进行配置
//    public boolean ready() {
//
//        Debug.download("ready");
//        globalComplete = 0;
//        downloadInfos = sqlTool.getInfos(downloadUrl);
//        File file = new File(localPath + "/" + fileName);
//        if (downloadInfos.size() == 0) {
//            if (file.exists()) {
//                Debug.download("file exists file size:" + file.length());
//                return false;
//            } else {
//                initFirst();
//                return true;
//            }
//
//        } else {
//            if (!file.exists()) {
//                sqlTool.delete(downloadUrl);
//                initFirst();
//                return true;
//            } else {
//                //文件存在时
//                fileSize = downloadInfos.get(downloadInfos.size() - 1)
//                        .getEndPos();
//                for (DownloadInfo info : downloadInfos) {
//                    globalComplete += info.getCompleteSize();
//                }
//
//                if (globalComplete == 0) {
//                    //1.globalComplete==0:表示已存在完整的文件
//                    Debug.download("file exists and file Cached part CompleteSize:" + globalComplete + "----fileSize:" + file);
//                    return true;
//                } else {
//                    //2.globalComplete>0:表示存在正在下载中的文件
//                    Debug.download("file exists and file Cached part CompleteSize:" + globalComplete + "----fileSize:" + file);
//                    return true;
//                }
//            }
//        }
//    }
//
//    public void start() {
//
//        if (downloadInfos != null) {
//            if (state == Download_State.Downloading) {
//                onError("file is downloading");
//                return;
//            }
//            state = Download_State.Downloading;
//            for (DownloadInfo info : downloadInfos) {
//                Debug.download("startThread:" + info.getThreadId() + "--currentDownloadPos:" + info.getCompleteSize());
//                new DownloadThread(info.getThreadId(), info.getStartPos(),
//                        info.getEndPos(), info.getCompleteSize(),
//                        info.getUrl()).start();
//            }
//        }
//    }
//
//
//    //第一次下载初始化
//    private void initFirst() {
//        Debug.download("initFirst");
//        try {
//            URL url = new URL(downloadUrl);
//            HttpURLConnection connection = (HttpURLConnection) url
//                    .openConnection();
//            connection.setConnectTimeout(8000);
//            connection.setRequestMethod("GET");
//            fileSize = connection.getContentLength();
//            if (fileSize < 0) {
//                retryInit("connection.getContentLength() == -1");
//                return;
//            }
//            File fileParent = new File(localPath);
//            if (!fileParent.exists()) {
//                fileParent.mkdirs();
//            }
//            File file = new File(fileParent, fileName);
//            if (!file.exists()) {
//                file.createNewFile();
//                DownloadCache.getInstance().writeCacheRecord(fileName, file.getAbsolutePath());
//            }
//            //本地访问文件
//            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
//            accessFile.setLength(fileSize);
//            accessFile.close();
//            connection.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//            retryInit(e.getMessage());
//            return;
//        }
//        int range = fileSize / threadCount;
//        downloadInfos = new ArrayList<DownloadInfo>();
//        for (int i = 0; i < threadCount - 1; i++) {
//            DownloadInfo info = new DownloadInfo(i, i * range, (i + 1) * range
//                    - 1, 0, downloadUrl);
//            downloadInfos.add(info);
//        }
//        DownloadInfo info = new DownloadInfo(threadCount - 1, (threadCount - 1)
//                * range, fileSize - 1, 0, downloadUrl);
//        downloadInfos.add(info);
//        sqlTool.insertInfos(downloadInfos);
//    }
//
//
//    private void retryInit(String errorMsg) {
//        retryCount--;
//        if (retryCount > 0) {
//            Debug.e("Download the initialization exception, request the link again---surplus retry count:" + retryCount);
//            initFirst();
//        } else {
//            onError("download init error:" + errorMsg);
//        }
//    }
//
//    public void pause() {
//        state = Download_State.Pause;
//        sqlTool.closeDb();
//    }
//
//    public void delete() {
//        complete();
//        File file = new File(localPath + "/" + fileName);
//        DownloadCache.getInstance().deleteCacheRecord(fileName);
//        file.delete();
//    }
//
//    public void complete() {
//        sqlTool.delete(downloadUrl);
//        sqlTool.closeDb();
//        state = Download_State.Ready;
//    }
//
//    public int getFileSize() {
//        return fileSize;
//    }
//
//    public int getCompleteSize() {
//        return globalComplete;
//    }
//
//    //自定义下载线程
//    private class DownloadThread extends Thread {
//
//        private int threadId;
//        private int startPos;
//        private int endPos;
//        private int completeSize;
//        private String downloadUrl;
//        private int totalThreadSize;
//        private int retryCount;
//
//
//        public DownloadThread(int threadId, int startPos, int endPos,
//                              int CompleteSize, String downloadUrl) {
//            this.threadId = threadId;
//            this.startPos = startPos;
//            this.endPos = endPos;
//            totalThreadSize = endPos - startPos + 1;
//            this.downloadUrl = downloadUrl;
//            this.completeSize = CompleteSize;
//            this.retryCount = 3;
//        }
//
//        @Override
//        public void run() {
//            HttpURLConnection connection = null;
//            RandomAccessFile randomAccessFile = null;
//            InputStream is = null;
//            try {
//                randomAccessFile = new RandomAccessFile(localPath + "/"
//                        + fileName, "rwd");
//                randomAccessFile.seek(startPos + completeSize);
//                URL url = new URL(downloadUrl);
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setConnectTimeout(6000);
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Range", "bytes=" + (startPos + completeSize) + "-" + endPos);
//                is = connection.getInputStream();
//                byte[] buffer = new byte[1024 * 10];
//                int length;
//                while ((length = is.read(buffer)) != -1) {
//                    randomAccessFile.write(buffer, 0, length);
//                    completeSize += length;
//                    Message message = Message.obtain();
//                    message.what = threadId;
//                    message.obj = downloadUrl;
//                    message.arg1 = length;
//                    mHandler.sendMessage(message);
//                    sqlTool.updateInfos(threadId, completeSize, downloadUrl);
//
////                        Debug.video("ThreadId::" + threadId + "    Complete::" + completeSize + "    total::" + totalThreadSize);
//
//
//                    if (completeSize >= totalThreadSize) {
//                        break;
//                    }
//                    if (state != Download_State.Downloading) {
//                        break;
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                retryCount--;
//                if (retryCount >= 0) {
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                    Debug.e("thread:" +threadId+" Download error, request the link again---surplus retry count" + retryCount);
//                    run();
//                } else {
//                    onError(threadId, e.getMessage());
//                }
//            } finally {
//                try {
//                    if (is != null) {
//                        is.close();
//                    }
//                    if (randomAccessFile != null)
//                        randomAccessFile.close();
//
//                    if (connection != null)
//                        connection.disconnect();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    private void onError(int threadId, String errorMsg) {
//        Debug.download("thread:" + threadId + " download error");
//        if (++errorCount >= threadCount) {
//            //每条线程都下载失败
//            Message message = Message.obtain();
//            message.what = -1;
//            message.obj = errorMsg;
//            mHandler.sendMessage(message);
//            errorCount=0;
//        }
//
//    }
//
//    private void onError(String errorMsg) {
//        Message message = Message.obtain();
//        message.what = -1;
//        message.obj = errorMsg;
//        mHandler.sendMessage(message);
//    }
//
//
//}