package org.pretty.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {
	public static boolean debug=false;
	private static final String SDK = "kksdk";	
	public static final String CORE = "kkcore";
	private static final String VIDEO = "kkvideo";
	private static final String DOWNLOAD = "kkdownload";
	private static final Logger Log = LoggerFactory.getLogger(Debug.class);

	//core模块的日志
	public static void core(String msg){
		if (!debug){
			Log.info(CORE, msg);
		}
	}
	
	//video模块的日志
	public static void video(String msg){
		if (!debug){
			Log.info(VIDEO, msg);
		}
	}
	
	//video模块的日志
	public static void download(String msg){
		if (!debug){
			Log.info(DOWNLOAD, msg);
		}
	}
	
	//客户可见的日志
	public static void i(String msg){
		Log.warn(SDK, msg);
	}
	//客户可见的日志
	public static void e(String msg){
		Log.error(SDK, msg);
	}
	
}
