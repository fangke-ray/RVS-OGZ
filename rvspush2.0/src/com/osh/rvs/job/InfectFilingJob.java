package com.osh.rvs.job;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.CheckedFileStorageEntity;
import com.osh.rvs.entity.PeriodsEntity;
import com.osh.rvs.entity.PositionEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.PositionMapper;
import com.osh.rvs.mapper.statistics.InfectMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;

public class InfectFilingJob implements Job {

	public static Logger _log = Logger.getLogger("InfectWarningJob");
	// 按照日期的所在点检周期信息
	private static Map<String, PeriodsEntity> periodsOfDate = new HashMap<String, PeriodsEntity>();

	protected static JSON json = new JSON();
	static {
		json.setSuppressNull(true);
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar today = Calendar.getInstance();

		_log.info("InfectWarningJob: " + jobKey + " executing at " + today);

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try {
			conn.startManagedSession(false);
			makeOfMonth(today, conn);
			int month = today.get(Calendar.MONTH);
			if (month == Calendar.APRIL || month == Calendar.OCTOBER) {
				makeOfPeriod(today, conn);
			}

			conn.commit();
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				_log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}

		conn = null;

	}

	public synchronized static PeriodsEntity getPeriodsOfDate(String todayString,
			SqlSession conn){
		if (periodsOfDate.containsKey(todayString)) {
			return periodsOfDate.get(todayString);
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

			// 半月开始终了
			cal.setTime(today);
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

			periodsOfDate.put(todayString, neo);
		}
		return neo;
	}

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}
	public static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}

	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.YEAR, 2019);
		today.set(Calendar.MONTH, Calendar.APRIL);
		today.set(Calendar.DATE, 1);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

//		String destPath = "E:\\rvsG\\Infections\\151P";
//
//		File fdestPath = new File(destPath);
//		for (File p : fdestPath.listFiles()) {
//			if (p.isDirectory())
//			for (File f : p.listFiles()) {
//				f.setLastModified(today.getTimeInMillis());
//			}
//		}
		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		InfectFilingJob job = new InfectFilingJob();
		try {
			conn.startManagedSession(false);
			job.makeOfMonth(today, conn);
			int month = today.get(Calendar.MONTH);
			if (month == Calendar.APRIL) { //  || month == Calendar.OCTOBER
				job.makeOfPeriod(today, conn);
			}
			conn.commit();
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				_log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	private void makeOfPeriod(Calendar adjustDate, SqlSessionManager conn) {
		InfectMapper ifMapper = conn.getMapper(InfectMapper.class);
		// 期开始终了时间
		Calendar periodStart = Calendar.getInstance();
		Calendar periodEnd = Calendar.getInstance();

		// 上期底
		periodEnd.setTimeInMillis(adjustDate.getTimeInMillis());
		periodEnd.set(Calendar.DATE, 1);

		// 上期初
		periodStart.setTimeInMillis(adjustDate.getTimeInMillis());
		periodStart.set(Calendar.DATE, 1);
		periodStart.add(Calendar.YEAR, -1);

		String sPeriod = RvsUtils.getBussinessYearString(periodStart); // 147P\QR-B31002-20

		Date dPeriodStart = periodStart.getTime();
		Date dPeriodNextPeriod = periodEnd.getTime();
		periodEnd.add(Calendar.DATE, -1);
		Date dPeriodEnd = periodEnd.getTime();

		PositionMapper pMapper = conn.getMapper(PositionMapper.class);

		// 单独归档
		List<Map<String, Object>> retSingle = ifMapper.getSingleOfPeriod(dPeriodStart, dPeriodNextPeriod);

		// 重复文件
		Set<String> fileNames = new HashSet<String>();

		for (Map<String, Object> ret : retSingle) {
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();
			String check_file_manage_id = "" + ret.get("check_file_manage_id");
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);
			checked_file_storage.setDevices_manage_id("" + ret.get("devices_manage_id"));

			checked_file_storage.setStart_record_date(dPeriodStart);
			checked_file_storage.setFiling_date(dPeriodEnd);
			String storage_file_name = encodeFileNameAsFullchar(ret.get("check_manage_code") + "_" + ret.get("manage_code"));

			storage_file_name = checkStroageFileName(storage_file_name, sPeriod, fileNames);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name("" + ret.get("sheet_file_name"));
			ifMapper.recordFileData(checked_file_storage);

			try {
				makeFileSingle(checked_file_storage);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
		}

		// 按工位归档
		List<Map<String, Object>> retOnPosition = ifMapper.getOnPositionOfPeriod(dPeriodStart, dPeriodNextPeriod);
		_log.info("retOnPosition:" + retOnPosition.size());

		String comparePosition = "";
		String sFileName = "";
		Map<String, List<String>> devicesOfPosition = new HashMap<String, List<String>>();
		Map<String, String> fileNameOfPosition = new HashMap<String, String>();
		List<String> current = new ArrayList<String>();
		String sPosition = "";
		for (Map<String, Object> device : retOnPosition) {
			sPosition = "" + device.get("section_id") + "|" + device.get("position_id") + "|"
					+ device.get("check_file_manage_id") + "|" + device.get("check_manage_code");
			if (!devicesOfPosition.containsKey(sPosition)) {
				if(!isEmpty(comparePosition)) {
					devicesOfPosition.put(comparePosition, current);
					fileNameOfPosition.put(comparePosition, "" + sFileName);
				}
				current = new ArrayList<String>();
				comparePosition = sPosition;
			}
			sFileName = "" + device.get("sheet_file_name");
			current.add("" + device.get("devices_manage_id"));
		}
		if (!devicesOfPosition.containsKey(sPosition)) {
			if(!isEmpty(comparePosition)) {
				devicesOfPosition.put(comparePosition, current);
				fileNameOfPosition.put(comparePosition, "" + sFileName);
			}
		}

		_log.info("devicesOfPosition:" + devicesOfPosition.size());
		for (String iPosition : devicesOfPosition.keySet()) {
			List<String> lDevices = devicesOfPosition.get(iPosition);
			String[] cutter = iPosition.split("\\|");
			String sProcessCode = pMapper.getPositionWithSectionByID(cutter[0], cutter[1]);
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();

			checked_file_storage.setSection_id(cutter[0]);
			checked_file_storage.setPosition_id(cutter[1]);

			checked_file_storage.setStart_record_date(dPeriodStart);
			checked_file_storage.setFiling_date(dPeriodEnd);
			String storage_file_name = encodeFileNameAsFullchar(cutter[3] + "_" + sProcessCode + "_" + sPeriod);

			storage_file_name = checkStroageFileName(storage_file_name, sPeriod, fileNames);
			String check_file_manage_id = "" + cutter[2];
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name(fileNameOfPosition.get(iPosition));
			for (String lDevice : lDevices) {
				checked_file_storage.setDevices_manage_id("" + lDevice);
				 ifMapper.recordFileData(checked_file_storage);
			}
			try {
				makeFileGroup(checked_file_storage, lDevices);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
			_log.info(cutter[3] + sProcessCode + ">>" + CommonStringUtil.joinBy(";", lDevices.toArray(new String[lDevices.size()])));
		}

		// 按工程归档
		List<Map<String, Object>> retOnLine = ifMapper.getOnLineOfPeriod(dPeriodStart, dPeriodNextPeriod);
		_log.info("retOnLine:" + retOnLine.size());

		String compareLine = "";
		sFileName = "";
		Map<String, List<String>> devicesOfLine = new HashMap<String, List<String>>();
		Map<String, String> fileNameOfLine = new HashMap<String, String>();
		current = new ArrayList<String>();
		String sLine = "";
		for (Map<String, Object> device : retOnLine) {
			sLine = "" + device.get("section_id") + "|" + device.get("line_id") + "|"
					+ device.get("check_file_manage_id") + "|" + device.get("check_manage_code");
			if (!devicesOfLine.containsKey(sLine)) {
				if(!isEmpty(compareLine)) {
					devicesOfLine.put(compareLine, current);
					fileNameOfLine.put(compareLine, "" + sFileName);
				}
				current = new ArrayList<String>();
				compareLine = sLine;
			}
			sFileName = "" + device.get("sheet_file_name");
			current.add("" + device.get("devices_manage_id"));
		}
		if (!devicesOfLine.containsKey(sLine)) {
			if(!isEmpty(compareLine)) {
				devicesOfLine.put(compareLine, current);
				fileNameOfLine.put(compareLine, "" + sFileName);
			}
		}

		_log.info("devicesOfLine:" + devicesOfLine.size());
		for (String iLine : devicesOfLine.keySet()) {
			List<String> lDevices = devicesOfLine.get(iLine);
			String[] cutter = iLine.split("\\|");
			String sLineName = pMapper.getLineWithSectionByID(cutter[0], cutter[1]);
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();

			checked_file_storage.setSection_id(cutter[0]);
			checked_file_storage.setLine_id(cutter[1]);

			checked_file_storage.setStart_record_date(dPeriodStart);
			checked_file_storage.setFiling_date(dPeriodEnd);
			String storage_file_name = encodeFileNameAsFullchar(cutter[3] + "_" + sLineName + "_" + sPeriod);

			storage_file_name = checkStroageFileName(storage_file_name, sPeriod, fileNames);
			String check_file_manage_id = "" + cutter[2];
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name(fileNameOfLine.get(iLine));
			for (String lDevice : lDevices) {
				checked_file_storage.setDevices_manage_id("" + lDevice);
				 ifMapper.recordFileData(checked_file_storage);
			}
			try {
				makeFileGroup(checked_file_storage, lDevices);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
			_log.info(cutter[3] + sLineName + ">>" + CommonStringUtil.joinBy(";", lDevices.toArray(new String[lDevices.size()])));
		}
	}

	private void makeOfMonth(Calendar adjustDate, SqlSessionManager conn) {

		InfectMapper ifMapper = conn.getMapper(InfectMapper.class);
		// 月开始终了时间
		Calendar monthStart = Calendar.getInstance();
		Calendar monthEnd = Calendar.getInstance();

		// 上月底
		monthEnd.setTimeInMillis(adjustDate.getTimeInMillis());
		monthEnd.set(Calendar.DATE, 1);
		monthEnd.add(Calendar.DATE, -1);

		// 上月初
		monthStart.setTimeInMillis(monthEnd.getTimeInMillis());
		monthStart.set(Calendar.DATE, 1);

		String sPeriod = RvsUtils.getBussinessYearString(monthStart); // 147P\QR-B31002-20
		String sMonth = monthStart.get(Calendar.MONTH) + 1 + "月";

		Date dMonthStart = monthStart.getTime();
		Date dMonthEnd = monthEnd.getTime();

		// 上月底 -> 本月初
		monthEnd.add(Calendar.DATE, 1);
		Date dNextMonth = monthEnd.getTime();

		// 重复文件
		Set<String> fileNames = new HashSet<String>();

		// 单独归档
		List<Map<String, Object>> retSingle = ifMapper.getSingleOfMonth(dMonthStart, dNextMonth);

		for (Map<String, Object> ret : retSingle) {
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();
			String check_file_manage_id = "" + ret.get("check_file_manage_id");
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);
			checked_file_storage.setDevices_manage_id("" + ret.get("devices_manage_id"));

			checked_file_storage.setStart_record_date(dMonthStart);
			checked_file_storage.setFiling_date(dMonthEnd);
			String storage_file_name = checkStroageFileName
					(ret.get("check_manage_code") + "_" + ret.get("manage_code"), sPeriod + sMonth, fileNames);

			storage_file_name = encodeFileNameAsFullchar(storage_file_name);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name("" + ret.get("sheet_file_name"));
			ifMapper.recordFileData(checked_file_storage);

			try {
				makeFileSingle(checked_file_storage);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
		}

		fileNames.clear();

		// 按工位归档
		List<Map<String, Object>> retOnPosition = ifMapper.getOnPositionOfMonth(dMonthStart, dNextMonth);
		_log.info("retOnPosition:" + retOnPosition.size());

		String comparePosition = "";
		String sFileName = "";
		Map<String, List<String>> devicesOfPosition = new HashMap<String, List<String>>();
		Map<String, String> fileNameOfPosition = new HashMap<String, String>();
		List<String> current = new ArrayList<String>();
		String sPosition = "";
		for (Map<String, Object> device : retOnPosition) {
			sPosition = "" + device.get("section_id") + "|" + device.get("position_id") + "|"
					+ device.get("check_file_manage_id") + "|" + device.get("check_manage_code");
			Object oSpecialized = device.get("specialized");
			if (oSpecialized != null) sPosition += ("|" + oSpecialized);
			if (!devicesOfPosition.containsKey(sPosition)) {
				if(!isEmpty(comparePosition)) {
					devicesOfPosition.put(comparePosition, current);
					fileNameOfPosition.put(comparePosition, "" + sFileName);
				}
				current = new ArrayList<String>();
				comparePosition = sPosition;
			}
			sFileName = "" + device.get("sheet_file_name");
			current.add("" + device.get("devices_manage_id"));
		}
		if (!devicesOfPosition.containsKey(sPosition)) {
			if(!isEmpty(comparePosition)) {
				devicesOfPosition.put(comparePosition, current);
				fileNameOfPosition.put(comparePosition, "" + sFileName);
			}
		}

		PositionMapper pMapper = conn.getMapper(PositionMapper.class);

		_log.info("devicesOfPosition:" + devicesOfPosition.size());
		for (String iPosition : devicesOfPosition.keySet()) {
			List<String> lDevices = devicesOfPosition.get(iPosition);
			String[] cutter = iPosition.split("\\|");
			String sProcessCode = pMapper.getPositionWithSectionByID(cutter[0], cutter[1]);
			if (sProcessCode == null) {
				PositionEntity pEntity = pMapper.getPositionByID(cutter[1]);
				if (pEntity != null) {
					sProcessCode = "(未知部门) " + pEntity.getProcess_code() + " " + pEntity.getName();
				}
			}
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();

			checked_file_storage.setSection_id(cutter[0]);
			checked_file_storage.setPosition_id(cutter[1]);

			checked_file_storage.setStart_record_date(dMonthStart);
			checked_file_storage.setFiling_date(dMonthEnd);
			String storage_file_name = cutter[3] + "_" + sProcessCode + "_" + sPeriod + sMonth;
			
			storage_file_name = checkStroageFileName(encodeFileNameAsFullchar(storage_file_name), sPeriod, fileNames);
			if (cutter.length > 4) checked_file_storage.setSpecialized(cutter[4]);

			String check_file_manage_id = "" + cutter[2];
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name(fileNameOfPosition.get(iPosition));
			for (String lDevice : lDevices) {
				checked_file_storage.setDevices_manage_id("" + lDevice);
				ifMapper.recordFileData(checked_file_storage);
			}
			try {
				makeFileGroup(checked_file_storage, lDevices);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
			_log.info(cutter[3] + sProcessCode + ">>" + CommonStringUtil.joinBy(";", lDevices.toArray(new String[lDevices.size()])));
		}

		fileNames.clear();

		// 按工程归档
		List<Map<String, Object>> retOnLine = ifMapper.getOnLineOfMonth(dMonthStart, dNextMonth);
		_log.info("retOnLine:" + retOnLine.size());

		String compareLine = "";
		sFileName = "";
		Map<String, List<String>> devicesOfLine = new HashMap<String, List<String>>();
		Map<String, String> fileNameOfLine = new HashMap<String, String>();
		current = new ArrayList<String>();
		String sLine = "";
		for (Map<String, Object> device : retOnLine) {
			sLine = "" + device.get("section_id") + "|" + device.get("line_id") + "|"
					+ device.get("check_file_manage_id") + "|" + device.get("check_manage_code");
			Object oSpecialized = device.get("specialized");
			if (oSpecialized != null) sLine += ("|" + oSpecialized);
			if (!devicesOfLine.containsKey(sLine)) {
				if(!isEmpty(compareLine)) {
					devicesOfLine.put(compareLine, current);
					fileNameOfLine.put(compareLine, "" + sFileName);
				}
				current = new ArrayList<String>();
				compareLine = sLine;
			}
			sFileName = "" + device.get("sheet_file_name");
			current.add("" + device.get("devices_manage_id"));
		}
		if (!devicesOfLine.containsKey(sLine)) {
			if(!isEmpty(compareLine)) {
				devicesOfLine.put(compareLine, current);
				fileNameOfLine.put(compareLine, "" + sFileName);
			}
		}

		_log.info("devicesOfLine:" + devicesOfLine.size());
		for (String iLine : devicesOfLine.keySet()) {
			List<String> lDevices = devicesOfLine.get(iLine);
			String[] cutter = iLine.split("\\|");
			String sLineName = pMapper.getLineWithSectionByID(cutter[0], cutter[1]);
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();

			checked_file_storage.setSection_id(cutter[0]);
			checked_file_storage.setLine_id(cutter[1]);

			checked_file_storage.setStart_record_date(dMonthStart);
			checked_file_storage.setFiling_date(dMonthEnd);
			String storage_file_name = cutter[3] + "_" + sLineName + "_" + sPeriod + sMonth;
			storage_file_name = checkStroageFileName(encodeFileNameAsFullchar(storage_file_name), sPeriod, fileNames);
			if (cutter.length > 4) checked_file_storage.setSpecialized(cutter[4]);

			String check_file_manage_id = "" + cutter[2];
			checked_file_storage.setCheck_file_manage_id(check_file_manage_id);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setTemplate_file_name(fileNameOfLine.get(iLine));
			for (String lDevice : lDevices) {
				checked_file_storage.setDevices_manage_id("" + lDevice);
				 ifMapper.recordFileData(checked_file_storage);
			}
			try {
				makeFileGroup(checked_file_storage, lDevices);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
			_log.info(cutter[3] + sLineName + ">>" + CommonStringUtil.joinBy(";", lDevices.toArray(new String[lDevices.size()])));
		}

	}

	private String encodeFileNameAsFullchar(String storage_file_name) {
		// \\ : * ? " < > |
		if (storage_file_name.indexOf('/') >= 0) {
			storage_file_name = storage_file_name.replaceAll("/", "／");
		}
		if (storage_file_name.indexOf('\\') >= 0) {
			storage_file_name = storage_file_name.replaceAll("\\\\", "＼");
		}
		if (storage_file_name.indexOf(':') >= 0) {
			storage_file_name = storage_file_name.replaceAll(":", "：");
		}
		if (storage_file_name.indexOf('*') >= 0) {
			storage_file_name = storage_file_name.replaceAll("*", "＊");
		}
		if (storage_file_name.indexOf('?') >= 0) {
			storage_file_name = storage_file_name.replaceAll("?", "？");
		}
		if (storage_file_name.indexOf('<') >= 0) {
			storage_file_name = storage_file_name.replaceAll("<", "＜");
		}
		if (storage_file_name.indexOf('>') >= 0) {
			storage_file_name = storage_file_name.replaceAll(">", "＞");
		}
		if (storage_file_name.indexOf('|') >= 0) {
			storage_file_name = storage_file_name.replaceAll("\\|", "｜");
		}
		return storage_file_name;
	}

	/**
	 * 对应多个同名文件
	 * @param stroageFileName
	 * @param sPeriod
	 * @param fileNames
	 * @return
	 */
	private String checkStroageFileName(String stroageFileName, String sPeriod, Set<String> fileNames) {
		String sRet = stroageFileName + "_" + sPeriod;
		if (fileNames.contains(sRet)) {
			sRet = getCount(stroageFileName, sPeriod, 2, fileNames);
		}
		fileNames.add(sRet);

		return sRet;
	}

	private String getCount(String stroageFileName, String sPeriod, int count,
			Set<String> fileNames) {
		String sRet = stroageFileName + "_" + count + "_" + sPeriod;
		if (fileNames.contains(sRet)) {
			sRet = getCount(stroageFileName, sPeriod, ++count, fileNames);
		}
		return sRet;
	}

	private static final String MAKE_URL = "http://localhost:8080/rvsG2/filingdownload.do?method=make";
	// 单独归档
	@SuppressWarnings("static-access")
	private void makeFileSingle(CheckedFileStorageEntity checked_file_storage) throws IOException {
		// 要求主工程建立文件
		try {
			String encodedEntity = java.net.URLEncoder.encode(json.encode(checked_file_storage), "UTF-8");
			String sDeviceId = checked_file_storage.getDevices_manage_id();
			String destUrl = MAKE_URL + "&entity="+ encodedEntity+"&sDeviceId=" + sDeviceId;
			_log.info("destUrl=" + destUrl);
			URL url = new URL(destUrl);
			url.getQuery();
			URLConnection urlconn = url.openConnection();
			HttpURLConnection hUrlconn = (HttpURLConnection) urlconn;

			hUrlconn.setDoOutput(true);
			hUrlconn.setRequestMethod("POST");
			hUrlconn.setRequestProperty("Accept-Charset", "utf-8");
			hUrlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf8");
			// hUrlconn.setRequestProperty("Content-Length", String.valueOf(parameterBuffer.length()));

			hUrlconn.setReadTimeout(30000); // 等返回
			hUrlconn.connect();
			hUrlconn.getContent();
		} catch (Exception e) {
			_log.error("Failed", e);
		}
	}

	// 集体归档
	@SuppressWarnings("static-access")
	private void makeFileGroup(
			CheckedFileStorageEntity checked_file_storage, List<String> lDevices) throws IOException {

		// 要求主工程建立文件
		try {
			String encodedEntity = java.net.URLEncoder.encode(json.encode(checked_file_storage), "UTF-8");
			String encodedDeviceList = json.encode(lDevices);
			String destUrl = MAKE_URL + "&entity="+
					encodedEntity+"&encodedDeviceList=" + encodedDeviceList;
			_log.info("destUrl=" + destUrl);
			URL url = new URL(destUrl);
			url.getQuery();
			URLConnection urlconn = url.openConnection();
			HttpURLConnection hUrlconn = (HttpURLConnection) urlconn;

			hUrlconn.setDoOutput(true);
			hUrlconn.setRequestMethod("POST");
			hUrlconn.setRequestProperty("Accept-Charset", "utf-8");
			hUrlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf8");
			// hUrlconn.setRequestProperty("Content-Length", String.valueOf(parameterBuffer.length()));

			hUrlconn.setReadTimeout(30000); // 等返回
			hUrlconn.connect();
			hUrlconn.getContent();
		} catch (Exception e) {
			_log.error("Failed", e);
		}
	}
}
