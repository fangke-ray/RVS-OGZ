package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.infect.PeriodsEntity;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.master.HolidayMapper;

import framework.huiqing.common.util.copy.DateUtil;

public class CheckResultService {

	public static final int TYPE_ITEM_FREE = 0;
	public static final int TYPE_ITEM_DAY = 1;
	public static final int TYPE_ITEM_WEEK = 2;
	public static final int TYPE_ITEM_MONTH = 3;
	public static final int TYPE_ITEM_PERIOD = 4;
	public static final int TYPE_ITEM_YEAR = 5;

	static final Integer TYPE_FILED_WEEK_OF_MONTH = 6;
	static final Integer TYPE_FILED_MONTH = 7;
	static final Integer TYPE_FILED_YEAR = 8;
	static final String ELECTRIC_IRON_FILE_A = "00000000100";
	static final String ELECTRIC_IRON_FILE_B = "00000000101";
	static final String ELECTRIC_IRON_FILE_B2 = "00000000102";
	static final String TORSION_FILE_A = "00000000105";
	static final String TORSION_FILE_B = "00000000106";
	static final String TORSION_FILE_C = "00000000107";
	static final String TORSION_FILE_D = "00000000108";
	static final String TORSION_FILE_E = "00000000109";
	static final String TORSION_FILE_F = "00000000110";
	static final String TORSION_FILE_G = "00000000111";
	static final String TORSION_FILE_H = "00000000112";

	private static Map<String, Date[][]> weekEndOfMonth = new HashMap<String, Date[][]>();
	// 按照日期的所在点检周期信息
	private static Map<String, PeriodsEntity> periodsOfDate = new HashMap<String, PeriodsEntity>();

	static Calendar getStartOfPeriod(Calendar adjustCal) {
		String sdate = RvsUtils.getBussinessStartDate(adjustCal);
		Date date = DateUtil.toDate(sdate, DateUtil.DATE_PATTERN);
		Calendar ret = Calendar.getInstance();
		ret.setTime(date);
		return ret;
	}

	static int getAxis(Calendar adjustCal, Integer itemType, Integer fileType) {
		if (itemType == TYPE_ITEM_MONTH && fileType == TYPE_FILED_YEAR) {
			int month = adjustCal.get(Calendar.MONTH);
			if (month < 3) {
				return month + 9;
			} else {
				return month - 3;
			}
		} else if (itemType == TYPE_ITEM_PERIOD && fileType == TYPE_FILED_YEAR) {
			int month = adjustCal.get(Calendar.MONTH);
			if (month < 3) {
				return 1;
			} else if (month >= 9) {
				return 1;
			} else {
				return 0;
			}
		} else if (itemType == TYPE_ITEM_YEAR && fileType == TYPE_FILED_YEAR) {
			return 0;
		} else if (itemType == TYPE_ITEM_DAY && fileType == TYPE_FILED_MONTH) {
			return adjustCal.get(Calendar.DATE) - 1;
		} else if (itemType == TYPE_ITEM_WEEK && fileType == TYPE_FILED_WEEK_OF_MONTH) {
			int ret = 0;
			// java里的第一周如果没工作日则不算第一周,同理月末跨越下一周但没有工作日则算上一月的最后周
			Date[][] wesOfMonth = getWeekEndsOfMonth(adjustCal);
			for (int j = 0; j < wesOfMonth.length; j++) {
				Date[] wes = wesOfMonth[j];
				if (adjustCal.getTimeInMillis() <= wes[1].getTime()) {
					ret = j;
					break;
				}
			}

			return ret;
		}
		return 0;
	}

	static int getMaxAxis(Integer itemType, Integer fileType) {
		if (fileType == TYPE_FILED_YEAR) {
			if (itemType == TYPE_ITEM_MONTH) {
				return 11;
			} else if (itemType == TYPE_ITEM_PERIOD) {
				return 1;
			} else if (itemType == TYPE_ITEM_YEAR) {
				return 0;
			}
		} else if (fileType == TYPE_FILED_MONTH) {
			if (itemType == TYPE_ITEM_DAY) {
				return 30;
			} else {
				return 0;
			}
		} else if (fileType == TYPE_FILED_WEEK_OF_MONTH) {
			if (itemType == TYPE_ITEM_WEEK) {
				return 4;
			} else {
				return 0;
			}
		}
//		} else if (type == TYPE_HALF_MONTH_OF_YEAR) {
//			return 23;

		return -1;
	}

	static Date[] getDayOfAxis(Calendar start, int axisDiff, int axisType, Integer fileType) {
		Date[] retDs = new Date[2];
		Calendar jCal = Calendar.getInstance(); 
		jCal.setTimeInMillis(start.getTimeInMillis());
		jCal.set(Calendar.HOUR_OF_DAY, 0);
		jCal.set(Calendar.MINUTE, 0);
		jCal.set(Calendar.SECOND, 0);
		jCal.set(Calendar.MILLISECOND, 0);

		if (fileType == TYPE_FILED_MONTH) {
			if (axisType == TYPE_ITEM_DAY) {
				int iMonthC = jCal.get(Calendar.MONTH);
				jCal.add(Calendar.DATE, axisDiff);
				int iMonthN = jCal.get(Calendar.MONTH);
				if (iMonthN != iMonthC) {
					jCal.set(Calendar.YEAR, 1900);
				}
				retDs[0] = jCal.getTime();
				retDs[1] = jCal.getTime();
			} else {
				retDs[0] = jCal.getTime();
				jCal.add(Calendar.MONTH, 1);
				jCal.add(Calendar.DATE, -1);
				retDs[1] = jCal.getTime();
			}
		} else if (fileType == TYPE_FILED_WEEK_OF_MONTH) {
			if (axisType == TYPE_ITEM_WEEK) {
				Date[][] weekEnds = getWeekEndsOfMonth(start);
				if (axisDiff >= weekEnds.length) {
					jCal.set(Calendar.YEAR, 1900);
					retDs[0] = jCal.getTime();
					retDs[1] = jCal.getTime();
				} else {
					return weekEnds[axisDiff];
				}
			} else {
				Date[][] weekEnds = getWeekEndsOfMonth(start);

				retDs[0] = weekEnds[0][0];

				retDs[1] = weekEnds[weekEnds.length - 1][1];
			}
		} else if (fileType == TYPE_FILED_YEAR) {
			if (axisType == TYPE_ITEM_MONTH) {
				jCal.add(Calendar.MONTH, axisDiff);
				jCal.set(Calendar.DATE, 1);
				retDs[0] = jCal.getTime();
				jCal.add(Calendar.MONTH, 1);
				jCal.add(Calendar.DATE, -1);
				retDs[1] = jCal.getTime();
			} else if (axisType == TYPE_ITEM_PERIOD) {
				jCal.add(Calendar.MONTH, 6 * axisDiff);
				jCal.set(Calendar.DATE, 1);
				retDs[0] = jCal.getTime();
				jCal.add(Calendar.MONTH, 6);
				jCal.add(Calendar.DATE, -1);
				retDs[1] = jCal.getTime();
			} else {
				jCal.set(Calendar.DATE, 1);
				retDs[0] = jCal.getTime();
				jCal.add(Calendar.MONTH, 12);
				jCal.add(Calendar.DATE, -1);
				retDs[1] = jCal.getTime();
			}
		}

//		} else if (axisType == TYPE_HALF_MONTH_OF_YEAR) {
//			int month = axisDiff / 2;
//			int half = axisDiff % 2;
//			if (half == 0) {
//				jCal.add(Calendar.MONTH, month);
//				jCal.set(Calendar.DATE, 1);
//				retDs[0] = jCal.getTime();
//				jCal.set(Calendar.DATE, 15);
//				retDs[1] = jCal.getTime();
//			} else {
//				jCal.add(Calendar.MONTH, month);
//				jCal.set(Calendar.DATE, 16);
//				retDs[0] = jCal.getTime();
//				jCal.set(Calendar.DATE, 1);
//				jCal.add(Calendar.MONTH, 1);
//				jCal.add(Calendar.DATE, -1);
//				retDs[1] = jCal.getTime();
//			}

		return retDs;
	}

	/**
	 * 取得或计算时间范围
	 * @param dayString
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public synchronized static PeriodsEntity getPeriodsOfDate(String dayString,
			SqlSession conn) throws Exception {
		if (periodsOfDate.containsKey(dayString)) {
			return periodsOfDate.get(dayString);
		}

		HolidayMapper hMapper = conn.getMapper(HolidayMapper.class);
		// 建立日的开始结束时间
		PeriodsEntity old = null;
		PeriodsEntity neo = new PeriodsEntity();
		for (String key : periodsOfDate.keySet()) {
			old = periodsOfDate.get(key);
		}
		synchronized (periodsOfDate) {
//			periodsOfDate.clear();

			Date theDay = DateUtil.toDate(dayString, DateUtil.ISO_DATE_PATTERN);
			Calendar cal = Calendar.getInstance();
			cal.setTime(theDay);

			// 周开始终了
			int week = cal.get(Calendar.DAY_OF_WEEK);
			if (week == Calendar.SUNDAY) {
				neo.setEndOfWeek(cal.getTime());

				cal.add(Calendar.DATE, -6);
				neo.setStartOfWeek(cal.getTime());
			} else {
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				neo.setStartOfWeek(cal.getTime());

				cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				cal.add(Calendar.DATE, 1);
				neo.setEndOfWeek(cal.getTime());
			}

			// 月开始终了
			cal.setTime(theDay);
			cal.set(Calendar.DATE, 1);
			neo.setStartOfMonth(cal.getTime());

			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DATE, -1);
			neo.setEndOfMonth(cal.getTime());

			// 半月开始终了
			cal.setTime(theDay);
			if (cal.get(Calendar.DATE) <= 15) {
				neo.setStartOfHMonth(neo.getStartOfMonth());
				cal.set(Calendar.DATE, 15);
				neo.setEndOfHMonth(cal.getTime());
			} else {
				cal.set(Calendar.DATE, 16);
				neo.setStartOfHMonth(cal.getTime());
				neo.setEndOfHMonth(neo.getEndOfMonth());
			}
			
			// 半期开始终了
			cal.setTime(theDay);
			int nowMonth = cal.get(Calendar.MONTH);
			if (nowMonth < Calendar.APRIL) {
				cal.add(Calendar.YEAR, -1);
				cal.set(Calendar.MONTH, Calendar.APRIL);
				cal.set(Calendar.DATE, 1);
				neo.setStartOfPeriod(cal.getTime());
				cal.set(Calendar.MONTH, Calendar.OCTOBER);
			} else if (nowMonth >= Calendar.OCTOBER) {
				cal.set(Calendar.MONTH, Calendar.APRIL);
				cal.set(Calendar.DATE, 1);
				neo.setStartOfPeriod(cal.getTime());
				cal.set(Calendar.MONTH, Calendar.OCTOBER);
			} else {
				cal.set(Calendar.MONTH, Calendar.APRIL);
				cal.set(Calendar.DATE, 1);
				neo.setStartOfPeriod(cal.getTime());
			}

			neo.setStartOfHbp(cal.getTime());
			cal.add(Calendar.MONTH, 6);
			cal.add(Calendar.DATE, -1);
			neo.setEndOfHbp(cal.getTime());

			cal.setTimeInMillis(neo.getStartOfPeriod().getTime());
			cal.add(Calendar.MONTH, 12);
			cal.add(Calendar.DATE, -1);
			neo.setEndOfPeriod(cal.getTime());

			Map<String, Object> cond = new HashMap<String, Object>();

			// 周点检限期
			if (old != null) {
				// 如果上一天在同一个区域
				if (neo.getStartOfWeek().equals(old.getStartOfWeek())) {
					neo.setExpireOfWeek(old.getExpireOfWeek());
					neo.setLastOfWeek(old.getLastOfWeek());
				}
				if (neo.getStartOfMonth().equals(old.getStartOfMonth())) {
					neo.setExpireOfMonth(old.getExpireOfMonth());
					neo.setLastOfMonth(old.getLastOfMonth());
				}
				if (neo.getStartOfHMonth().equals(old.getStartOfHMonth())) {
					neo.setExpireOfHMonth(old.getExpireOfHMonth());
				}
				if (neo.getStartOfHbp().equals(old.getStartOfHbp())) {
					neo.setExpireOfHbp(old.getExpireOfHbp());
					neo.setLastOfHbp(old.getLastOfHbp());
				}
				if (neo.getStartOfPeriod().equals(old.getStartOfPeriod())) {
					neo.setExpireOfPeriod(old.getExpireOfPeriod());
				}
			}

			if (neo.getExpireOfWeek() == null) {
				Date expireDate = new Date(neo.getStartOfWeek().getTime() - 1);
				// 往前1天开始算天数
				cond.put("date", expireDate);
				cond.put("interval", 2);
				expireDate = hMapper.addWorkdays(cond);
				if (expireDate.after(neo.getEndOfWeek()) ) {
					// 一周两天都没有的情况
					expireDate.setTime(neo.getStartOfWeek().getTime());
				}
				neo.setExpireOfWeek(expireDate);
			}
			if (neo.getLastOfWeek() == null) {
				Date lastDate = new Date(neo.getEndOfWeek().getTime() + 86400001l);
				// 往前1天开始算天数
				cond.put("date", lastDate);
				cond.put("interval", -1);
				lastDate = hMapper.addWorkdays(cond);
				if (lastDate.before(neo.getStartOfWeek()) ) {
					// 一周两天都没有的情况
					lastDate.setTime(neo.getEndOfWeek().getTime());
				}
				neo.setLastOfWeek(lastDate);
			}
			if (neo.getExpireOfMonth() == null) {
				Date expireDate = new Date(neo.getStartOfMonth().getTime() - 1);

				cond.put("date", expireDate);
				// 月3天宽限
				cond.put("interval", 4);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfMonth(expireDate);
				cond.put("date", expireDate);
				cond.put("interval", 2);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfMonthOfJig(expireDate);
			}
			if (neo.getExpireOfMonthOfJig() == null) {
				Date expireDate = new Date(neo.getStartOfMonth().getTime() - 1);

				cond.put("date", expireDate);
				// 月5天宽限
				cond.put("interval", 6);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfMonthOfJig(expireDate);
			}
			if (neo.getLastOfMonth() == null) {
				Date lastDate = new Date(neo.getEndOfMonth().getTime() + 86400001l);

				cond.put("date", lastDate);
				// 月3天宽限
				cond.put("interval", -1);
				lastDate = hMapper.addWorkdays(cond);
				neo.setLastOfMonth(lastDate);
			}
			if (neo.getExpireOfHMonth() == null) {

				// 半月3天宽限
				if (neo.getStartOfMonth().equals(neo.getStartOfHMonth())) {
					neo.setExpireOfHMonth(neo.getExpireOfMonth());
				} else {
					Date expireDate = new Date(neo.getStartOfHMonth().getTime() - 1);
					cond.put("date", expireDate);
					cond.put("interval", 4);
					expireDate = hMapper.addWorkdays(cond);
					neo.setExpireOfHMonth(expireDate);
				}
			}

			if (neo.getExpireOfHbp() == null) {
				Date expireDate = new Date(neo.getStartOfHbp().getTime() - 1);
				
				cond.put("date", expireDate);
				cond.put("interval", 4);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfHbp(expireDate);
			}
			if (neo.getLastOfHbp() == null) {
				Date lastDate = new Date(neo.getEndOfHbp().getTime() + 86400001l);

				cond.put("date", lastDate);
				// 月3天宽限
				cond.put("interval", -1);
				lastDate = hMapper.addWorkdays(cond);
				neo.setLastOfHbp(lastDate);
			}

			if (neo.getExpireOfPeriod() == null) {
				Date expireDate = new Date(neo.getStartOfPeriod().getTime() - 1);
				
				cond.put("date", expireDate);
				cond.put("interval", 4);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfPeriod(expireDate);
			}

			periodsOfDate.put(dayString, neo);
		}
		return neo;
	}

	static String getNoScale(BigDecimal plainBd) {
		return getNoScale(plainBd.toPlainString());
	}
	static String getNoScale(String plainString) {
		if ("999999.999".equals(plainString)) {
			return "∞";
		}
		return plainString.replaceAll("^(\\-?\\d*\\.\\d*[1-9])0*$", "$1").replaceAll("^(\\-?\\d*)\\.0*$", "$1");
	}
	static String getScale(BigDecimal plainBd, int scale) {
		return plainBd.setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
	}

	/**
	 * 取得当月每星期的开始终了日期
	 * @param adjustDate
	 * @return
	 */
	static Date[][] getWeekEndsOfMonth(Calendar adjustDate) {
		Calendar jCal = Calendar.getInstance();
		jCal.setTimeInMillis(adjustDate.getTimeInMillis());
		jCal.set(Calendar.HOUR_OF_DAY, 0);
		jCal.set(Calendar.MINUTE, 0);
		jCal.set(Calendar.SECOND, 0);
		jCal.set(Calendar.MILLISECOND, 0);

		// 本月第一个周一
		jCal.set(Calendar.DATE, 1);
		int week = jCal.get(Calendar.DAY_OF_WEEK);
		int month = jCal.get(Calendar.MONTH);
		if (week == Calendar.SUNDAY) {
			jCal.add(Calendar.DATE, 1);
		} else if (week == Calendar.MONDAY) {
		} else {
			jCal.add(Calendar.DATE, 9 - week);
		}

		// 如果不在本月范围内,则在上月范围内
		if (adjustDate.getTimeInMillis() < jCal.getTimeInMillis()) {
			Calendar jEndOfLastMonth = Calendar.getInstance();
			jEndOfLastMonth.setTimeInMillis(adjustDate.getTimeInMillis());
			jEndOfLastMonth.set(Calendar.DATE, 1);
			jEndOfLastMonth.add(Calendar.DATE, -1);
			return getWeekEndsOfMonth(jEndOfLastMonth);
		}

		String monthString = DateUtil.toString(jCal.getTime(), "yy-MM");
		if (weekEndOfMonth.containsKey(monthString)) {
			return weekEndOfMonth.get(monthString);
		} else {
			List<Date[]> seLOfWeeks = new ArrayList<Date[]>();
			while (month == jCal.get(Calendar.MONTH)){
				Date[] se = new Date[2];
				se[0] = jCal.getTime();
				jCal.add(Calendar.DATE, 6);
				se[1] = jCal.getTime();
				jCal.add(Calendar.DATE, 1);
				seLOfWeeks.add(se);
			}

			int seLOfWeeksSize = seLOfWeeks.size();
			Date[][] seAOfWeeks = new Date[seLOfWeeksSize][];
			for (int i=0; i<seLOfWeeksSize; i++) {
				seAOfWeeks[i] = seLOfWeeks.get(i);
			}
			weekEndOfMonth.put(monthString, seAOfWeeks);
			return weekEndOfMonth.get(monthString);
		}
	}
}