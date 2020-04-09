package com.smartism.znzk.util.yaokan;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Utility {

	private static String TAG = Utility.class.getSimpleName();
	
	private Utility() {
		
	}

	/**
	 * get the current calendar
	 * 
	 * @return Calendar
	 */
	public static Calendar getCurrentCalendarTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		return calendar;
	}

	/**
	 * isEmpty Test to see whether input string is empty.
	 * 
	 * @param str
	 *          a String
	 * @return True if it is empty; false if it is not.
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.length() == 0 || "".equals(str.trim()) || "null".equalsIgnoreCase(str.trim()));
	}

	/**
	 * isEmpty Test to see whether input string buffer is empty.
	 * 
	 * @param str
	 *          A StringBuffer
	 * @return True if it is empty; false if it is not.
	 */
	public static boolean isEmpty(StringBuffer stringBuffer) {
		return (stringBuffer == null || stringBuffer.length() == 0 || stringBuffer.toString().trim().equals(""));
	}

	/**
	 * isEmpty Test to see whether input string is empty.
	 * 
	 * @param str
	 *          A String
	 * @return True if it is empty; false if it is not.
	 */
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * isEmpty Test to see whether input is empty.
	 * 
	 * @param StringArray
	 *          A array
	 * @return True if it is empty; false if it is not.
	 */
	public static boolean isEmpty(String[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * isEmpty Test to see whether input is representing a NULL value.
	 * 
	 * @param val
	 *          An Object
	 * @return True if it represents NULL; false if it is not.
	 */
	public static boolean isEmpty(Object val) {
		return (val == null);
	}

	/**
	 * isEmpty Test to see whether input is empty.
	 * 
	 * @param list
	 *          A List
	 * @return True if it is empty; false if it is not.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(List list) {
		return (list == null || list.isEmpty() || list.size() == 0);
	}

	/**
	 * isEmpty Test to see whether input is empty.
	 * 
	 * @param vector
	 *          A Vector
	 * @return True if it is empty; false if it is not.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(java.util.Vector vector) {
		return (vector == null || vector.size() == 0);
	}

	/**
	 * getStringArray
	 * 
	 * @param ary
	 *          The object array.
	 * @return The list contains all the object array elements.
	 */
	@SuppressWarnings("rawtypes")
	public static String[] getStringArray(List list) {
		if (list == null) {
			return (null);
		}
		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			try {
				result[i] = (String) list.get(i);
			} catch (ClassCastException ce) {
				result[i] = ((Integer) list.get(i)).toString();
			}
		}
		return (result);
	}


	/**
	 * remove the repeat element in the list
	 * 
	 * @param list
	 * @return newList
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List removeDuplicate(List list) {
		if (Utility.isEmpty(list)) {
			return null;
		}
		HashSet h = new HashSet(list);
		List newList = new ArrayList();
		newList.addAll(h);
		return newList;
	}

	/**
	 * InputStream is changed to the String
	 * 
	 * @param is
	 *          an InputStream
	 * @return String
	 * @throws IOException
	 */
	public static String inputStream2String(InputStream is) throws IOException {
		if (null == is) {
			return "";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	/**
	 * get the key set from the Map
	 * 
	 * @param map
	 *          is Map Collection
	 * @return collection
	 */
	@SuppressWarnings("rawtypes")
	public static Set keySetFromMap(Map map) {
		return map.keySet();
	}

	/**
	 * get the value set from the Map Collection
	 * 
	 * @param map
	 *          is Map collection
	 * @return collection
	 */
	@SuppressWarnings("rawtypes")
	public static Set valueSetFromMap(Map map) {
		return map.entrySet();
	}

	/**
	 * 返回年月�?
	 * @param date
	 * @return
	 */
	public static String getDateByDate(Date date){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());  
        return format.format(date);  
	}
	
	 //把日期转为字符串  
    public static String converToString(Date date)  
    {  
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.getDefault());  
        return df.format(date);  
    }  
    //把字符串转为日期  
    public static Date converToDate(String strDate)  
    {  
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.getDefault());  
        try {
			return df.parse(strDate);
		} catch (ParseException e) {
			//Logger.e(TAG , "error:" + e.getMessage());
		}  
        return null ;
    }  
	
}
