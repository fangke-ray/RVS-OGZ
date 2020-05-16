package com.osh.rvs.common;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.OperatorMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class RvsUtils {

	protected static final Logger logger = Logger.getLogger("RvsUtil");

	public static boolean isHoliday(Date date, SqlSession conn) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		boolean conncreated = false;
		try {
			if (conn == null) {
				SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
				conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
				conncreated = true;
			}

			HolidayMapper dao = conn.getMapper(HolidayMapper.class);
			boolean logged = dao.existsHoliday(date);
			// 是双休日
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				return logged ? false : true;
			} else {
				return logged ? true : false;
			}
		} catch(Exception e) {
			return false;
		} finally {
			if (conncreated && conn != null) conn.close();
		}
	}

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

	private static Map<String, String> overLineCache = new HashMap<String, String>();

	public static String getZeroOverLine(String model_name, String category_name, String process_code) throws Exception {
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
	
	public static String getLevelOverLine(String model_name, String category_name, String level, String spprocess_code) throws Exception {
		// 取得用户信息
		String process_code;
		if (spprocess_code != null) {
			process_code = spprocess_code;
		} else {
			return "-1";
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
			sOverline = getZeroOverLine(model_name, category_name, process_code);
		}

		overLineCache.put(cacheKey, sOverline);

		return sOverline;
	}

	public static String getWaitingflow(String section, String spprocess_code) {
		// 取得用户信息
		String process_code = "0";
		if (spprocess_code != null) {
			process_code = spprocess_code;
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
	  * @param cal
	  * @return
	  */
	public static Date getMonthStartDate(Calendar cal) {
		
		Calendar calenar = Calendar.getInstance();
		calenar.setTime(cal.getTime());
		calenar.set(Calendar.DATE, 1);
		calenar.set(Calendar.HOUR_OF_DAY, 0);
		calenar.set(Calendar.MINUTE, 0);
		calenar.set(Calendar.SECOND, 0);
		calenar.set(Calendar.MILLISECOND, 0);
		
		return calenar.getTime();
	}

	 /**
	  * 获取一个月中最后一天的日期
	  * @param cal
	  * @return
	  */
	public static Date getMonthEndDate(Calendar cal) {
		int lastDay = cal.getActualMaximum(Calendar.DATE);

		Calendar calenar = Calendar.getInstance();
		calenar.setTime(cal.getTime());
		calenar.set(Calendar.DATE, lastDay);
		calenar.set(Calendar.HOUR_OF_DAY, 0);
		calenar.set(Calendar.MINUTE, 0);
		calenar.set(Calendar.SECOND, 0);
		calenar.set(Calendar.MILLISECOND, 0);

		return calenar.getTime();
	}

	public static SqlSession getTempConn() {
		logger.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}

	public static SqlSessionManager getTempWritableConn() {
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}


	/**
	 * 当前日期摆成一个数值（2016/10/21日启动后）
	 * @return
	 */
	public static int getTodayCounts() {
		long timeDiff = new Date().getTime() - 1477008000000l;
		int dateDiff = (int) (timeDiff / 86400000l);
		return dateDiff;
	}

	/**
	 * 取得信息
	 * @param property
	 * @param conn
	 * @return
	 */
	public static Collection<InternetAddress> getMailIas(String property,
			SqlSession conn) {
		return getMailIas(property, conn, null, RvsConsts.DEPART_REPAIR, null);
	}
	public static Collection<InternetAddress> getMailIas(String property,
			SqlSession conn, String line_id) {
		return getMailIas(property, conn, line_id, RvsConsts.DEPART_REPAIR);
	}
	public static Collection<InternetAddress> getMailIas(String property,
			SqlSession conn, String line_id, Integer department) {
		return getMailIas(property, conn, line_id, department, null);
	}
	public static Collection<InternetAddress> getMailIas(String property,
			SqlSession conn, String line_id, List<String> senderIds) {
		return getMailIas(property, conn, line_id, RvsConsts.DEPART_REPAIR, senderIds);
	}
	public static Collection<InternetAddress> getMailIas(String property,
			SqlSession conn, String line_id, Integer department, List<String> senderIds) {
		List<InternetAddress> ias = new ArrayList<InternetAddress>();

		// 找到所有经理以上人员
		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);

		String propertyTo = PathConsts.MAIL_CONFIG.getProperty(property);
		if (isEmpty(propertyTo)) {
			return null;
		}

		for (String rever : propertyTo.split(";")) {
			if (rever.indexOf("<") <= 0) {
				OperatorEntity cond = new OperatorEntity();
				if ("M".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_MANAGER);
					cond.setDepartment(department);
				} else if ("?L".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id(line_id);
				} else if ("QL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000011");
				} else if ("DL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000012");
				} else if ("NL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000013");
				} else if ("CL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000014");
				} else if ("EL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000050");
				} else if ("FDL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000060");
				} else if ("FCL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000061");
				} else if ("PL".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000070");
				} else if ("LML".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_LINELEADER);
					cond.setLine_id("00000000054");
				} else if ("DM".equals(rever)) {
					cond.setRole_id(RvsConsts.ROLE_DEVICEMANAGER);
				} else {
					continue;
				}

				List<OperatorEntity> lOp = oMapper.searchOperator(cond);

				for (OperatorEntity op : lOp) {
					String email = op.getEmail();
					if (senderIds != null) {
						senderIds.add(op.getOperator_id());
					}
					if (!CommonStringUtil.isEmpty(email)) {
						try {
							ias.add(new InternetAddress(email, op.getName()));
						} catch (UnsupportedEncodingException e) {
							logger.error("Add reciever " + op.getName(), e);
						}
					}
				}
			} else {
				String name = rever.replaceAll("<.*>", "").trim();
				String address = rever.replaceAll(".*<(.*)>", "$1").trim();
				if (!isEmpty(address)) {
					try {
						ias.add(new InternetAddress(address, name));
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		return ias;
	}

	public static void initAll() {
		overLineCache.clear();
	}

	public static String getProperty(Properties properties, String property, String... items) {
		if (properties == null) return null;
		String value = properties.getProperty(property, "");
		for (int i=0; i < items.length; i ++) {
			value = value.replaceAll("\\{" + i +  "\\}", items[i]);
		}
		return value;
	}
}
