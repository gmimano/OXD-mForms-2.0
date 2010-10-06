package org.fcitmuk.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utilities {
	
	public static String dateToString(Date d, byte format){
		if(d == null)
			return null;
		
		Calendar cd = Calendar.getInstance(TimeZone.getDefault()); //GMT+830
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);
		
		if (month.length()<2)
			month = "0" + month;
		
		if (day.length()<2)
			day = "0" + day;
		
		if(format == 0)
			return day + "-" + month + "-" + year;
		else if(format == 1)
			return month + "-" + day + "-" + year;
		return year + "-" + month + "-" + day;
	}
	
	public static boolean stringToBoolean(String val){
		return stringToBoolean(val,false);
	}
	
	public static boolean stringToBoolean(String val, boolean defaultValue) {
		if(val == null) {
			return defaultValue;
		} else {
			return !val.equals("0");
		}
	}
	
	public static String booleanToString(boolean val){
		if(val)
			return "1";
		return "0";
	}
}
