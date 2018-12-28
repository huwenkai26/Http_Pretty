//package org.pretty.download;
//
//import com.hwk.utils.Debug;
//import com.hwk.utils.FileUtil;
//
//import java.io.File;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//
///**
// * Created by Administrator on 2017/5/11.
// */
//
//public class DownloadCache implements Serializable {
//	private static final long serialVersionUID = -6816252270578098084L;
//    //缓存有效时间，7day
//    private final int CACHE_VALID_TIME = 7 * 24 * 60 * 60 * 1000;
//    private static final String CACHE_DIR = "DownloadCache";
//    private static final String CACHE_FILE_INFOS = "cacheFileInfos";
//    private File fileParent;
//    private static DownloadCache downloadCache;
//    //缓存文件的最大个数
//    private static final int CACHE_MAX_COUNT = 10;
//    private List<CacheFileInfo> cacheFileInfos = null;
//
//
//    private DownloadCache() {
//        optimizeCacheInfo();
//        fileParent = FileUtil.getDiskDir(CACHE_DIR);
//    }
//
//    public static DownloadCache getInstance() {
//        if (downloadCache == null) {
//            synchronized (DownloadCache.class) {
//                if (downloadCache == null) {
//                    downloadCache = new DownloadCache();
//                }
//            }
//        }
//
//        return downloadCache;
//    }
//
//    public void writeCacheRecord(String fileName, String filePath) {
//
//        //1.读取缓存信息
//        cacheFileInfos = (List<CacheFileInfo>) FileUtil.readObject(fileParent, CACHE_FILE_INFOS);
//        if (cacheFileInfos == null) {
//            cacheFileInfos = new ArrayList<CacheFileInfo>();
//        }
//        Debug.download("DownloadCache current cache info" + cacheFileInfos.toString());
//        //2.判断是否已经存在该缓存
//        boolean isExist = false;
//        for (CacheFileInfo cacheFileInfo : cacheFileInfos) {
//            if (cacheFileInfo.fileName.equals(fileName)) {
//                isExist = true;
//                break;
//            }
//        }
//        if (!isExist) {
//            //2.1 增加缓存信息
//            CacheFileInfo cacheFileInfo = new CacheFileInfo(fileName, 0, System.currentTimeMillis(), filePath);
//            cacheFileInfos.add(cacheFileInfo);
//            //3.更新缓存信息
//            FileUtil.writeObject(fileParent, CACHE_FILE_INFOS, cacheFileInfos);
//            //4.每次缓存文件 都优化一次缓存空间
//            Debug.download("writeCacheRecord fileName:" + fileName);
//            Debug.download("DownloadCache current cache info" + cacheFileInfos.toString());
//        } else {
//            Debug.download("writeCacheRecord error file exist fileName:" + fileName);
//        }
//
//    }
//
//    public void deleteCacheRecord(String fileName) {
//        //1.读取缓存信息
//        cacheFileInfos = (List<CacheFileInfo>) FileUtil.readObject(fileParent, CACHE_FILE_INFOS);
//        if (cacheFileInfos == null) {
//            cacheFileInfos = new ArrayList<CacheFileInfo>();
//        }
//
//        //2.判断是否已经存在该缓存
//        boolean isExist = false;
//        int deleteIndex = 0;
//        for (CacheFileInfo cacheFileInfo : cacheFileInfos) {
//            if (cacheFileInfo.fileName.equals(fileName)) {
//                isExist = true;
//                break;
//            }
//            deleteIndex++;
//        }
//
//        if (isExist) {
//            //2.1删除缓存信息
//            cacheFileInfos.remove(deleteIndex);
//            Debug.download("deleteCacheRecord fileName:" + fileName);
//            FileUtil.writeObject(fileParent, CACHE_FILE_INFOS, cacheFileInfos);
//            Debug.download("DownloadCache current cache info" + cacheFileInfos.toString());
//            //3.每次缓存 都优化一次缓存空间
//            optimizeCache();
//        }
//
//    }
//
//    public void updateCacheRecordByUseCount(String fileName) {
//        cacheFileInfos = (List<CacheFileInfo>) FileUtil.readObject(fileParent, CACHE_FILE_INFOS);
//        if (cacheFileInfos == null) {
//            cacheFileInfos = new ArrayList<CacheFileInfo>();
//        }
//        for (CacheFileInfo fileInfo : cacheFileInfos) {
//            if (fileInfo.getFileName().equals(fileName)) {
//                fileInfo.useCount++;
//                fileInfo.cacheTime = System.currentTimeMillis();
//                Debug.download("updateCacheRecordByUseCount fileName:" + fileName);
//                FileUtil.writeObject(fileParent, CACHE_FILE_INFOS, cacheFileInfos);
//                Debug.download("DownloadCache current cache info" + cacheFileInfos.toString());
//                break;
//            }
//        }
//
//        optimizeCache();
//    }
//
//
//    /*
//    * 优化缓存文件
//    * */
//    private void optimizeCache() {
//
//
//
//        if (cacheFileInfos.size() > CACHE_MAX_COUNT) {
//            //1.缓存文件数量时候超出限制,获取所有缓存文件记录 根据缓存时间及使用次数进行删除旧文件
//
//            //2.排序算法，将使用次数最少和缓存时间最早的 排在集合最前端
//            Collections.sort(cacheFileInfos, new CacheComparator());
//            Debug.download("optimizeCache----cacheFileInfos:"+cacheFileInfos.toString());
//
//            //3.删除多余的缓存文件
//            int deleteCount = cacheFileInfos.size() - CACHE_MAX_COUNT;
//            ArrayList<CacheFileInfo> waitDeletes = new ArrayList<CacheFileInfo>();
//            for (int i = 0; i < cacheFileInfos.size(); i++) {
//                if (i < deleteCount) {
//                    waitDeletes.add(cacheFileInfos.get(i));
//                }
//            }
//            //删除文件及记录
//            for (CacheFileInfo waitDelete : waitDeletes) {
//                cacheFileInfos.remove(waitDelete);
//                new File(waitDelete.getCachePath()).delete();
//            }
//
//            //更新缓存记录
//            FileUtil.writeObject(fileParent, CACHE_FILE_INFOS, cacheFileInfos);
//            Debug.download("optimizeCache----cacheFileInfos:"+cacheFileInfos.toString());
//
//        }
//
//
//
//    }
//
//
//    /*
//    * 优化缓存记录和缓存文件 之间的无效记录
//    * */
//    public void optimizeCacheInfo(){
////        cacheFileInfos = (List<CacheFileInfo>) FileUtil.readObject(fileParent, CACHE_FILE_INFOS);
////        if (cacheFileInfos == null) {
////            cacheFileInfos = new ArrayList<>();
////        }
////
////
////        //1.获取本地已存取的缓存文件
////        File[] files = new File(Environment.getExternalStorageDirectory()+"/aaaa").listFiles();
////        ArrayList<String> filePaths = new ArrayList<>();
////        for (File file : files) {
////            Debug.d(file.getAbsolutePath());
////            filePaths.add(file.getAbsolutePath());
////        }
////
////
////        ArrayList<CacheFileInfo> FileInfos = new ArrayList<>();
////        for (CacheFileInfo cacheFileInfo : this.cacheFileInfos) {
////            if (filePaths.contains(cacheFileInfo.cachePath)){
////                Debug.d(cacheFileInfo.toString());
////                FileInfos.add(cacheFileInfo);
////            }else {
////                new File(cacheFileInfo.getCachePath()).delete();
////            }
////        }
////
////        if (cacheFileInfos.size()==0){
////            for (String filePath : filePaths) {
////                new File(filePath).delete();
////            }
////        }
////
////        cacheFileInfos.clear();
////        cacheFileInfos.addAll(FileInfos);
////        FileUtil.writeObject(fileParent,CACHE_FILE_INFOS,cacheFileInfos);
////
////        Debug.download("optimizeCacheInfo----cacheFileInfos:"+cacheFileInfos.toString());
//    }
//
//
//    private class CacheFileInfo implements Serializable {
//
//        private String fileName;
//        private int useCount;
//        private long cacheTime;
//        private String cachePath;
//
//        public CacheFileInfo(String fileName, int useCount, long cacheTime, String cachePath) {
//            this.fileName = fileName;
//            this.cacheTime = cacheTime;
//            this.cachePath = cachePath;
//            this.useCount = useCount;
//        }
//
//        public String getFileName() {
//            return fileName;
//        }
//
//
//        public long getCacheTime() {
//            return cacheTime;
//        }
//
//        public int getUseCount() {
//            return useCount;
//        }
//
//        public String getCachePath() {
//            return cachePath;
//        }
//
//        @Override
//        public String toString() {
//            return "CacheFileInfo{" +
//                    "fileName='" + fileName + '\'' +
//                    ", useCount=" + useCount +
//                    ", cacheTime=" + cacheTime +
//                    ", cachePath='" + cachePath + '\'' +
//                    '}';
//        }
//
//    }
//
//    private class CacheComparator implements Comparator<CacheFileInfo> {
//
//        //等待完善
//        @Override
//        public int compare(CacheFileInfo s1, CacheFileInfo s2) {
//
//            if (s1.getUseCount() != s2.getUseCount()) {
//
//                return s1.getUseCount() - s2.getUseCount();
//            } else {
//
//                if (s1.getCacheTime() != s2.getCacheTime()) {
//                    return (int) (s1.getCacheTime() - s2.getCacheTime());
//                } else {
//                    return 1;
//                }
//            }
//
//        }
//    }
//
//}
