package com.osh.rvs.job;

import java.io.File;
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

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.ZipUtility;
import com.osh.rvs.entity.CheckedFileStorageEntity;
import com.osh.rvs.entity.PositionEntity;
import com.osh.rvs.mapper.push.PositionMapper;
import com.osh.rvs.mapper.statistics.InfectMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class InfectFilingJob implements Job {

	public static Logger _log = Logger.getLogger("InfectWarningJob");

	private static JSON json = new JSON();
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

		Set<String> madeSet = new HashSet<String>();

		try {
			conn.startManagedSession(false);
			// 清理全部待点检记录
			clearCheckStatusWait(conn);
			makeOfMonth(today, madeSet, conn);
			int month = today.get(Calendar.MONTH);
			if (month == Calendar.APRIL || month == Calendar.OCTOBER) {
				makeOfPeriod(today, madeSet, conn);
				makeOfJig(today, madeSet, conn);
			}

			conn.commit();

			collect2ShareDir(today, madeSet);
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

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}
	private static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}

	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.YEAR, 2019);
		today.set(Calendar.MONTH, Calendar.DECEMBER);
		today.set(Calendar.DATE, 2);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		MAKE_URL = "http://localhost:8080/rvsG2/filingdownload.do?method=make";

//		String destPath = "E:\\rvsG\\Infections\\151P";
//
//		File fdestPath = new File(destPath);
//		for (File p : fdestPath.listFiles()) {
//			if (p.isDirectory())
//			for (File f : p.listFiles()) {
//				f.setLastModified(today.getTimeInMillis());
//			}
//		}

		PathConsts.BASE_PATH = "E:\\rvsG";
		PathConsts.INFECTIONS = "\\Infections";
		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		Set<String> madeSet = new HashSet<String>();

		InfectFilingJob job = new InfectFilingJob();
		try {
			conn.startManagedSession(false);
			job.clearCheckStatusWait(conn);
			job.makeOfMonth(today, madeSet, conn);
			int month = today.get(Calendar.MONTH);
//			if (month == Calendar.APRIL) { //  || month == Calendar.OCTOBER
//				job.makeOfPeriod(today, madeSet, conn);
//				job.makeOfJig(today, madeSet, conn);
//			}
			conn.commit();
			job.collect2ShareDir(today, madeSet);
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

	private void collect2ShareDir(Calendar adjustDate, Set<String> madeSet) {
		String BACKUP_DISK = PathConsts.BASE_PATH.substring(0, 1);
		String descBaseDir = BACKUP_DISK + "://RVS_BACKUP//INFECT//";
		Calendar collectMonth = Calendar.getInstance();
		collectMonth.setTime(adjustDate.getTime());
		collectMonth.add(Calendar.MONTH, -1);
		String bussinessYearString = RvsUtils.getBussinessYearString(collectMonth);
		String packPathString = descBaseDir + bussinessYearString + DateUtil.toString(collectMonth.getTime(), "MM月");
		File packPath = new File(packPathString);
		if (!packPath.exists()) {
			packPath.mkdirs();
		}

		for (String madeFile : madeSet) {
			File srcFile = new File(PathConsts.BASE_PATH + PathConsts.INFECTIONS + "//" + bussinessYearString + "//" + madeFile + ".pdf");
			if (srcFile.exists()) {
				try {
					FileUtils.copyFileToDirectory(srcFile, packPath);
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
			}
		}

		ZipUtility.zipper(packPathString, packPathString + ".zip", "GBK");
		try {
			FileUtils.deleteDirectory(packPath);
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}

	private void clearCheckStatusWait(SqlSessionManager conn) {
		InfectMapper ifMapper = conn.getMapper(InfectMapper.class);
		ifMapper.removeCheckStatusWait();
	}


	private void makeOfPeriod(Calendar adjustDate, Set<String> madeSet, SqlSessionManager conn) {
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

		// 重复文件
		Set<String> fileNames = new HashSet<String>();

		// 单独归档
		List<Map<String, Object>> retSingle = ifMapper.getSingleOfPeriod(dPeriodStart, dPeriodNextPeriod);

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
				madeSet.add(ret.get("check_manage_code") + "/" + storage_file_name);
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
				current = new ArrayList<String>();
				comparePosition = sPosition;
				sFileName = "" + device.get("sheet_file_name");
				devicesOfPosition.put(comparePosition, current);
				fileNameOfPosition.put(comparePosition, "" + sFileName);
			}
			current.add("" + device.get("devices_manage_id"));
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
				madeSet.add(cutter[3] + "/" + storage_file_name);
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
				current = new ArrayList<String>();
				compareLine = sLine;
				sFileName = "" + device.get("sheet_file_name");
				devicesOfLine.put(compareLine, current);
				fileNameOfLine.put(compareLine, "" + sFileName);
			}
			current.add("" + device.get("devices_manage_id"));
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
				madeSet.add(cutter[3] + "/" + storage_file_name);
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

	private void makeOfMonth(Calendar adjustDate, Set<String> madeSet, SqlSessionManager conn) {

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
				madeSet.add(ret.get("check_manage_code") + "/" + storage_file_name);
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
				current = new ArrayList<String>();
				comparePosition = sPosition;
				sFileName = "" + device.get("sheet_file_name");
				devicesOfPosition.put(comparePosition, current);
				fileNameOfPosition.put(comparePosition, "" + sFileName);
			}
			current.add("" + device.get("devices_manage_id"));
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
				madeSet.add(cutter[3] + "/" + storage_file_name);
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
				current = new ArrayList<String>();
				compareLine = sLine;
				sFileName = "" + device.get("sheet_file_name");
				devicesOfLine.put(compareLine, current);
				fileNameOfLine.put(compareLine, "" + sFileName);
			}
			current.add("" + device.get("devices_manage_id"));
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
				madeSet.add(cutter[3] + "/" + storage_file_name);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}
			_log.info(cutter[3] + sLineName + ">>" + CommonStringUtil.joinBy(";", lDevices.toArray(new String[lDevices.size()])));
		}

	}

	private void makeOfJig(Calendar adjustDate, Set<String> madeSet, SqlSessionManager conn) {
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

		// 治具归档
		List<Map<String, Object>> retJig = ifMapper.getJig(dPeriodStart, dPeriodNextPeriod);
		Map<String, List<Map<String, Object>>> retJigGroup = new HashMap<String, List<Map<String, Object>>>();

		String current_p_and_o = null;
		List<Map<String, Object>> retJigByPO = null;
		for (Map<String, Object> ret : retJig) {
			String p_and_o = ret.get("section_id") + "_" + ret.get("position_id") + "_" + ret.get("responsible_operator_id");
			if (current_p_and_o == null) {
				retJigByPO = new ArrayList<Map<String, Object>>();
				current_p_and_o = p_and_o;
			} else if (!current_p_and_o.equals(p_and_o)) {
				retJigGroup.put(current_p_and_o, retJigByPO);
				retJigByPO = new ArrayList<Map<String, Object>>();
				current_p_and_o = p_and_o;
			}
			retJigByPO.add(ret);
		}
		if (retJigByPO != null) {
			retJigGroup.put(current_p_and_o, retJigByPO);
		}

		// 重复文件
		Set<String> fileNames = new HashSet<String>();

		for (String p_and_o_id : retJigGroup.keySet()) {
			List<Map<String, Object>> rets = retJigGroup.get(p_and_o_id);
			Map<String, Object> ret0 = rets.get(0);
			CheckedFileStorageEntity checked_file_storage = new CheckedFileStorageEntity();

			checked_file_storage.setStart_record_date(dPeriodStart);
			checked_file_storage.setFiling_date(dPeriodEnd);
			String storage_file_name = "QF0601-5专用工具定期清点保养记录_"
					+ encodeFileNameAsFullchar(ret0.get("process_code") + "_" 
							+ nullToAlter(ret0.get("name"), "（责任者未定）"));

			storage_file_name = checkStroageFileName(storage_file_name, sPeriod, fileNames);

			checked_file_storage.setStorage_file_name(storage_file_name);
			checked_file_storage.setCheck_file_manage_id("00000000000");
			checked_file_storage.setSection_id("" + ret0.get("section_id"));
			checked_file_storage.setLine_id("" + ret0.get("line_id"));
			checked_file_storage.setPosition_id("" + ret0.get("position_id"));

			List<String> jigList = new ArrayList<String>();
			for(Map<String, Object> ret : rets) {
				String jig_manage_id = "" + ret.get("jig_manage_id");
				checked_file_storage.setDevices_manage_id(jig_manage_id);
				jigList.add(jig_manage_id);
				ifMapper.recordFileData(checked_file_storage);
			}

			try {
				makeFileJig(checked_file_storage, current_p_and_o, jigList);
				madeSet.add("QF0601-5/" + storage_file_name);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
				continue;
			}

		}
	}

	private String nullToAlter(Object object, String alter) {
		if (object == null) return alter;
		return CommonStringUtil.nullToAlter("" + object, alter);
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

	private static String MAKE_URL = "http://localhost:8080/rvs/filingdownload.do?method=make"; // rvsG2
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
			String encodedDeviceList = java.net.URLEncoder.encode(json.encode(lDevices), "UTF-8");
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

	@SuppressWarnings("static-access")
	private void makeFileJig(
			CheckedFileStorageEntity checked_file_storage, String operator_id, List<String> lJigs) throws IOException {

		// 要求主工程建立文件
		try {
			String encodedEntity = java.net.URLEncoder.encode(json.encode(checked_file_storage), "UTF-8");
			String encodedDeviceList = json.encode(lJigs);
			String destUrl = MAKE_URL + "&entity="+
					encodedEntity+"&encodedDeviceList=" + encodedDeviceList + "&sJigOperaterId=" + operator_id;
			_log.info("destUrl=" + destUrl);
			URL url = new URL(destUrl);
			url.getQuery();
			URLConnection urlconn = url.openConnection();
			HttpURLConnection hUrlconn = (HttpURLConnection) urlconn;

			hUrlconn.setDoOutput(true);
			hUrlconn.setRequestMethod("POST");
			hUrlconn.setRequestProperty("Accept-Charset", "utf-8");
			hUrlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf8");

			hUrlconn.setReadTimeout(30000); // 等返回
			hUrlconn.connect();
			hUrlconn.getContent();
		} catch (Exception e) {
			_log.error("Failed", e);
		}
	}
}
