package com.melon.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LoggerEx {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	static {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
	}
	public static String dateString(Date date) {
		return sdf.format(date);
	}
	public static String dateString(long time) {
		return sdf.format(new Date(time));
	}
	public static String dateString() {
		return sdf.format(new Date());
	}

	private static Logger logger = LoggerFactory.getLogger("");
	private LoggerEx() {}
	
	public static String getClassTag(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	public static void debug(String tag, String msg) {
	    logger.info(getLogMsg(tag, msg));
	}

//    public static void debug(String msg) {
//	    logger.info(msg);
//	}

	public static void info(String tag, String msg) {
	    logger.info(getLogMsg(tag, msg));
	}
	
//	public static void info(String msg) {
//	    logger.info(msg);
//	}
	
	public static void warn(String tag, String msg) {
	    logger.warn(getLogMsg(tag, msg));
	}
	
//	public static void warn(String msg) {
//	    logger.warn(msg);
//	}
	
	public static void error(String tag, String msg) {
	    logger.error(getLogMsg(tag, msg));
	}
	
//	public static void error(String msg) {
//	    logger.error(msg);
//	}

	public static void fatal(String tag, String msg) {
	    logger.error("FATAL: " + getLogMsg(tag, msg));
	}
	
	private static String getLogMsg(String tag, String msg) {
        return new StringBuilder("[").append(dateString()).append("|").append(tag).append("] ").append(msg).toString();
    }
	
}
