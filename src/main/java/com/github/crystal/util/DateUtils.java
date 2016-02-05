package com.github.crystal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static final Date DEF_START = parseDate("1971-01-01");
	public static final Date DEF_END = parseDate("2037-01-01");

	public static boolean isBetween(Date v, Date start, Date end) {
		if (v.compareTo(start) >= 0 && v.compareTo(end) <= 0) {
			return true;
		}
		return false;
	}

	public static boolean isConflict(Date v1, Date v2, Date v3, Date v4) {
		if (isBetween(v1, v3, v4)) {
			return true;
		}
		if (isBetween(v2, v3, v4)) {
			return true;
		}
		if (isBetween(v3, v1, v2)) {
			return true;
		}
		if (isBetween(v4, v1, v2)) {
			return true;
		}
		return false;
	}

	public static Date parse(String val, String format) {
		try {
			return new SimpleDateFormat(format).parse(val);
		} catch (Exception e) {
		}
		return null;
	}

	public static Date parseDate(String val) {
		return parse(val, "yyyy-MM-dd");
	}
	
	public static Date parseDateTime(String val) {
		return parse(val, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static Date addYears(Date val, int amount){
		return org.apache.commons.lang.time.DateUtils.addYears(val, amount);
	}
	
	public static Date addMonths(Date val, int amount){
		return org.apache.commons.lang.time.DateUtils.addMonths(val, amount);
	}
	
	public static Date addDays(Date val, int amount){
		return org.apache.commons.lang.time.DateUtils.addDays(val, amount);
	}
	
	public static Date addHours(Date val, int amount){
		return org.apache.commons.lang.time.DateUtils.addHours(val, amount);
	}

	public static Date addSeconds(Date val, int amount){
		return org.apache.commons.lang.time.DateUtils.addSeconds(val, amount);
	}
	
	public static Date parse(String val, String format, Date def) {
		Date d = parse(val, format);
		if (d != null) {
			return d;
		}
		return def;
	}

	public static String format(Date val, String format) {
		return new SimpleDateFormat(format).format(val);
	}

	public static String formatDate(Date val) {
		return format(val, "yyyy-MM-dd");
	}

	public static String formatDateTime(Date val) {
		return format(val, "yyyy-MM-dd HH:mm:ss");
	}

}
