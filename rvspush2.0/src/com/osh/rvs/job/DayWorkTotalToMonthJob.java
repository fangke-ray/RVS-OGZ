package com.osh.rvs.job;
import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.exceptions.IbatisException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.ZipUtility;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.mapper.push.OperatorMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

@SuppressWarnings("deprecation")
public class DayWorkTotalToMonthJob implements Job {

	private static final String C_SHEET_GLOBAL = "汇总";
	private static final String C_SHEET_DEC = "分解工程";
	private static final String C_SHEET_NS = "NS 工程";
	private static final String C_SHEET_COM = "总组工程";
	private static final String C_SHEET_QUOTE_TEAM = "报价组";
	private static final String C_SHEET_SP_TEAM = "外科镜维修";
	private static final String C_SHEET_QA_TEAM = "品保课";

	private static final String C_SHEET_FDEC = "纤维镜分解";
	private static final String C_SHEET_FCOM = "纤维镜总组";
	private static final String C_SHEET_PERI = "周边维修";
	private static final String C_SHEET_LM = "中小修修理";

	private static final String C_SHEET_LIST = "工作日报";

	private static final int DETAIL_INDEX_DEC = 1;
	private static final int DETAIL_INDEX_NS = 2;
	private static final int DETAIL_INDEX_COM = 3;
	private static final int DETAIL_INDEX_QUOTE_TEAM = 0;
	private static final int DETAIL_INDEX_SP_TEAM = 4;

	private static final int DETAIL_INDEX_FDEC = 5;
	private static final int DETAIL_INDEX_FCOM = 6;
	private static final int DETAIL_INDEX_PERI = 7;
	private static final int DETAIL_INDEX_LM = 8;

	private static final int DETAIL_INDEX_QA_TEAM = 9;

	private static final String[] COL_NAMES = {"A", "B", "C", "D", "E", "F"};
	private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

	public static Logger _log = Logger.getLogger("DayWorkTotalToMonthJob");

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar monthStart = Calendar.getInstance();

		_log.info("DayWorkTotalToMonthJob: " + jobKey + " executing at " + monthStart);

		monthStart.set(Calendar.DATE, 1);
		monthStart.set(Calendar.HOUR_OF_DAY, 0);
		monthStart.set(Calendar.MINUTE, 0);
		monthStart.set(Calendar.SECOND, 0);
		monthStart.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();
		SqlSessionManager connManager = getTempWritableConn();
		
		makeStatistics(monthStart, conn);
		monthlyPcsPack(conn);
//		clearSap(connManager);
		monthlyFilePack(conn, monthStart);
		if (conn != null) {
			conn.close();
		}
		conn = null;
		
		if (connManager != null && connManager.isManagedSessionStarted()) {
			connManager.close();
		}
		connManager = null;

	}

	/**
	 * 
	 */
	private void monthlyPcsPack(SqlSession conn) {

		try {
			Connection connection = conn.getConnection();

			Statement sm = connection.createStatement();

			Calendar cal = Calendar.getInstance();
//			cal.set(Calendar.YEAR, 2015);
//			cal.set(Calendar.MONTH, Calendar.AUGUST);
//			cal.set(Calendar.DATE, 11);

			cal.add(Calendar.MONTH, -3);
			cal.set(Calendar.DATE, 1);

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);

			Calendar end = Calendar.getInstance();

			while (true) {
				String st = df.format(cal.getTime());

				File destDir = new File("d:\\com\\" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1));
				if (!destDir.exists()) {
					destDir.mkdirs();
				}

				if (cal.after(end)) {
					_log.info("after:"  + cal.getTime());
					break;
				}

				String bussinessPack = RvsUtils.getBussinessYearString(cal) + "-" + DateUtil.toString(cal.getTime(), "MM");
				
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DATE, 1);

				String en = df.format(cal.getTime());

				String msqlText ="SELECT " +
						"    m.omr_notifi_no as sorc_no,m.qa_check_time as outline_time " +
						"FROM material m join v_model mdl on m.model_id = mdl.model_id" +
						" where qa_check_time >= '" + st + "' " +
						" and qa_check_time < '"+ en +"' " +
						" and break_back_flg = 0" +
						" and (fix_type = 1 or kind = 06)" 
						+ " order by outline_time ";
				ResultSet q = sm.executeQuery(msqlText);

				String sorcNo = "";
				Date date = null;
				while (q.next()) {
					sorcNo = q.getString(1);
					date = q.getDate(2);

					String subPath = "";
					if (sorcNo== null || sorcNo.length() < 8) // If EndoEye
						subPath = "CELL-" + sorcNo + "________";
					else if (sorcNo.length() == 8)
						subPath = "OMRN-" + sorcNo + "________";
					else 
						subPath = sorcNo;
					String sub8 = subPath.substring(0, 8);
					String fileStart = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + sorcNo + ".zip";
					File sourceFile = new File(fileStart);
					if (!sourceFile.exists()) {
						if (subPath.startsWith("CELL")) {
							subPath = "SAPD-" + sorcNo + "________";
							sub8 = subPath.substring(0, 8);
							fileStart = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + sorcNo + ".zip";
							sourceFile = new File(fileStart);
							if (!sourceFile.exists()) {
								subPath = "SAPD-" + sorcNo + "________";
								sub8 = subPath.substring(0, 8);
								fileStart = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + sorcNo + ".zip";
								sourceFile = new File(fileStart);
							}
						} else
						if (subPath.startsWith("OMRN-")) {
							subPath = "OMRN-" + sorcNo + "________";
							sub8 = subPath.substring(0, 8);
							fileStart = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + sorcNo + ".zip";
							sourceFile = new File(fileStart);
						}
					}
					if (sourceFile.exists()) {
						FileUtils.copyFileToDirectory(sourceFile, destDir);

						File destFile = new File(destDir + "\\" + sorcNo + ".zip");
						if (destFile.exists()) {
							destFile.setLastModified(date.getTime());
						}
					} else {
						System.out.println("没 " + fileStart);
					}
				}

				_log.info(msqlText);
				ZipUtility.zipper(destDir.getAbsolutePath(), PathConsts.BASE_PATH + PathConsts.PCS + "\\_monthly\\" 
						+ bussinessPack + ".zip", "GBK");

			}

			// 建立下月的临时目录
			end.add(Calendar.MONTH, 1);
			String pathString = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(end.getTime(), "YYYYMM");
			File tempPath = new File(pathString);
			if (!tempPath.exists()) {
				tempPath.mkdir();
			}

			// 删除过去的临时目录
			end.add(Calendar.YEAR, -1);
			pathString = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(end.getTime(), "YYYYMM");
			File oldPath = new File(pathString);
			deleteDir(oldPath);

		} catch (IbatisException e) {
			_log.error(e.getMessage(), e);
		} catch (SQLException e1) {
			_log.error(e1.getMessage(), e1);
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		} finally {
		}
	}

	private void monthlyFilePack(SqlSession conn, Calendar monthStart) {
		String descBaseDir = "E://RVS_BACKUP";
		String monthString = DateUtil.toString(monthStart.getTime(), "YYYYMM");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//accept//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//受理//");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//inline//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//投线//");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//schedule//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//计划//");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//shipping//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//出货//");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//weeks//" , 
				descBaseDir + "//RPT//" + monthString + "//周报//", monthStart.getTimeInMillis());

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//wip//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//WIP库存//");

//		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "//works//" + monthString , 
//				descBaseDir + "//RPT//" + monthString + "//受理//");

		copyDirectory(PathConsts.BASE_PATH + PathConsts.REPORT + "//kpi_process//" + monthString , 
				descBaseDir + "//RPT//" + monthString + "//工时//");

		copyDirectoryThursday("D://sqlDump//", 
				descBaseDir + "//DB//" + monthString, monthStart);
	}

	/**
	 * 制作统计记录
	 * @param today
	 * @param conn
	 */
	private void makeStatistics(Calendar monthStart, SqlSession conn) {

		POIFSFileSystem fs;
		HSSFWorkbook book = null;

		try {

			Map<Integer, Boolean> restDays = new HashMap<Integer, Boolean>();
			List<WeekBean> weekBeans = null;
			// 取得本周休假日以及周报分配
			weekBeans = getWeekBeans(monthStart, restDays, conn);

			int weekBeanSize = weekBeans.size();

			// 取得实际操作人员
			OperatorMapper odao = conn.getMapper(OperatorMapper.class);
			List<OperatorEntity> countWorkOperators = odao.getAllActivingOperator(monthStart.getTime());
			Map<String, List<WorkCount>> factWork = new HashMap<String, List<WorkCount>>();

			Map<String, OperatorEntity> operatorOnJobno = new HashMap<String, OperatorEntity>();

			for (OperatorEntity countWorkOperator : countWorkOperators) {
				List<WorkCount> workCountList = new ArrayList<WorkCount>();
				workCountList.add(new WorkCount()); // 当前日用
				for (int i = 0; i < weekBeanSize; i++) { // 各时段统计
					workCountList.add(new WorkCount());
				}
				factWork.put(countWorkOperator.getJob_no(), workCountList);

				operatorOnJobno.put(countWorkOperator.getJob_no(), countWorkOperator);
			}

			// 取得本月工作日报
			List<Map<String, Object>> operatorProcessInMonth = odao.searchOperatorProcessInMonth(monthStart.getTime());

			// 月报文件名
			String monthName = DateUtil.toString(monthStart.getTime(), "MM月");
			String cacheFilename = "月度有效工时统计比率表" + monthName + "汇总(" + RvsUtils.getBussinessHalfYearString(monthStart) + ").xls";
			// 建立月报文件
			String template_path =  PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "月度有效工时统计比率表模板-"+weekBeanSize+"周.xls";
			// String template_path =  "D:\\rvs\\ReportTemplates\\" + "月度有效工时统计比率表模板-"+weekBeanSize+"周.xls";
			String destPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\works\\" + cacheFilename;
			// String destPath = "D:\\rvs\\Reports\\" + DateUtil.toString(monthStart.getTime(), "yyyyMM") + "\\" + cacheFilename;
			FileUtils.copyFile(new File(template_path), new File(destPath));

			fs = new POIFSFileSystem(new FileInputStream(destPath));
			book = new HSSFWorkbook(fs);

			// 一览列表生成
			HSSFSheet listSheet = book.getSheet(C_SHEET_LIST);
			HSSFRow row = null;

			HSSFCellStyle defaultCell = book.createCellStyle();
			defaultCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			defaultCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			defaultCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			defaultCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框

			HSSFCellStyle commentCell = book.createCellStyle(); // 换行
			commentCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			commentCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			commentCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			commentCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			commentCell.setWrapText(true);

			HSSFCellStyle highlightCell = book.createCellStyle(); // 亮色
			highlightCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			highlightCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			highlightCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			highlightCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			highlightCell.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);
			highlightCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			listSheet.setDefaultColumnStyle(0, defaultCell);
			listSheet.setDefaultColumnStyle(1, defaultCell);
			listSheet.setDefaultColumnStyle(2, defaultCell);
			listSheet.setDefaultColumnStyle(3, defaultCell);
			listSheet.setDefaultColumnStyle(4, defaultCell);
			listSheet.setDefaultColumnStyle(5, defaultCell);
			listSheet.setDefaultColumnStyle(6, defaultCell);
			listSheet.setDefaultColumnStyle(7, defaultCell);
			listSheet.setDefaultColumnStyle(8, defaultCell);
			listSheet.setDefaultColumnStyle(9, defaultCell);
			listSheet.setDefaultColumnStyle(10, defaultCell);
			listSheet.setDefaultColumnStyle(11, defaultCell);
			listSheet.setDefaultColumnStyle(12, defaultCell);
			listSheet.setDefaultColumnStyle(13, defaultCell);

			Map<String, String> readCursor = new HashMap<String, String>();
			for (int i = 0; i < operatorProcessInMonth.size(); i++) {
				Map<String, Object> operatorProcess = operatorProcessInMonth.get(i);
				Map<String, Object> next_operatorProcess = null;
				if (i + 1 < operatorProcessInMonth.size()) next_operatorProcess = operatorProcessInMonth.get(i + 1);
				// _log.info(operatorProcess.get("action_date"));
				row = listSheet.createRow(i+1);

				createRowCells(row, operatorProcess, next_operatorProcess, factWork, operatorOnJobno, readCursor, weekBeans, commentCell, highlightCell);
			}
			// 处理最后一条个人记录
			String last_action_date = readCursor.get("action_date");
			String last_job_no = readCursor.get("job_no");
			if (last_action_date != null)
				treatDayOperator(last_action_date, weekBeans, factWork.get(last_job_no));

			// 总计
			HSSFSheet globalSheet = book.getSheet(C_SHEET_GLOBAL);
			// globalSheet
			book.setSheetName(0, monthName + C_SHEET_GLOBAL);

			// W(线长代工时间)
			List<Double> gLeaderWorks = newDoubleList(weekBeanSize); 
			// O(加班工时总计)
			List<Double> gOverWorks = newDoubleList(weekBeanSize); 
			// 直接作业时间(M)
			List<Double> gScanWorks = newDoubleList(weekBeanSize); 
			// M(管理等待时间)
			List<Double> gManageWorks = newDoubleList(weekBeanSize); 
			// H(休息，离开流水线)
			List<Double> gHealthWorks = newDoubleList(weekBeanSize); 
			// 出勤平均人数
			List<Double> gLaboryDays = newDoubleList(weekBeanSize); 
			// 总平均人数
			Integer gWorkers= 0;

			// 明细表
			int iCountSheetDetail = 10;
			// W(线长代工时间)
			List<List<Double>> dtlLeaderWorks = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// O(加班工时总计)
			List<List<Double>> dtlOverWorks = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// 直接作业时间(M)
			List<List<Double>> dtlScanWorks = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// M(管理等待时间)
			List<List<Double>> dtlManageWorks = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// H(休息，离开流水线)
			List<List<Double>> dtlHealthWorks = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// 出勤平均人数
			List<List<Double>> dtlLaboryDays = newDoubleLists(weekBeanSize, iCountSheetDetail); 
			// 总平均人数
			Integer[] dtlWorkers = {0,0,0,0,0,0,0,0,0,0,0};

			// WIP修理同意数
			MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
			List<Integer> gWipAgrees = new ArrayList<Integer>(); 
			for (int i = 0; i < weekBeanSize; i++) {
				gWipAgrees.add(mDao.countWipAgreed(weekBeans.get(i).start_date, weekBeans.get(i).end_date, null));
			}

			for (String job_no : factWork.keySet()) {
				List<WorkCount> factWorkByWeek = factWork.get(job_no);

				OperatorEntity operator = operatorOnJobno.get(job_no);
				String section_id = operator.getSection_id();
				String line_id = operator.getLine_id();
				if (operator.getWork_count_flg() == 1) {
					for (int i = 0; i < weekBeanSize; i++) {
						gScanWorks.set(i, gScanWorks.get(i) + factWorkByWeek.get(i+1).Wtime);
						gManageWorks.set(i, gManageWorks.get(i) + factWorkByWeek.get(i+1).Mtime);
						gHealthWorks.set(i, gHealthWorks.get(i) + factWorkByWeek.get(i+1).Htime);
						gOverWorks.set(i, gOverWorks.get(i) + factWorkByWeek.get(i+1).Otime);
						gLaboryDays.set(i, gLaboryDays.get(i) + factWorkByWeek.get(i+1).Wdays);
						if ("00000000001".equals(section_id)) {
							if ("00000000012".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_DEC).set(i, dtlScanWorks.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_DEC).set(i, dtlManageWorks.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_DEC).set(i, dtlHealthWorks.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_DEC).set(i, dtlOverWorks.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_DEC).set(i, dtlLaboryDays.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000013".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_NS).set(i, dtlScanWorks.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_NS).set(i, dtlManageWorks.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_NS).set(i, dtlHealthWorks.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_NS).set(i, dtlOverWorks.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_NS).set(i, dtlLaboryDays.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000014".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_COM).set(i, dtlScanWorks.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_COM).set(i, dtlManageWorks.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_COM).set(i, dtlHealthWorks.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_COM).set(i, dtlOverWorks.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_COM).set(i, dtlLaboryDays.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000011".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlScanWorks.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlManageWorks.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlHealthWorks.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlOverWorks.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlLaboryDays.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000050".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_SP_TEAM).set(i, dtlScanWorks.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_SP_TEAM).set(i, dtlManageWorks.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_SP_TEAM).set(i, dtlHealthWorks.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_SP_TEAM).set(i, dtlOverWorks.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_SP_TEAM).set(i, dtlLaboryDays.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000060".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_FDEC).set(i, dtlScanWorks.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_FDEC).set(i, dtlManageWorks.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_FDEC).set(i, dtlHealthWorks.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_FDEC).set(i, dtlOverWorks.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_FDEC).set(i, dtlLaboryDays.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000061".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_FCOM).set(i, dtlScanWorks.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_FCOM).set(i, dtlManageWorks.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_FCOM).set(i, dtlHealthWorks.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_FCOM).set(i, dtlOverWorks.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_FCOM).set(i, dtlLaboryDays.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000070".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_PERI).set(i, dtlScanWorks.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_PERI).set(i, dtlManageWorks.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_PERI).set(i, dtlHealthWorks.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_PERI).set(i, dtlOverWorks.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_PERI).set(i, dtlLaboryDays.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Wdays);
							} else if ("00000000054".equals(line_id)) {
								dtlScanWorks.get(DETAIL_INDEX_LM).set(i, dtlScanWorks.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Wtime);
								dtlManageWorks.get(DETAIL_INDEX_LM).set(i, dtlManageWorks.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Mtime);
								dtlHealthWorks.get(DETAIL_INDEX_LM).set(i, dtlHealthWorks.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Htime);
								dtlOverWorks.get(DETAIL_INDEX_LM).set(i, dtlOverWorks.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Otime);
								dtlLaboryDays.get(DETAIL_INDEX_LM).set(i, dtlLaboryDays.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Wdays);
							}
						} else if ("00000000007".equals(section_id)) {
							dtlScanWorks.get(DETAIL_INDEX_QA_TEAM).set(i, dtlScanWorks.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
							dtlManageWorks.get(DETAIL_INDEX_QA_TEAM).set(i, dtlManageWorks.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Mtime);
							dtlHealthWorks.get(DETAIL_INDEX_QA_TEAM).set(i, dtlHealthWorks.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Htime);
							dtlOverWorks.get(DETAIL_INDEX_QA_TEAM).set(i, dtlOverWorks.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Otime);
							dtlLaboryDays.get(DETAIL_INDEX_QA_TEAM).set(i, dtlLaboryDays.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Wdays);
						}
					}
					gWorkers++;
					if ("00000000001".equals(section_id)) {
						if ("00000000012".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_DEC]++;
						} else if ("00000000013".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_NS]++;
						} else if ("00000000014".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_COM]++;
						} else if ("00000000011".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_QUOTE_TEAM]++;
						} else if ("00000000050".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_SP_TEAM]++;
						} else if ("00000000060".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_FDEC]++;
						} else if ("00000000061".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_FCOM]++;
						} else if ("00000000070".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_PERI]++;
						} else if ("00000000054".equals(line_id)) {
							dtlWorkers[DETAIL_INDEX_LM]++;
						}
					} else if ("00000000007".equals(section_id)) {
						dtlWorkers[DETAIL_INDEX_QA_TEAM]++;
					}
				} else if (RvsConsts.ROLE_LINELEADER.equals(operator.getRole_id())){ // 是线长
					for (int i = 0; i < weekBeanSize; i++) {
						gLeaderWorks.set(i, gLeaderWorks.get(i) + factWorkByWeek.get(i+1).Wtime);

						if ("00000000001".equals(section_id)) {
							if ("00000000012".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_DEC).set(i, dtlLeaderWorks.get(DETAIL_INDEX_DEC).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000013".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_NS).set(i, dtlLeaderWorks.get(DETAIL_INDEX_NS).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000014".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_COM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_COM).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000011".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_QUOTE_TEAM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_QUOTE_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000050".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_SP_TEAM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_SP_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000060".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_FDEC).set(i, dtlLeaderWorks.get(DETAIL_INDEX_FDEC).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000061".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_FCOM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_FCOM).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000070".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_PERI).set(i, dtlLeaderWorks.get(DETAIL_INDEX_PERI).get(i) + factWorkByWeek.get(i+1).Wtime);
							} else if ("00000000054".equals(line_id)) {
								dtlLeaderWorks.get(DETAIL_INDEX_LM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_LM).get(i) + factWorkByWeek.get(i+1).Wtime);
							}
						} else if ("00000000007".equals(section_id)) {
							dtlLeaderWorks.get(DETAIL_INDEX_QA_TEAM).set(i, dtlLeaderWorks.get(DETAIL_INDEX_QA_TEAM).get(i) + factWorkByWeek.get(i+1).Wtime);
						}
					}
				}
			}

			// 记入明细表
			insertIntoDetailsFile(book, weekBeans, dtlLeaderWorks, dtlOverWorks, dtlScanWorks, dtlManageWorks, dtlHealthWorks, dtlLaboryDays, dtlWorkers, gWipAgrees, mDao);

			// 记入全局表 bu记le , by Excel
			// insertIntoFile(globalSheet, weekBeans, gLeaderWorks, gOverWorks, gScanWorks, gManageWorks, gHealthWorks, gLaboryDays, gWorkers, gWipAgrees);
			insertIntoFileOnlyDays(globalSheet, weekBeans, gLeaderWorks, gOverWorks, gScanWorks, gManageWorks, gHealthWorks, gLaboryDays, gWorkers, gWipAgrees);
			globalSheet.setForceFormulaRecalculation(true);

			// 保存文件
			FileOutputStream fileOut = new FileOutputStream(destPath);
			book.write(fileOut);
			fileOut.close();

		} catch(Exception e) {
			_log.error(e.getMessage(), e);

		} finally {
		}
	}

	private void insertIntoFileOnlyDays(HSSFSheet globalSheet, List<WeekBean> weekBeans, List<Double> leaderWorks,
			List<Double> overWorks, List<Double> scanWorks, List<Double> manageWorks, List<Double> healthWorks,
			List<Double> laboryDays, Integer workers, List<Integer> gWipAgrees) {
		// 写入文档
		HSSFRow totalWorkTimeRow = globalSheet.getRow(11);
		
		for (int i = 0; i < weekBeans.size(); i++) {
			totalWorkTimeRow.getCell(i+1).setCellFormula(COL_NAMES[i+1] + "9*480*" + weekBeans.get(i).count_include_dates);
		}

		globalSheet.setForceFormulaRecalculation(true);
	}

	private void insertIntoDetailFile(HSSFSheet globalSheet, List<WeekBean> weekBeans, List<Double> leaderWorks,
			List<Double> overWorks, List<Double> scanWorks, List<Double> manageWorks, List<Double> healthWorks,
			List<Double> laboryDays, Integer workers, List<Integer> gWipAgrees) {
		// 写入文档
		HSSFRow weekTitleRow = globalSheet.getRow(0);
		HSSFRow leaderRow = globalSheet.getRow(2);
		HSSFRow overRow = globalSheet.getRow(3);
		HSSFRow laborRow = globalSheet.getRow(1);
		HSSFRow manageRow = globalSheet.getRow(5);
		HSSFRow healthRow = globalSheet.getRow(7);
		HSSFRow laborDaysRow = globalSheet.getRow(8);
		HSSFRow registDayRow = globalSheet.getRow(9);
		HSSFRow totalWorkTimeRow = globalSheet.getRow(11);
		HSSFRow wipAgreeRow = globalSheet.getRow(15);
		
		for (int i = 0; i < weekBeans.size(); i++) {
			weekTitleRow.getCell(i+1).setCellValue(weekBeans.get(i).show_text);
			leaderRow.getCell(i+1).setCellValue(leaderWorks.get(i));
			overRow.getCell(i+1).setCellValue(overWorks.get(i));
			laborRow.getCell(i+1).setCellValue(scanWorks.get(i));
			manageRow.getCell(i+1).setCellValue(manageWorks.get(i));
			healthRow.getCell(i+1).setCellValue(healthWorks.get(i));
			laborDaysRow.getCell(i+1).setCellValue(laboryDays.get(i) / weekBeans.get(i).count_include_dates / 475); // 475 分钟
			registDayRow.getCell(i+1).setCellValue(workers);
			totalWorkTimeRow.getCell(i+1).setCellFormula(COL_NAMES[i+1] + "9*480*" + weekBeans.get(i).count_include_dates);
			if(gWipAgrees != null) wipAgreeRow.getCell(i+1).setCellValue(gWipAgrees.get(i));
		}

		globalSheet.setForceFormulaRecalculation(true);
	}

	private void insertIntoDetailsFile(HSSFWorkbook book_detail, List<WeekBean> weekBeans,
			List<List<Double>> dtlLeaderWorks, List<List<Double>> dtlOverWorks, List<List<Double>> dtlScanWorks,
			List<List<Double>> dtlManageWorks, List<List<Double>> dtlHealthWorks, List<List<Double>> dtlLaboryDays,
			Integer[] dtlWorkers, List<Integer> gWipAgrees, MaterialMapper mDao) {

		// 记入报价组表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_QUOTE_TEAM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_QUOTE_TEAM),
				dtlOverWorks.get(DETAIL_INDEX_QUOTE_TEAM), dtlScanWorks.get(DETAIL_INDEX_QUOTE_TEAM), dtlManageWorks.get(DETAIL_INDEX_QUOTE_TEAM),
				dtlHealthWorks.get(DETAIL_INDEX_QUOTE_TEAM), dtlLaboryDays.get(DETAIL_INDEX_QUOTE_TEAM), dtlWorkers[DETAIL_INDEX_QUOTE_TEAM], gWipAgrees);

		// 记入分解工程 表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_DEC), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_DEC),
				dtlOverWorks.get(DETAIL_INDEX_DEC), dtlScanWorks.get(DETAIL_INDEX_DEC), dtlManageWorks.get(DETAIL_INDEX_DEC),
				dtlHealthWorks.get(DETAIL_INDEX_DEC), dtlLaboryDays.get(DETAIL_INDEX_DEC), dtlWorkers[DETAIL_INDEX_DEC], gWipAgrees);

		// 记入NS 工程表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_NS), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_NS),
				dtlOverWorks.get(DETAIL_INDEX_NS), dtlScanWorks.get(DETAIL_INDEX_NS), dtlManageWorks.get(DETAIL_INDEX_NS),
				dtlHealthWorks.get(DETAIL_INDEX_NS), dtlLaboryDays.get(DETAIL_INDEX_NS), dtlWorkers[DETAIL_INDEX_NS],
				gWipAgrees);

		// 记入总组工程表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_COM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_COM),
				dtlOverWorks.get(DETAIL_INDEX_COM), dtlScanWorks.get(DETAIL_INDEX_COM), dtlManageWorks.get(DETAIL_INDEX_COM),
				dtlHealthWorks.get(DETAIL_INDEX_COM), dtlLaboryDays.get(DETAIL_INDEX_COM), dtlWorkers[DETAIL_INDEX_COM],
				gWipAgrees);

		// 记入外科镜维修表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_SP_TEAM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_SP_TEAM),
				dtlOverWorks.get(DETAIL_INDEX_SP_TEAM), dtlScanWorks.get(DETAIL_INDEX_SP_TEAM), dtlManageWorks.get(DETAIL_INDEX_SP_TEAM),
				dtlHealthWorks.get(DETAIL_INDEX_SP_TEAM), dtlLaboryDays.get(DETAIL_INDEX_SP_TEAM), dtlWorkers[DETAIL_INDEX_SP_TEAM],
				gWipAgrees);

		// 记入纤维镜分解表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_FDEC), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_FDEC),
				dtlOverWorks.get(DETAIL_INDEX_FDEC), dtlScanWorks.get(DETAIL_INDEX_FDEC), dtlManageWorks.get(DETAIL_INDEX_FDEC),
				dtlHealthWorks.get(DETAIL_INDEX_FDEC), dtlLaboryDays.get(DETAIL_INDEX_FDEC), dtlWorkers[DETAIL_INDEX_FDEC],
				gWipAgrees);

		// 记入纤维镜总组表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_FCOM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_FCOM),
				dtlOverWorks.get(DETAIL_INDEX_FCOM), dtlScanWorks.get(DETAIL_INDEX_FCOM), dtlManageWorks.get(DETAIL_INDEX_FCOM),
				dtlHealthWorks.get(DETAIL_INDEX_FCOM), dtlLaboryDays.get(DETAIL_INDEX_FCOM), dtlWorkers[DETAIL_INDEX_FCOM],
				gWipAgrees);

		// 记入周边维修表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_PERI), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_PERI),
				dtlOverWorks.get(DETAIL_INDEX_PERI), dtlScanWorks.get(DETAIL_INDEX_PERI), dtlManageWorks.get(DETAIL_INDEX_PERI),
				dtlHealthWorks.get(DETAIL_INDEX_PERI), dtlLaboryDays.get(DETAIL_INDEX_PERI), dtlWorkers[DETAIL_INDEX_PERI],
				gWipAgrees);

		// 记入中小修修理表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_LM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_LM),
				dtlOverWorks.get(DETAIL_INDEX_LM), dtlScanWorks.get(DETAIL_INDEX_LM), dtlManageWorks.get(DETAIL_INDEX_LM),
				dtlHealthWorks.get(DETAIL_INDEX_LM), dtlLaboryDays.get(DETAIL_INDEX_LM), dtlWorkers[DETAIL_INDEX_LM],
				gWipAgrees);

		// 记入品保课 表
		insertIntoDetailFile(book_detail.getSheet(C_SHEET_QA_TEAM), weekBeans, dtlLeaderWorks.get(DETAIL_INDEX_QA_TEAM),
				dtlOverWorks.get(DETAIL_INDEX_QA_TEAM), dtlScanWorks.get(DETAIL_INDEX_QA_TEAM), dtlManageWorks.get(DETAIL_INDEX_QA_TEAM),
				dtlHealthWorks.get(DETAIL_INDEX_QA_TEAM), dtlLaboryDays.get(DETAIL_INDEX_QA_TEAM), dtlWorkers[DETAIL_INDEX_QA_TEAM],
				gWipAgrees);
		//
//		// 直送平均受理量
//		HSSFRow avgDirectRecieptRow = book_detail.getSheet(D_SHEET_QUOTATE).getRow(15);
//		for (int i = 0; i < weekBeans.size(); i++) {
//			WeekBean weekBean = weekBeans.get(i);
//			HSSFCell cell = avgDirectRecieptRow.getCell(i+1);
//			BigDecimal iCountReciept = mDao.getCountRecieptInPeriod(weekBean.start_date, weekBean.end_date, "dir");
//			cell.setCellValue(iCountReciept.divide(new BigDecimal(weekBean.count_include_dates), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		}
//		// 平均受理量
//		HSSFRow avgRecieptRow = book_detail.getSheet(D_SHEET_QUOTATE).getRow(16);
//		for (int i = 0; i < weekBeans.size(); i++) {
//			WeekBean weekBean = weekBeans.get(i);
//			HSSFCell cell = avgRecieptRow.getCell(i+1);
//			BigDecimal iCountReciept = mDao.getCountRecieptInPeriod(weekBean.start_date, weekBean.end_date, null);
//			cell.setCellValue(iCountReciept.divide(new BigDecimal(weekBean.count_include_dates), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		}
	}

	/**
	 * 记录循环并生成列
	 * @param row
	 * @param operatorProcess
	 * @param next_operatorProcess
	 * @param factWork
	 * @param operatorOnJobno
	 * @param readCursor
	 * @param weekBeans 
	 * @param commentCell
	 * @param highlightCell
	 */
	private void createRowCells(HSSFRow row, Map<String, Object> operatorProcess,
			Map<String, Object> next_operatorProcess, Map<String, List<WorkCount>> factWork,
			Map<String, OperatorEntity> operatorOnJobno, Map<String, String> readCursor, List<WeekBean> weekBeans,
			HSSFCellStyle commentCell, HSSFCellStyle highlightCell) {
		HSSFCell cell = null;

		// 行数
		int excelRowNum = row.getRowNum() + 1;

		String action_date = (String)operatorProcess.get("action_date");
		cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(action_date);

		// 工号
		String job_no = (String)operatorProcess.get("job_no");
		// 检测是否记工时
		boolean docount = operatorOnJobno.containsKey(job_no);

		cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
		if (!docount)
			cell.setCellStyle(highlightCell);
		setCell(cell, job_no.toUpperCase());

		String last_action_date = readCursor.get("action_date");
		String last_job_no = readCursor.get("job_no");

		if (last_action_date == null) {
			readCursor.put("action_date", action_date);
			readCursor.put("job_no", job_no);
			factWork.get(job_no).set(0, new WorkCount());
		} else if(!action_date.equals(last_action_date) || !job_no.equals(last_job_no)) { // 换人
			// 处理上一个人员的数据
			treatDayOperator(last_action_date, weekBeans, factWork.get(last_job_no));
			_log.info("Row" + excelRowNum + " " + last_action_date + "|" + last_job_no + ":" + factWork.get(last_job_no).get(0));

			// 准备统计下一个人员
			readCursor.put("action_date", action_date);
			readCursor.put("job_no", job_no);
			factWork.get(job_no).set(0, new WorkCount());
		}
		// 当前统计存放位置
		WorkCount currentWorkCount = factWork.get(job_no).get(0);

		// 姓名
//		operatorProcess.get("labor");
		cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
		if (!docount)
			cell.setCellStyle(highlightCell);
		setCell(cell, (String)operatorProcess.get("oname"));

		cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
		String section_name = (String)operatorProcess.get("sname");
		String line_name = (String)operatorProcess.get("lname");
		if (line_name != null) {
			if (line_name.contains("工程") && !line_name.contains("品保") && section_name != null) {
				if (line_name.startsWith("N")) line_name = " " + line_name;
				cell.setCellValue(section_name.replaceAll("翻修", "") + line_name);
			} else {
				cell.setCellValue(line_name);
			}
		}

		String process_code = (String)operatorProcess.get("process_code");
		cell = row.createCell(4, HSSFCell.CELL_TYPE_NUMERIC);
		setCell(cell, getInteger(process_code));

		String action_time = (String)operatorProcess.get("action_time");
		cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(action_time);

		String finish_time = (String)operatorProcess.get("finish_time");

		// 数据类型 1 扫描 2 输入
		Long d_type = (Long)operatorProcess.get("d_type");

		// 特记事项理由
		Integer pauseReason = getInteger(operatorProcess.get("pause_reason"));
		String sReason = getReason(pauseReason);
		String defaultReason = getReasonContent(pauseReason);
		String comments = (String)operatorProcess.get("comments");
		int stopReason = getStopReason(sReason, comments);

		boolean forgetfinish = false;
		if (d_type == 2L && stopReason != REASON_OVERWORK) {
			if (next_operatorProcess != null && job_no.equals(next_operatorProcess.get("job_no"))) {
				String next_action_time = (String)next_operatorProcess.get("action_time");
				finish_time = next_action_time;
			} else if (finish_time == null ||finish_time.compareTo(action_time) < 0) { // TODO 第二天
				if ("17:30".compareTo(action_time) < 0) {
					finish_time = action_time; // 未知加班时间
				} else {
					finish_time = "17:30";
				}
			}
		} else if (d_type == 1L) {
			if (finish_time == null) {
				if ("17:30".compareTo(action_time) < 0) {
					finish_time = action_time; // 未知加班时间
				} else {
					finish_time = "17:30";
				}
			} else if (finish_time.compareTo(action_time) < 0) {
				forgetfinish = true;
			}

			// 求工作天数
			if (action_time.compareTo("12:00") < 0) { // 上午工作
				currentWorkCount.Wdays |= 2;
			}
			if (finish_time.compareTo("13:30") > 0) { // 下午工作
				currentWorkCount.Wdays |= 1;
			}
			if (finish_time.compareTo("17:30") > 0) { // 平日加班实绩
				Double otime_f = 0.0 + diffMinutes("17:30", finish_time);
				if (otime_f > currentWorkCount.Otime_f) {
					currentWorkCount.Otime_f = otime_f;
				}
			}
		}

		cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(finish_time);

		cell = row.createCell(7, HSSFCell.CELL_TYPE_FORMULA);
		cell.setCellFormula("(IF(F" + excelRowNum + "=\"\",\"\",$G" + excelRowNum + "-$F" + excelRowNum + "))*24*60");

		// 直接时间（M）
		BigDecimal bdMinutes = (BigDecimal) operatorProcess.get("minutes");
		if ("111".equals(process_code) || "121".equals(process_code) || "131".equals(process_code) || "711".equals(process_code) || "301".equals(process_code)
				|| (("251".equals(process_code) || "252".equals(process_code) || "321".equals(process_code) 
						|| "400".equals(process_code)) && bdMinutes != null && bdMinutes.setScale(0, RoundingMode.HALF_EVEN).equals(BigDecimal.ZERO))) {
			// TODO xianzai
			cell = row.createCell(8, HSSFCell.CELL_TYPE_NUMERIC);
			if ("111".equals(process_code) || "711".equals(process_code)) {
				cell.setCellValue(10);

				currentWorkCount.Wtime += 10;
			} else if ("301".equals(process_code)) {
				cell.setCellValue(28);

				currentWorkCount.Wtime += 28;
			} else if ("251".equals(process_code) || "252".equals(process_code) || "321".equals(process_code) || "400".equals(process_code)) {
				cell.setCellValue(5);

				currentWorkCount.Wtime += 5;
			} else if ("121".equals(process_code) || "131".equals(process_code)) {
				cell.setCellValue(5);

				currentWorkCount.Wtime += 5;
			}
		} else {
			if (bdMinutes != null) {
				if (forgetfinish) bdMinutes = bdMinutes.subtract(new BigDecimal(900)); // 15小时
				cell = row.createCell(8, HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(bdMinutes.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());

				currentWorkCount.Wtime += bdMinutes.doubleValue();
			} else {
				cell = row.createCell(8, HSSFCell.CELL_TYPE_BLANK);
			}
		}
		
		cell = row.createCell(9, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue((String)operatorProcess.get("sorc_no"));

		cell = row.createCell(10, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(getRank(operatorProcess.get("level")));

		cell = row.createCell(11, HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue((String)operatorProcess.get("model_name"));

		cell = row.createCell(12, HSSFCell.CELL_TYPE_STRING);
		if (stopReason == REASON_OVERWORK) {
			cell.setCellStyle(highlightCell);

			currentWorkCount.Otime += diffMinutes(action_time, finish_time);
		} else if (stopReason == REASON_W) {
			currentWorkCount.Wtime += diffMinutes(action_time, finish_time);
			if (finish_time.compareTo("17:30") > 0) { // 平日加班实绩
				Double otime_f = 0.0 + diffMinutes("17:30", finish_time);
				if (otime_f > currentWorkCount.Otime_f) {
					currentWorkCount.Otime_f = otime_f;
				}
			}
		} else if (stopReason == REASON_M) {
			currentWorkCount.Mtime += diffMinutes(action_time, finish_time);
			if (finish_time.compareTo("17:30") > 0) { // 平日加班实绩
				Double otime_f = 0.0 + diffMinutes("17:30", finish_time);
				if (otime_f > currentWorkCount.Otime_f) {
					currentWorkCount.Otime_f = otime_f;
				}
			}
		}
		cell.setCellValue(sReason);

		// 内容
		cell = row.createCell(13, HSSFCell.CELL_TYPE_STRING);
		if (stopReason == REASON_OVERWORK)
			cell.setCellStyle(highlightCell);
		cell.setCellStyle(commentCell);

		if (!isEmpty(defaultReason) && !isEmpty(comments)) {
			cell.setCellValue(defaultReason + "\n" + comments);
		} else if (isEmpty(defaultReason) && isEmpty(comments)){
			cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
		} else {
			cell.setCellValue(defaultReason + comments);
		}

	}

	/**
	 * 计算日数据到周数据
	 * @param action_date 
	 * @param weekBeans
	 * @param list
	 */
	private void treatDayOperator(String action_date, List<WeekBean> weekBeans, List<WorkCount> workCounts) {
		WorkCount countWorkCount = workCounts.get(0);
		if (countWorkCount.Wdays == 3) {
			countWorkCount.Wdays = 475; // 全天 475分钟
		} else if (countWorkCount.Wdays == 2) {
			countWorkCount.Wdays = 230; // 4 小时 - 10 分钟
		} else if (countWorkCount.Wdays == 1) {
			countWorkCount.Wdays = 245; // 4 小时 15 分钟 - 10 分钟
		}

		// 计算休息时间
		double worktime = countWorkCount.Wdays;// + countWorkCount.Otime_f;
		countWorkCount.Htime = worktime - countWorkCount.Wtime - countWorkCount.Mtime;
		if (countWorkCount.Htime < 0) countWorkCount.Htime = 0.0; 

//		// 如果有记入的加班时间，则直接作业时间内去除的加班时间内的作业时间，以免重复统计 2013/10/11取消
//		if (countWorkCount.Otime > 0) {
//			countWorkCount.Wtime -= countWorkCount.Otime_f;
//		}
		if (countWorkCount.Otime == 0 && countWorkCount.Otime_f > 0) {
			countWorkCount.Otime = countWorkCount.Otime_f;
		}

		int i = 0;
		for (; i < weekBeans.size(); i++) {
			WeekBean weekBean = weekBeans.get(i);
			if (action_date.compareTo(weekBean.start_date) >=0 && action_date.compareTo(weekBean.end_date) <=0) {
				
				break;
			}
		}
		if (i + 1 < workCounts.size()) {
			WorkCount destWorkCount = workCounts.get(i + 1);
			destWorkCount.Htime += countWorkCount.Htime;
			destWorkCount.Mtime += countWorkCount.Mtime;
			destWorkCount.Wtime += countWorkCount.Wtime;
			destWorkCount.Otime += countWorkCount.Otime;
			destWorkCount.Otime_f += countWorkCount.Otime_f;

			destWorkCount.Wdays += countWorkCount.Wdays;
		} else {
			workCounts.size();
		}
	}

	private static final int REASON_NONE = 0;
	private static final int REASON_W = 1;
	private static final int REASON_M = 2;
	private static final int REASON_H = 3;
	private static final int REASON_OVERWORK = 5;
	private static final int REASON_UNKNOWN = 9;

	private int getStopReason(String sReason, String comments) {
		if (sReason == null) return REASON_NONE;
		if (sReason.startsWith("M")) return REASON_M;
		if (sReason.startsWith("DT")) return REASON_W;
		if (sReason.startsWith("H")) return REASON_H;
		if (sReason.startsWith("O")) return REASON_OVERWORK;
		if (sReason.equals("其他") && comments.toUpperCase().contains("M")) return REASON_M;
		if (sReason.equals("其他") && comments.contains("工位")) return REASON_W;
		return REASON_UNKNOWN;
	}

	private Map<String, String> rankMap = new HashMap<String, String>();

	private String getRank(Object along) {
		if (along == null) {
			return null;
		}
		String string = "" + along;
		if (isEmpty(string)) {
			return null;
		}
		if (rankMap.containsKey(string)) { 
			return rankMap.get(string);
		}

		String rankText = CodeListUtils.getValue("material_level_all", string);
		if (isEmpty(rankText)) {
			return string;
		}
		rankMap.put(string, rankText);

		return rankText;
	}

	private Map<String, String> reasonMap = new HashMap<String, String>();
	private Map<String, String> reasonContentMap = new HashMap<String, String>();

	private String getReason(Integer object) {
		if (object == null) {
			return null;
		}
		String string = "" + object;
		if (reasonMap.containsKey(string)) { 
			return reasonMap.get(string);
		}

		String reasonText = CodeListUtils.getValue("all_break_reason", string);
		String[] reasonPart = reasonText.split(":");
		reasonMap.put(string, reasonPart[0]);
		if (reasonPart.length > 1) {
			reasonContentMap.put(string, reasonPart[1]);
		} else  {
			reasonContentMap.put(string, "");
		}

		return reasonPart[0];
	}

	private String getReasonContent(Integer object) {
		if (object == null) {
			return null;
		}
		String string = "" + object;
		if (reasonContentMap.containsKey(string)) { 
			return reasonContentMap.get(string);
		}

		String reasonText = CodeListUtils.getValue("all_break_reason", string);
		String[] reasonPart = reasonText.split(":");
		reasonMap.put(string, reasonPart[0]);
		reasonContentMap.put(string, reasonPart[1]);

		return reasonPart[1];
	}

	private Integer getInteger(Object object) {
		if (object == null) return null;
		if (object instanceof String) {
			try {
				return Integer.parseInt(object.toString());
			} catch(Exception e) {
			}
		}
		if (object instanceof Long) {
			try {
				return ((Long) object).intValue();
			} catch(Exception e) {
			}
		}
		return null;
	}


	/**
	 * 取得周和工作日信息
	 * @param monthStart
	 * @param restDays
	 * @param conn
	 * @return
	 */
	private List<WeekBean> getWeekBeans(Calendar monthStart, Map<Integer, Boolean> restDays, SqlSession conn) {
		List<WeekBean> weekBeans = new ArrayList<WeekBean>();
		Calendar thisMonthStart = Calendar.getInstance();
		Calendar nextMonthStart = Calendar.getInstance();

		thisMonthStart.setTimeInMillis(monthStart.getTimeInMillis());
		nextMonthStart.setTimeInMillis(monthStart.getTimeInMillis());

		nextMonthStart.add(Calendar.MONTH, 1);

		// 得到本月休假及调整日
		HolidayMapper hdao = conn.getMapper(HolidayMapper.class);
		List<String> lholidays = hdao.searchHolidayOfMonth(DateUtil.toString(thisMonthStart.getTime(), "yyyy/MM"));
		Set<Integer> sHolidays = new HashSet<Integer>();
		for (String lholiday : lholidays) sHolidays.add(Integer.parseInt(lholiday));

		// 周分段
		WeekBean weekBean= new WeekBean();
		for (;thisMonthStart.before(nextMonthStart); thisMonthStart.add(Calendar.DATE, 1)){
			int dayOfWeek = thisMonthStart.get(Calendar.DAY_OF_WEEK);
			int cdate = thisMonthStart.get(Calendar.DATE);
			// 是否工作日
			boolean isWorkDay = (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) 
					!= sHolidays.contains(cdate);
			String textDate = DateUtil.toString(thisMonthStart.getTime(), DateUtil.ISO_DATE_PATTERN);
			if (isWorkDay) {
				if (weekBean.start_date == null)
					weekBean.start_date = textDate;
				weekBean.count_include_dates++;
				weekBean.end_date = textDate;
			}
			if (dayOfWeek == Calendar.THURSDAY && weekBean.count_include_dates >= 3) { // 小于3天的归入下一周
				// 周结束
				weekBeans.add(weekBean);
				weekBean = new WeekBean();
			}
			restDays.put(cdate, isWorkDay);
		}
		// 最后一周
		if (weekBean.count_include_dates > 0) {
			if (weekBean.count_include_dates >= 3) {
				weekBeans.add(weekBean);
			} else {
				// 计入上一周
				WeekBean lastWeekBean = weekBeans.get(weekBeans.size() - 1); // myiqsi
				lastWeekBean.end_date = weekBean.end_date;
				lastWeekBean.count_include_dates += weekBean.count_include_dates;
			}
		}

		// 设定周的文字表达
		for (WeekBean aweekBean :weekBeans) {
			// Sample 4/1～4/11
			aweekBean.show_text = aweekBean.start_date.replace('-', '/') + "～" 
					+ aweekBean.end_date.replace('-', '/');
			aweekBean.show_text = aweekBean.show_text.replaceAll("/0", "/").replaceAll("\\d{4}/", "");
		}
		return weekBeans;
	}

	/**
	 * 有空白格判定的数值填写
	 * @param cell
	 * @param integer
	 */
	private void setCell(HSSFCell cell, Integer integer) {
		if (integer == null) {
			cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
		} else {
			cell.setCellValue(integer);
		}
	}

	/**
	 * 有空白格判定的文字填写
	 * @param cell
	 * @param string
	 */
	private void setCell(HSSFCell cell, String string) {
		if (string == null) {
			cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
		} else {
			cell.setCellValue(string);
		}
	}

	/**
	 * 求同一天内分钟差
	 */
	private int diffMinutes(String start_time, String end_time) {
		try {
			String[] s_ticks = start_time.split(":");
			String[] e_ticks = end_time.split(":");
			Calendar s_cal = Calendar.getInstance();
			Calendar e_cal = Calendar.getInstance();
			s_cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s_ticks[0]));
			s_cal.set(Calendar.MINUTE, Integer.parseInt(s_ticks[1]));
			e_cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(e_ticks[0]));
			e_cal.set(Calendar.MINUTE, Integer.parseInt(e_ticks[1]));
			long mindiff = e_cal.getTimeInMillis() - s_cal.getTimeInMillis();
			return new Long(mindiff / 60000).intValue();
		} catch(Exception e) {
			_log.error(e.getMessage(), e);
			return 0;
		}

	}

	/**
	 * 测试进口
	 * @param args
	 * @throws JobExecutionException
	 */
	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.YEAR, 2018);
		today.set(Calendar.MONTH, Calendar.NOVEMBER);
		today.set(Calendar.DATE, 1);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();
//		SqlSessionManager connManager = getTempWritableConn();
//		PathConsts.BASE_PATH = "D:\\rvs";
		PathConsts.BASE_PATH = "E:\\rvsG";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
				;
		PathConsts.REPORT = "\\Reports";
		PathConsts.PCS_TEMPLATE = "\\PcsTemplates";
		;
		PathConsts.PCS = "\\Pcs";

		DayWorkTotalToMonthJob job = new DayWorkTotalToMonthJob();
		job.makeStatistics(today, conn);
		// job.monthlyFilePack(conn, today);
		// job.clearSap(connManager);

		if (conn != null) {
			conn.close();
		}
		conn = null;
	}

	private List<Double> newDoubleList(int size) {
		List<Double> ret = new ArrayList<Double>();
		for (int i =0;i< size;i++) {
			ret.add(0.0);
		}
		return ret;
	}

	private List<List<Double>> newDoubleLists(int weekBeanSize, int iCountSheetDetail) {
		List<List<Double>> ret = new ArrayList<List<Double>>();
		for (int i =0;i < iCountSheetDetail;i++) {
			List<Double> sheetData = newDoubleList(weekBeanSize);
			ret.add(sheetData);
		}
		return ret;
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
	
	private class WeekBean {
		private String show_text = null;
		private String start_date = null;
		private String end_date = null;
		private int count_include_dates = 0;
		public String toString() {
			return show_text;
		}
	}

	private class WorkCount {
		private Double Wtime = 0.0;
		private Double Mtime = 0.0;
		private Double Htime = 0.0;
		private Double Otime = 0.0;
		private Double Otime_f = 0.0;
		private Integer Wdays = 0; // 半小时数

		public String scale(double time) {
			return new BigDecimal(time).setScale(2, RoundingMode.HALF_UP).toString();
		}
		public String toString() {
			return "直接作业时间:" + scale(Wtime) + 
					" 管理等待时间:" + scale(Mtime) + 
					" 休息暂停时间:" + scale(Htime) + 
					" 加班工时实绩:" + scale(Otime_f) + 
					" 加班工时登记:" + scale(Otime) + 
					" 实际工作天数:" + scale(Wdays / 475.0);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private static boolean deleteDir(File dir) {
		if (!dir.exists())
			return true;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	private void copyDirectory(String srcDir, String destDir) {
		copyDirectory(srcDir, destDir, null);
	}
	private void copyDirectory(String srcDir, String destDir, Long lfrom) {
		File fSrcDir = new File(srcDir);
		if (!fSrcDir.exists() || !fSrcDir.isDirectory()) {
			return;
		}

		File fDestDir = new File(destDir);
		if (!fDestDir.exists()) {
			fDestDir.mkdirs();
		}
		if (!fDestDir.isDirectory()) {
			return;
		}

		File[] files = fSrcDir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				if (lfrom == null || file.lastModified() > lfrom) 
				try {
					FileUtils.copyFileToDirectory(file, fDestDir);
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
			}
		}
	}
	private void copyDirectoryThursday(String srcDir, String destDir,
			Calendar monthStart) {
		File fSrcDir = new File(srcDir);
		if (!fSrcDir.exists() || !fSrcDir.isDirectory()) {
			return;
		}

		File fDestDir = new File(destDir);
		if (!fDestDir.exists()) {
			fDestDir.mkdirs();
		}
		if (!fDestDir.isDirectory()) {
			return;
		}
		Calendar nextMonth = Calendar.getInstance();
		Calendar pace = Calendar.getInstance();
		nextMonth.setTimeInMillis(monthStart.getTimeInMillis());
		nextMonth.add(Calendar.MONTH, 1);
		pace.setTimeInMillis(monthStart.getTimeInMillis());

		while (pace.before(nextMonth)) {
			if (pace.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
				File lfile = new File(srcDir + DateUtil.toString(pace.getTime(), "MMdd"));
				_log.info("sqlDump:" + lfile.getAbsolutePath());
				if (lfile.exists()) {
					try {
						FileUtils.copyFileToDirectory(lfile, fDestDir);
					} catch (IOException e) {
						_log.error(e.getMessage(), e);
					}
				}
			}

			pace.add(Calendar.DATE, 1);
		}
	}
}
