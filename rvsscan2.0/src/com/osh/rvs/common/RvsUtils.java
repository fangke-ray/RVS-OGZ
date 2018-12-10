package com.osh.rvs.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

public class RvsUtils {

	protected static final Logger logger = Logger.getLogger("Production");

	/**
	 * 用时(分钟)转换为小时+分格式。
	 * @param minutes 分钟数
	 */
	public static String formatMinutes(int minutes) {
		int hours = minutes / 60;
		int minutesRemain = minutes % 60;
		return (hours == 0 ? "" : "" + hours + "小时") + minutesRemain + "分";
	}

	public static String regfy(String modelName) {
		if (modelName == null)
			return null;
		else {
			// UTF-8全角三字节
			return "^" + modelName.replaceAll("[\\{\\(\\[｛【「『（\\}\\)\\]｝】」』）]", ".{1,3}").replaceAll("[  　]", "[  　]{0,1}") + "$";
		}
	}

	public static String get2Char(String num) {
		if (num == null) return null;
		return num.length() == 1 ? "0"+num : num;
	}

	private static Map<String, String> overLineCache = new HashMap<String, String>();

	public static String getZeroOverLine(String model_name, String category_name, LoginData user, String spprocess_code) throws Exception {
		// 取得用户信息
		String process_code;
		if (spprocess_code != null) {
			process_code = spprocess_code;
		} else {
			process_code = user.getProcess_code();
		}

		String sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "." + model_name);

		if (sOverline == null && category_name != null) {
			sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "." + category_name);
		}
		if (sOverline == null) {
			sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "._default");
		}
		if (sOverline == null) {
			logger.warn(process_code + "工位的标准作业时间没有定义。");
			sOverline = "-1";
		}

		return sOverline;
	}
	
	public static String getLevelOverLine(String model_name, String category_name, String level, LoginData user, String spprocess_code) throws Exception {
		// 取得用户信息
		String process_code;
		if (spprocess_code != null) {
			process_code = spprocess_code;
		} else {
			process_code = user.getProcess_code();
		}

		String cacheKey = "M[" + model_name + "]L[" + level + "]P[" + process_code + "]";
		if (overLineCache.containsKey(cacheKey)) {
			return overLineCache.get(cacheKey);
		}

		String sOverline = "";

		if (level != null) {
			boolean enType = false;
			if (level.matches("\\d+")) {
				level = CodeListUtils.getValue("material_level", level, "0");
				if (level.length() > 1 && !level.startsWith("S")) {
					enType = true;
				}
			}

			sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level + "." + process_code + "." + model_name);
	
			if (sOverline == null && enType) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level.substring(0, 1) + process_code + "." + model_name);
			}
			if (sOverline == null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "." + model_name);
			}
			if (sOverline == null && category_name != null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level + "." + process_code + "." + category_name);
			}
			if (sOverline == null && category_name != null && enType) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level.substring(0, 1) + "." + process_code + "." + category_name);
			}
			if (sOverline == null && category_name != null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "." + category_name);
			}
			if (sOverline == null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level + "." + process_code + "._default");
			}
			if (sOverline == null && enType) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline." + level.substring(0, 1) + "." + process_code + "._default");
			}
			if (sOverline == null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("overline.0." + process_code + "._default");
			}

		} else {
			sOverline = getZeroOverLine(model_name, category_name, user, process_code);
		}

		overLineCache.put(cacheKey, sOverline);

		return sOverline;
	}

	public static String getWaitingflow(String section, LoginData user, String spprocess_code) {
		// 取得用户信息
		String process_code = "0";
		if (spprocess_code != null) {
			process_code = spprocess_code;
		} else if (user != null) {
			process_code = user.getProcess_code();
		}

		String cacheKey = "S[" + section + "]P[" + process_code + "]";
		if (overLineCache.containsKey(cacheKey)) {
			return overLineCache.get(cacheKey);
		}

		String sOverline = "";

		if (section != null) {
			sOverline = PathConsts.POSITION_SETTINGS.getProperty("waitingflow." + section.substring(section.length() - 1) + "." + process_code);
			if (sOverline == null) {
				sOverline = PathConsts.POSITION_SETTINGS.getProperty("waitingflow.0." + process_code);
			}
		} else {
			sOverline = PathConsts.POSITION_SETTINGS.getProperty("waitingflow.0." + process_code);
		}
		if (sOverline == null) {
			sOverline = "0";
		}

		overLineCache.put(cacheKey, sOverline);

		return sOverline;
	}

	public static Map<String, String> reverseLinkedMap(Map<String, String> oldMap) {
		Map<String, String> newMap = new LinkedHashMap<String, String>();

		List<String> tmpKeyList = new ArrayList<String>();
		List<String> tmpValueList = new ArrayList<String>();
		for (String key : oldMap.keySet()) {
			tmpKeyList.add(key);
			tmpValueList.add(oldMap.get(key));
		}

		for (int i = tmpKeyList.size() - 1; i >= 0 ; i--) {
			newMap.put(tmpKeyList.get(i), tmpValueList.get(i));
		}
		return newMap;
	}
	
	public static String arrivalPlanDate2String(Date arrival_plan_date, String sPattern) {
		String sArrivalPlanDate = DateUtil.toString(arrival_plan_date, "yyyy");
		if (sArrivalPlanDate == null) {
			return "-";
		} else if ("9999".equals(sArrivalPlanDate)) {
			return "未定";
		} else {
			return DateUtil.toString(arrival_plan_date, sPattern);
		}
	}


	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat dfE = new SimpleDateFormat("M月", new Locale("EN"));
	private static SimpleDateFormat dfS = new SimpleDateFormat("M/d");

	public static int getMonthAxisInBussinessYear(Date adjustDate, boolean toEnd, boolean toNow, List<String> years, List<String> months) {
		if (adjustDate == null || years == null || months == null) {
			return -1;
		}

		Calendar cal = Calendar.getInstance();
		int nowYear = cal.get(Calendar.YEAR);
		int nowMonth = cal.get(Calendar.MONTH);

		cal.setTime(adjustDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int adjustYear = cal.get(Calendar.YEAR);
		int adjustMonth = cal.get(Calendar.MONTH);

		if (adjustMonth < 3) {
			adjustYear--;
		}

		int currentIndex = -1;
		for (int i = 3; i < 15; i++) {
			int iYear = (adjustYear + (i >= 12 ? 1 : 0));
			years.add("" + iYear);
			months.add("" + (i % 12 + 1));
			if (toNow && nowYear == iYear && nowMonth == i % 12) return i - 3;
			if (i % 12 == adjustMonth) {
				if (toEnd) currentIndex = i - 3;
				else return i - 3;
			}
		}
		return currentIndex;
	}

	public static int getMonthPastHalfYear(Date adjustDate, List<String> years, List<String> months) {
		if (adjustDate == null || years == null || months == null) {
			return -1;
		}

		Calendar cal = Calendar.getInstance();

		cal.setTime(adjustDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		cal.add(Calendar.MONTH, -6);

		int currentIndex = -1;
		for (int i = 0; i < 6; i++) {
			years.add("" + cal.get(Calendar.YEAR));
			months.add("" + (cal.get(Calendar.MONTH)+1));
			cal.add(Calendar.MONTH, 1);
		}
		return currentIndex;
	}

	/**
	 * 取得月内的星期分割
	 * 
	 * @param adjustDate 参照日期
	 * @param toEnd 包含月内参照日期之后的结果
	 * @param startDates 返回：坐标区间开始日
	 * @param endDates 返回：坐标区间结束日
	 */
	public static void getWeekAxisInMonth(Date adjustDate, boolean toEnd, List<Date> startDates, List<Date> endDates) {
		if (adjustDate == null || startDates == null || endDates == null) {
			return;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(adjustDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int adjustMonth = cal.get(Calendar.MONTH);
		int adjustYear = cal.get(Calendar.YEAR);

		cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

		do {
			Calendar startCal = Calendar.getInstance();
			startCal.setTimeInMillis(cal.getTimeInMillis());
			startCal.add(Calendar.DATE, -6);
			if (toEnd) {
				if (startCal.get(Calendar.MONTH) != adjustMonth && cal.get(Calendar.MONTH) != adjustMonth) {
					return;
				}
			} else {
				if (startCal.getTime().after(adjustDate)) {
					return;
				}
			}
			if (startCal.get(Calendar.MONTH) != adjustMonth) {
				startCal.set(Calendar.YEAR, adjustYear);
				startCal.set(Calendar.MONTH, adjustMonth);
				startCal.set(Calendar.DATE, 1);
			}
			if (cal.get(Calendar.MONTH) != adjustMonth) {
				cal.set(Calendar.DATE, 1);
				cal.add(Calendar.DATE, -1);
			}

			startDates.add(startCal.getTime());
			endDates.add(cal.getTime());

			cal.add(Calendar.DATE, 7);
		} while (true);
	}
	
	
	public static int getMonthAndWeekAxisInBussinessYear(Date adjustDate, boolean toEnd, boolean toNow, List<String> axisTexts) {
		return getMonthAndWeekAxisInBussinessYear(adjustDate, toEnd, toNow, axisTexts ,null, null);
	}
	
	public static int getMonthAndWeekAxisInBussinessYear(Date adjustDate, boolean toEnd, boolean toNow, List<String> axisTexts, List<String> years, List<String> months) {
		if (adjustDate == null || axisTexts == null) {
			return -1;
		}

		int retCd = -1;
		df.setTimeZone(TimeZone.getDefault());
		dfE.setTimeZone(TimeZone.getDefault());
		dfS.setTimeZone(TimeZone.getDefault());

		Calendar cal = Calendar.getInstance();
		String nowYear = "" + cal.get(Calendar.YEAR);
		String nowMonth = "" + (cal.get(Calendar.MONTH) + 1);

		cal.setTime(adjustDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		String currentYear = "" + cal.get(Calendar.YEAR);
		String currentMonth = "" + (cal.get(Calendar.MONTH) + 1);

		String adjustDateText = dfS.format(adjustDate.getTime());

		if (years == null)
			years = new ArrayList<String>();
		if (months == null)
			months = new ArrayList<String>();

		getMonthAxisInBussinessYear(adjustDate, toEnd, false, years, months);

		for (int i = 0; i < years.size(); i++) {
			if (years.get(i).equals(currentYear) && months.get(i).equals(currentMonth)) {
				// 本月

				List<Date> start_Dates = new ArrayList<Date>();
				List<Date> end_Dates = new ArrayList<Date>();
				getWeekAxisInMonth(adjustDate, toEnd, start_Dates, end_Dates);

				for (int j = 0; j < start_Dates.size(); j++) {
					Date startDate = start_Dates.get(j);
					Date endDate = end_Dates.get(j);

					String endDateText = dfS.format(endDate.getTime());
					if (endDateText.equals(adjustDateText)) {
						retCd = axisTexts.size();
					}

					axisTexts.add(dfS.format(startDate.getTime()) + "～" + endDateText);
				}

			} else {
				if (toNow && years.get(i).equals(nowYear) && Integer.parseInt(months.get(i)) > Integer.parseInt(nowMonth)) {
					break;
				}
				// 非本月
				Calendar startDate = Calendar.getInstance();
				startDate.set(Calendar.YEAR, Integer.parseInt(years.get(i)));
				startDate.set(Calendar.MONTH, Integer.parseInt(months.get(i)) - 1);
				startDate.set(Calendar.DATE, 1);

				Calendar endDate = Calendar.getInstance();
				endDate.set(Calendar.YEAR, Integer.parseInt(years.get(i)));
				endDate.set(Calendar.MONTH, Integer.parseInt(months.get(i)));
				endDate.set(Calendar.DATE, 1);
				endDate.add(Calendar.DATE, -1);

				axisTexts.add(dfE.format(startDate.getTime()));
			}
		}
		for (int i=0; i < months.size();i++) {
			String month = months.get(i);
			if (month.length() == 1) {
				month = "0" + month;
				months.set(i, month);
			}
		}
		return retCd;
	}

	public static int getMonthAxisInHalfBussinessYear(Date adjustDate, boolean toEnd, List<String> years, List<String> months) {
		if (adjustDate == null || years == null || months == null) {
			return -1;
		}

		Calendar cal = Calendar.getInstance();

//		int nowMonth = cal.get(Calendar.MONTH);

		cal.setTime(adjustDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int adjustYear = cal.get(Calendar.YEAR);
		int adjustMonth = cal.get(Calendar.MONTH);

		if (adjustMonth < 3) {
			adjustYear--;
		}

		int currentIndex = -1;
		if (adjustMonth >=3 && adjustMonth < 9) {
			for (int i = 3; i < 9; i++) {
				int iYear = (adjustYear + (i >= 12 ? 1 : 0));
				years.add("" + iYear);
				months.add("" + (i % 12 + 1));
//				if (toNow && nowYear == iYear && nowMonth == i % 12) return i - 3;
				if (i % 12 == adjustMonth) {
					if (toEnd) currentIndex = i - 3;
					else 
					return i - 3;
				}
			}
		} else {
			for (int i = 9; i < 15; i++) {
				int iYear = (adjustYear + (i >= 12 ? 1 : 0));
				years.add("" + iYear);
				months.add("" + (i % 12 + 1));
//				if (toNow && nowYear == iYear && nowMonth == i % 12) return i - 3;
				if (i % 12 == adjustMonth) {
					if (toEnd) currentIndex = i - 3;
					else 
					return i - 9;
				}
			}
		}

		return currentIndex;
	}
	
	public static String getBussinessYearString(Calendar date) {
		int adjustYear = date.get(Calendar.YEAR);
		int adjustMonth = date.get(Calendar.MONTH);
		
		if (adjustMonth < 3) {
			adjustYear--;
		}
		return (adjustYear - 1867) + "P";
	}

	public static String getBussinessHalfYearString(Calendar date) {
		int adjustMonth = date.get(Calendar.MONTH);
		
		if (adjustMonth < 3 || adjustMonth >= 9) {
			return getBussinessYearString(date) + "B";
		} else {
			return getBussinessYearString(date) + "A";
		}
	}	

	/**
	* 取得半期起始时间
	*/
	public static Date getBussinessHalfStartDate(Calendar date) {
		int adjustYear = date.get(Calendar.YEAR);
		int adjustMonth = date.get(Calendar.MONTH);

		if (adjustMonth < 3) {
			adjustYear--;
			adjustMonth = Calendar.OCTOBER;
		} else if (adjustMonth >= 9) {
			adjustMonth = Calendar.OCTOBER;
		} else {
			adjustMonth = Calendar.APRIL;
		}

		Calendar ret = Calendar.getInstance();
		ret.set(Calendar.YEAR, adjustYear);
		ret.set(Calendar.MONTH, adjustMonth);
		ret.set(Calendar.DATE, 1);
		ret.set(Calendar.HOUR_OF_DAY, 0);
		ret.set(Calendar.MINUTE, 0);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);

		return ret.getTime();
	}

	/**
	 * 获取一个月中第一天的日期
	 * 
	 * @param year
	 * @param month
	 * @return strDate
	 */
	public static Date getStartDate(String year, String month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.valueOf(year), Integer.valueOf(month) - 1, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一个月中最后一天的日期
	 * 
	 * @param year
	 * @param month
	 * @return strDate
	 */
	public static Date getEndDate(String year, String month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.valueOf(year), Integer.valueOf(month), 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private static Map<String, String> dryTimeCache = new HashMap<String, String>();

	public static String getDryTime(String model_name, String category_name, String line_name) {

		String cacheKey = "M[" + model_name + "]L[" + line_name + "]";
		if (dryTimeCache.containsKey(cacheKey)) {
			return dryTimeCache.get(cacheKey);
		}

		String sDryTime = PathConsts.SCHEDULE_SETTINGS.getProperty("dryingTime." + line_name + "." + model_name);

		if (sDryTime == null && category_name != null) {
			sDryTime = PathConsts.SCHEDULE_SETTINGS.getProperty("dryingTime." + line_name + "." + category_name);
		}

		if (sDryTime == null) {
			sDryTime = "0";
		}

		dryTimeCache.put(cacheKey, sDryTime);

		return sDryTime;
	}

	public static void initAll() {
		overLineCache.clear();
		dryTimeCache.clear();
	}
}
