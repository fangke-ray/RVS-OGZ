package com.osh.rvs.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PathConsts {
	public static String BASE_PATH = "";
	public static String REPORT_TEMPLATE = "";
	public static String REPORT = "";
	public static String LOAD_TEMP = "";
	public static String PCS_TEMPLATE = "";
	public static String PCS = "";
	public static String PROPERTIES = "";
	public static String DEVICEINFECTION = "";
	public static String INFECTIONS ="";
	public static String IMAGES ="";

	public static Properties MAIL_CONFIG = new Properties();

	public static Properties POSITION_SETTINGS = new Properties();

	public static Properties SCHEDULE_SETTINGS = new Properties();
	
	public static Properties DIRECTAREA_SETTINGS = new Properties();

	public static void load() {
		try {
			InputStream in = new BufferedInputStream(new
					FileInputStream(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\positionsettings.properties"));
			PathConsts.POSITION_SETTINGS.load(in);
	
			in = new BufferedInputStream(new
					FileInputStream(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\mail.properties"));
			PathConsts.MAIL_CONFIG.load(in);

			in = new BufferedInputStream(new
					FileInputStream(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\schedule.properties"));
			PathConsts.SCHEDULE_SETTINGS.load(in);
			
			in = new BufferedInputStream(new
					FileInputStream(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\directarea.properties"));
			PathConsts.DIRECTAREA_SETTINGS.load(in);

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("--me?");
		}
	}
}
