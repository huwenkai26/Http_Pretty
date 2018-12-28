//package org.pretty.download;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//
//
//import com.hwk.utils.Debug;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * Created by Administrator on 2017/4/21/021.
// */
//
//public class DownloadSqlTool {
//    private DownLoadHelper dbHelper;
//
//    public DownloadSqlTool(Context context) {
//        dbHelper = new DownLoadHelper(context);
//    }
//
//    /**
//     * 创建下载的具体信息
//     */
//    public void insertInfos(List<DownloadInfo> infos) {
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        for (DownloadInfo info : infos) {
//            String sql = "insert into download_info(thread_id,start_pos, end_pos,complete_size,url) values (?,?,?,?,?)";
//            Object[] bindArgs = {info.getThreadId(), info.getStartPos(),
//                    info.getEndPos(), info.getCompleteSize(), info.getUrl()};
//            database.execSQL(sql, bindArgs);
//        }
//    }
//
//    /**
//     * 得到下载具体信息
//     */
//    public List<DownloadInfo> getInfos(String urlstr) {
//        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        String sql = "select thread_id, start_pos, end_pos,complete_size,url from download_info where url=?";
//        Cursor cursor = database.rawQuery(sql, new String[]{urlstr});
//        while (cursor.moveToNext()) {
//            DownloadInfo info = new DownloadInfo(cursor.getInt(0),
//                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
//                    cursor.getString(4));
//            list.add(info);
//        }
//        return list;
//    }
//
//    /**
//     * 更新数据库中的下载信息
//     */
//    public void updateInfos(int threadId, int completeSize, String downloadUrl) {
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        String sql = "update download_info set complete_size=? where thread_id=? and url=?";
//        Object[] bindArgs = {completeSize, threadId, downloadUrl};
//        database.execSQL(sql, bindArgs);
//    }
//
//    /**
//     * 关闭数据库
//     */
//    public void closeDb() {
//        dbHelper.close();
//    }
//
//    /**
//     * 下载完成后删除数据库中的数据
//     */
//    public void delete(String url) {
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        Debug.video("delete download info url:" + url);
//        database.delete("download_info", "url=?", new String[]{url});
//    }
//}
