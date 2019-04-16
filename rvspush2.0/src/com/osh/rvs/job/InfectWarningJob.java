package com.osh.rvs.job;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.entity.PeriodsEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.InfectMapper;
import com.osh.rvs.mapper.push.OperatorMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class InfectWarningJob implements Job {

	private static final String TITLE_JIG = "以下治具未被点检已经超期，请确认！\n"
			+ "序号\t管理编号\t所在课室\t使用工位\n";
	private static final String TITLE_DEV = "以下设备未被点检已经超期，请确认！\n"
			+ "序号\t管理编号\t所在课室\t使用工位\n";
	private static final String TITLE_E = "以下校验品即将超期或已经超期未送校验，请确认！\n"
			+ "序号\t管理编号\t品名\t型号\t过期日期\t校验机构名称\n";
	public static Logger _log = Logger.getLogger("InfectWarningJob");
	// 按照日期的所在点检周期信息
	private static Map<String, PeriodsEntity> periodsOfDate = new HashMap<String, PeriodsEntity>();

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar today = Calendar.getInstance();

		_log.info("InfectWarningJob: " + jobKey + " executing at " + today);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		if (RvsUtils.isHoliday(today.getTime(), conn)) {
			// 休日不发送
			_log.info("Enjoy Holiday！");
			return;
		}

		checkExpiredInfection(today, conn);
		checkExpiredExternal(today, conn);

		if (conn != null) {
			conn.close();
		}
		conn = null;

	}

	public synchronized static PeriodsEntity getPeriodsOfDate(String dayString,
			SqlSession conn){
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
			periodsOfDate.clear();

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date today = cal.getTime();

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
			cal.setTime(today);
			cal.set(Calendar.DATE, 1);
			neo.setStartOfMonth(cal.getTime());

			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DATE, -1);
			neo.setEndOfMonth(cal.getTime());

			// 半期开始终了
			cal.setTime(today);
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
				}
				if (neo.getStartOfMonth().equals(old.getStartOfMonth())) {
					neo.setExpireOfMonth(old.getExpireOfMonth());
				}
				if (neo.getStartOfHMonth().equals(old.getStartOfHMonth())) {
					neo.setExpireOfHMonth(old.getExpireOfHMonth());
				}
				if (neo.getStartOfHbp().equals(old.getStartOfHbp())) {
					neo.setExpireOfHbp(old.getExpireOfHbp());
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
				// TEMP
				Calendar tempExpireOfMonthOfJig = Calendar.getInstance();
				tempExpireOfMonthOfJig.set(Calendar.DAY_OF_MONTH, 15);
				neo.setExpireOfMonthOfJig(tempExpireOfMonthOfJig.getTime());
				// neo.setExpireOfMonthOfJig(expireDate);
			}
			if (neo.getExpireOfMonthOfJig() == null) {
				Date expireDate = new Date(neo.getStartOfMonth().getTime() - 1);

				cond.put("date", expireDate);
				// 月5天宽限
				cond.put("interval", 6);
				expireDate = hMapper.addWorkdays(cond);
				neo.setExpireOfMonthOfJig(expireDate);
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

			periodsOfDate.put(dayString, neo);
		}
		return neo;
	}

	/**
	 * 发邮件
	 * 
	 * @param today
	 * @param conn
	 */
	private void checkExpiredInfection(Calendar today, SqlSession conn) {

		String todayString = DateUtil.toString(today.getTime(), DateUtil.ISO_DATE_PATTERN);
		PeriodsEntity periodsEntity = getPeriodsOfDate(todayString, conn);
		InfectMapper mapper = conn.getMapper(InfectMapper.class);
		OperatorMapper omapper = conn.getMapper(OperatorMapper.class);

		List<Map<String, Object>> expiredInfects = null;

		Date dToday = today.getTime();

		try {
			if (dToday.after(periodsEntity.getExpireOfMonthOfJig())) {

				expiredInfects = mapper.getExpiredTools(periodsEntity);
	
				_log.info("未点检治具(Arrival Next) Count: " + expiredInfects.size());

				unchecked2Mail(TITLE_JIG, "超过时限未点检治具一览", expiredInfects, omapper);
			}

			Map<String, Object> periodsMap = new HashMap<String, Object> ();
			if (dToday.after(periodsEntity.getExpireOfWeek())) {
				periodsMap.put("startDate", periodsEntity.getStartOfWeek());
				periodsMap.put("endDate", periodsEntity.getEndOfWeek());
				periodsMap.put("cycle_type", 2); // 周
				expiredInfects = mapper.getExpiredDevices(periodsMap);
				_log.info("未点检设备工具月(周点)表单  Count: " + expiredInfects.size());
				unchecked2Mail(TITLE_DEV, "超过时限未点检设备工具一览", expiredInfects, omapper);
			}

			if (dToday.after(periodsEntity.getExpireOfMonth())) {
				periodsMap.put("startDate", periodsEntity.getStartOfMonth());
				periodsMap.put("endDate", periodsEntity.getEndOfMonth());
				periodsMap.put("cycle_type", 3); // 月
				expiredInfects = mapper.getExpiredDevices(periodsMap);
				_log.info("未点检设备工具月表单  Count: " + expiredInfects.size());
				unchecked2Mail(TITLE_DEV, "超过时限未点检设备工具一览", expiredInfects, omapper);
			}
	
			if (dToday.after(periodsEntity.getExpireOfHbp())) {
				periodsMap.put("startDate", periodsEntity.getStartOfHbp());
				periodsMap.put("endDate", periodsEntity.getEndOfHbp());
				periodsMap.put("cycle_type", 4); // 半期
				expiredInfects = mapper.getExpiredDevices(periodsMap);
				_log.info("未点检设备工具半期表单  Count: " + expiredInfects.size());
				unchecked2Mail(TITLE_DEV, "超过时限未点检设备工具一览", expiredInfects, omapper);
			}

		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} finally {
		}
	}

	private void unchecked2Mail(String contentTitle, String mailTitle, List<Map<String, Object>> expiredInfects,
			OperatorMapper omapper) throws UnsupportedEncodingException {
		String materails4Mail = contentTitle;
		
		int i = 0;
		String curManager = null;

		for (Map<String, Object> expiredInfect : expiredInfects) { // 对每个超期的维修对象
			// 邮件内容
			if (expiredInfect.get("manager_operator_id") == null) continue;
			String man = expiredInfect.get("manager_operator_id").toString();
			if (curManager == null || !man.equals(curManager)) {
				if (curManager == null) {
					curManager = man;
					continue;
				}
				OperatorEntity manager = omapper.getOperatorByID(curManager);
				curManager = man;
				i=0;

				if (isEmpty(manager.getEmail())) {
					_log.error("管理者无邮件地址!");
					continue;
				}
				Collection<InternetAddress> toIas = new ArrayList<InternetAddress>();
				toIas.add(new InternetAddress( manager.getEmail(), manager.getName()));

				Collection<InternetAddress> ccIas = RvsUtils.getMailIas("infect.unchecked.cc", null);

				// 邮件
				_log.info(materails4Mail);
				MailUtils.sendMail(toIas, ccIas, mailTitle,  materails4Mail);

				materails4Mail = contentTitle;
			}
			String sline = "" + (i+1) + "##spare##"
					+ (String)expiredInfect.get("manage_code") + "##spare##"
					+ expiredInfect.get("section_name") + "##spare##"
					+ expiredInfect.get("process_code") + "##spare##"
					;
			materails4Mail += sline.replaceAll("##spare##", "\t") + "\n" ;
			i++;
		}
	}
	/**
	 * 发邮件
	 * 
	 * @param today
	 * @param conn
	 */
	private void checkExpiredExternal(Calendar today, SqlSession conn) {

		String todayString = DateUtil.toString(today.getTime(), DateUtil.ISO_DATE_PATTERN);
		PeriodsEntity periodsEntity = getPeriodsOfDate(todayString, conn);

		try {
			if (today.before(periodsEntity.getExpireOfMonth())) {
				return;
			}
			InfectMapper mapper = conn.getMapper(InfectMapper.class);
			List<Map<String, Object>> expiredInfects = mapper.getExpiredExternals();

			_log.info("过期未交付校验品(Arrival Next) Count: " + expiredInfects.size());

			String materails4Mail = TITLE_E;

			int i = 0;

			for (Map<String, Object> expiredInfect : expiredInfects) { // 对每个超期的维修对象
				// 邮件内容 //"序号\t品名\t型号\t过期日期\t校验机构名称\n";
				String sline = "" + (i+1) + "##spare##"
						+ expiredInfect.get("manage_code") + "##spare##"
						+ expiredInfect.get("name") + "##spare##"
						+ expiredInfect.get("model_name") + "##spare##"
						+ expiredInfect.get("available_end_date") + "##spare##"
						+ CommonStringUtil.nullToAlter((String) expiredInfect.get("institution_name"), "（无）")
						;
				materails4Mail += sline.replaceAll("##spare##", "\t") + "\n" ;
				i++;
			}

			// 邮件
			Collection<InternetAddress> toIas = RvsUtils.getMailIas("expired.external.to", conn);
			Collection<InternetAddress> ccIas = RvsUtils.getMailIas("expired.external.cc", conn);

			_log.info(materails4Mail);
			MailUtils.sendMail(toIas, ccIas, "超过时限检查机器校验一览",  materails4Mail);

		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} finally {
		}
	}

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}


	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.MONTH, Calendar.JANUARY);
		today.set(Calendar.DATE, 20);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		InfectWarningJob job = new InfectWarningJob();
		job.checkExpiredInfection(today, conn);
	}
}
