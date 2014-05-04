package org.mailnews.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class Constants {

	public Constants()
	{
		//utility class
	}
	//STYLE CONSTANTS
	public static final String DIV_WIDTH = "div-width";
	public static final String DIV_HEIGHT = "div-height";
	public static final String HEADER_MARGIN = "header-margin";
	public static final String TEXT_MARGIN = "text-margin";
	public static final String COLORS = "head-colors";
	public static final String PROGRESS_BAR_HEIGHT ="progress-bar-height";
	public static final String FONT_SIZE = "font-size";
	public static final String SPACING = "spacing";
	public static final String FONT_HEADER_SIZE = "font-header-size";
	public static final String HEADER_SPACING = "header-spacing";
	public static final String TABLE_BORDER_SIZE = "table-border-size";
	public static final String FONT_FAMILY = "font-family";
	public static final String HEADER_FONT_FAMILY = "header-font-family";
	public static final String IMG_MARGIN = "img-margin";
	public static final String TEXT_INDENTER = "text-indenter";
	public static final int LEFT = 3;
	public static final int RIGHT = 1;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;
	
	//EMAIL CONSTANTS
	public static final String LOGIN = "login";
	public static final String PASSWORD = "password";
	public static final String HOST = "host";
	public static final String PORT = "port"; 
	public static final String STORE_PROTOCOL = "store-protocol";
	
	//TIME CONSTANTS
	// letters refresh time in minutes
	public static final String LETTERS_REFRESH_TIME = "letters-refresh-time";
	// only letters for last number of days
	public static final String DAYS_PERIOD = "days-period";
	public static final String SYMB_PER_SEC = "symb-per-sec";
	public static final String IMAGE_WATCH_TIME_SEC = "image-watch-time";
//
//	static {
//		Field[] fields = Constants.class.getFields();
//		Properties styleProps = new Properties();
//		try {
//			for (int i = 0; i < fields.length; i++) {
//				styleProps.put(fields[i].get(fields[i].getType()).toString(),"");
//			}
//			File file = new File("d:\\props.property");
//			file.createNewFile();
//			styleProps.store(new FileOutputStream(file), "");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void main(String ... args)
//	{
//		System.out.println("Hello");
//	}
}
