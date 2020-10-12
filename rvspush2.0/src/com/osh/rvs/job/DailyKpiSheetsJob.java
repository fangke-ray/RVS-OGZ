package com.osh.rvs.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.DailyKpiDataEntity;
import com.osh.rvs.mapper.statistics.DailyKpiMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class DailyKpiSheetsJob implements Job {

	public static Logger _log = Logger.getLogger("DailyWorkSheetsJob");
	private static final BigDecimal DIVISOR_HUNDRED = new BigDecimal(100);

	private static DailyKpiSheetsJob instance = new DailyKpiSheetsJob();

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar monthStart = Calendar.getInstance();

		_log.info("dailyKpiSheetsTriggerReport: " + jobKey + " executing at " + monthStart);

		monthStart.set(Calendar.DATE, 1);
		monthStart.set(Calendar.HOUR_OF_DAY, 0);
		monthStart.set(Calendar.MINUTE, 0);
		monthStart.set(Calendar.SECOND, 0);
		monthStart.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		Calendar today = Calendar.getInstance();

		// 上午
		kpi(today,conn);

		conn.close();
		conn = null;
	}

	private void kpi(Calendar today, SqlSession conn) {
		DailyKpiMapper dkMapper = conn.getMapper(DailyKpiMapper.class);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得前一个实际工作日
		for (int i=0; i< 5;i++) {
			today.add(Calendar.DATE, -1);
			if (dkMapper.getByDate(today.getTime()) != null) break;
		}

		// 建立下载文件
		String templatePath = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "日报模板.xls";

		String todayString = DateUtil.toString(today.getTime(), "yyyy-MM-dd");
		String destFilename = "每日KPI指标达成情况-" + todayString + ".xls";
		String destPdfname = "每日KPI指标达成情况-" + todayString + ".pdf";
		String destPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\kpi_process\\"
				+ DateUtil.toString(today.getTime(), "yyyyMM") + "\\" + destFilename;
		String destPdfpath= PathConsts.BASE_PATH + PathConsts.REPORT + "\\kpi_process\\"
				+ DateUtil.toString(today.getTime(), "yyyyMM") + "\\" + destPdfname;

		try {
			FileUtils.copyFile(new File(templatePath), new File(destPath));
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(destPath);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel

			HSSFSheet sheet = work.getSheetAt(0);

			HSSFRow row = sheet.getRow(36);
			HSSFCell cell = row.getCell(5);
			cell.setCellValue(today.getTime());

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(today.getTimeInMillis());

			// 取得当天位置
			int dow =cal.get(Calendar.DAY_OF_WEEK);
			if (dow == Calendar.SUNDAY) {;
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				cal.add(Calendar.WEEK_OF_MONTH, -1);
			} else {
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}

			String comment = "";
			for (int i=0;i<7;i++) {
				DailyKpiDataEntity dayData = dkMapper.getByDate(cal.getTime());
				if (dayData == null) {
					dayData = new DailyKpiDataEntity();
				}
				Date count_date = cal.getTime();
				if (!CommonStringUtil.isEmpty(dayData.getComment())) {
					comment += DateUtil.toString(count_date, "M月d日:") + "\n" + dayData.getComment() + "\n";
				}
				if (i != 6) {
					if (count_date.after(today.getTime())) break;

					// 147PB出货目标	/实绩	
					row = sheet.getRow(54);
					cell = getCellExist(row, 22 + i);

					if(dayData.getHalf_period_complete() != null) {
						cell.setCellValue(dayData.getHalf_period_complete());
					}

					// 147PB X月出货目标	/实绩	
					row = sheet.getRow(55);
					cell = getCellExist(row, 22 + i);

					if(dayData.getMonth_complete() != null) {
						cell.setCellValue(dayData.getMonth_complete());
					}

					// 保修期内返品率			
					row = sheet.getRow(56);
					cell = getCellExist(row, 22 + i);

					if(dayData.getService_repair_back_rate() != null) {
						cell.setCellValue(dayData.getService_repair_back_rate().divide(DIVISOR_HUNDRED, 4, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					//最终检查合格率			
					row = sheet.getRow(58);
					cell = getCellExist(row, 22 + i);

					if(dayData.getFinal_inspect_pass_rate() != null) {
						cell.setCellValue(dayData.getFinal_inspect_pass_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					//7天内纳期遵守比率	-> 大修理6天内纳期遵守比率		
					row = sheet.getRow(60);
					cell = getCellExist(row, 22 + i);

					if(dayData.getIntime_complete_slt_rate() != null) {
						cell.setCellValue(dayData.getIntime_complete_slt_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					// 大修理5天内纳期遵守比率		
					row = sheet.getRow(62);
					cell = getCellExist(row, 22 + i);

					if(dayData.getIntime_complete_rate() != null) {
						cell.setCellValue(dayData.getIntime_complete_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					// 中修理2天内纳期遵守比率	
					row = sheet.getRow(64);
					cell = getCellExist(row, 22 + i);

					if(dayData.getIntime_complete_medium_rate() != null) {
						cell.setCellValue(dayData.getIntime_complete_medium_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					// 小修理2天内纳期遵守比率
					row = sheet.getRow(65);
					cell = getCellExist(row, 22 + i);

					if(dayData.getIntime_complete_light_rate() != null) {
						cell.setCellValue(dayData.getIntime_complete_light_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					//每日生产计划达成率			
					row = sheet.getRow(69);
					cell = getCellExist(row, 22 + i);

					if(dayData.getTotal_plan_processed_rate() != null) {
						cell.setCellValue(dayData.getTotal_plan_processed_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}
//
//					//"翻修1课每日生产计划达成率" -> 周边设备8天纳期遵守比率
//					row = sheet.getRow(64);
//					cell = getCellExist(row, 22 + i);
//
//					if(dayData.getSection1_plan_processed_rate() != null) {
//						cell.setCellValue(dayData.getSection1_plan_processed_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
//					}
//
//					//"翻修2课每日生产计划达成率" -> 小修理2天内纳期遵守比率
//					row = sheet.getRow(62);
//					cell = getCellExist(row, 22 + i);
//
//					if(dayData.getSection2_plan_processed_rate() != null) {
//						cell.setCellValue(dayData.getSection2_plan_processed_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
//					}

//					//NS再生率			
//					row = sheet.getRow(66);
//					cell = getCellExist(row, 22 + i);
//
//					if(dayData.getNs_regenerate_rate() != null) {
//						cell.setCellValue(dayData.getNs_regenerate_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
//					}
//
					//工程内直行率			
					row = sheet.getRow(72);
					cell = getCellExist(row, 22 + i);

					if(dayData.getInline_passthrough_rate() != null) {
						cell.setCellValue(dayData.getInline_passthrough_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}

					//报价周期LT达成率			
					row = sheet.getRow(74);
					cell = getCellExist(row, 22 + i);

					if(dayData.getService_repair_analysis_lt24_rate() != null) {
						cell.setCellValue(dayData.getService_repair_analysis_lt24_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}
					//"直送报价周期LT达成率"			
					row = sheet.getRow(76);
					cell = getCellExist(row, 22 + i);

					if(dayData.getService_repair_analysis_lt48_rate() != null) {
						cell.setCellValue(dayData.getService_repair_analysis_lt48_rate().divide(DIVISOR_HUNDRED, 3, BigDecimal.ROUND_HALF_UP).doubleValue());
					}
				}
				cal.add(Calendar.DATE, 1);
			}

			// 取得本周末日期
			Calendar weekstart = Calendar.getInstance();
			weekstart.setTimeInMillis(cal.getTimeInMillis());
			weekstart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			Calendar weekend = Calendar.getInstance();
			weekend.setTimeInMillis(cal.getTimeInMillis());
			weekend.add(Calendar.DATE, -1);

			// 取得时期,生产计划
			String periodS = RvsUtils.getBussinessHalfYearString(weekstart);
			String period = RvsUtils.getBussinessHalfYearString(weekend);
			row = sheet.getRow(55);
			cell = row.getCell(1);
			if (periodS.equals(period)) {
				cell.setCellValue(period + " 出货目标");
			} else {
				cell.setCellValue(periodS + " 出货目标\r\n" + period + " 出货目标");
				setCellWrap(cell);
			}

			// 本期出货累计计划数
			Date dPeriodEndDate = RvsUtils.getBussinessHalfStartDate(cal);
			String currentPlannedAmount = getCurrentPlannedAmount(dPeriodEndDate, dkMapper);
			cell = row.getCell(5);
			if (periodS.equals(period)) {
				cell.setCellValue(currentPlannedAmount + " 台");
			} else {
				Date dPeriodStartDate = RvsUtils.getBussinessHalfStartDate(cal);
				String currentPlannedAmountS = getCurrentPlannedAmount(dPeriodStartDate, dkMapper);
				cell.setCellValue(currentPlannedAmountS + " 台\r\n" + currentPlannedAmount + " 台");
				setCellWrap(cell);
			}

			String planMonthS = "" + (weekstart.get(Calendar.MONTH) + 1);
			String planYear = "" + weekend.get(Calendar.YEAR);
			String planMonth = "" + (weekend.get(Calendar.MONTH) + 1);

			row = sheet.getRow(57);
			cell = row.getCell(1);
			if (planMonthS.equals(planMonth)) {
				cell.setCellValue(period + " " + planMonth + " 月出货目标");
			} else {
				cell.setCellValue(periodS + " " + planMonthS + " 月出货目标\n"
						+ period + " " + planMonth + " 月出货目标");
				setCellWrap(cell);
			}

			Integer shippingPlanOfMonth = dkMapper.getShippingPlan(planYear, planMonth);
			String sShippingPlanOfMonth = " - ";
			cell = row.getCell(5);
			if (shippingPlanOfMonth != null) sShippingPlanOfMonth = "" + shippingPlanOfMonth;

			if (planMonthS.equals(planMonth)) {
				cell.setCellValue(sShippingPlanOfMonth + " 台");
			} else {
				String planYearS = "" + weekstart.get(Calendar.YEAR);
				shippingPlanOfMonth = dkMapper.getShippingPlan(planYearS, planMonthS);
				String sShippingPlanOfMonthStart = " - ";
				if (shippingPlanOfMonth != null) sShippingPlanOfMonthStart = "" + shippingPlanOfMonth;

				cell.setCellValue(sShippingPlanOfMonthStart + " 台\n"
						+ sShippingPlanOfMonth + " 台");
				setCellWrap(cell);
			}

			row = sheet.getRow(27);
			cell = row.getCell(4);
			cell.setCellValue(period + " " + planMonth + "月 第" + weekend.get(Calendar.WEEK_OF_MONTH) + "周");

			row = sheet.getRow(194);
			cell = getCellExist(row, 1);
			cell.setCellValue(comment);

			sheet.setForceFormulaRecalculation(true);

			out = new FileOutputStream(destPath);
			work.write(out);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
			}
		}

		try {
			String destUrl = "http://localhost:8080/rvs/download.do?method=saveRPdf&path="+DateUtil.toString(today.getTime(), "yyyyMM")+"&filename=" + todayString;
			_log.info("destUrl=" + destUrl);
			URL url = new URL(destUrl);
			url.getQuery();
			URLConnection urlconn = url.openConnection();
			urlconn.setReadTimeout(1000); // 等返回
			urlconn.connect();
			urlconn.getContentType(); // 这个就能触发
		} catch (Exception e) {
			_log.error("Failed", e);
		}

		// 等待
		try {
			Thread.sleep(3 * 60 * 1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Collection<InternetAddress> toIas = RvsUtils.getMailIas("daily.kpi.to", conn);
		Collection<InternetAddress> ccIas = RvsUtils.getMailIas("daily.kpi.cc", conn);

		String subject = PathConsts.MAIL_CONFIG.getProperty("daily.kpi.title") + todayString;
		String content = PathConsts.MAIL_CONFIG.getProperty("daily.kpi.content").replaceAll("{0}", todayString);
		MailUtils.sendMultipartMail(toIas, ccIas, subject, content, destPdfpath);
	}

	private void setCellWrap(HSSFCell cell) {
		HSSFCellStyle cellStyle = cell.getCellStyle();
		cellStyle.setWrapText(true);
	}

	private HSSFCell getCellExist(HSSFRow row, int col) {
		HSSFCell cell = row.getCell(col);
		if (cell == null) {
			cell = row.createCell(col);
		}
		return cell;
	}

	/**
	 * 测试进口
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.MONTH, Calendar.MARCH);
		today.set(Calendar.DATE, 30);

		today.set(Calendar.HOUR_OF_DAY, 10);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		PathConsts.BASE_PATH = "C:\\work\\rvsG";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
		PathConsts.PCS_TEMPLATE = "\\PcsTemplates";
		;
		PathConsts.REPORT = "\\Reports";

		DailyKpiSheetsJob job = new DailyKpiSheetsJob();

		job.kpi(today, conn);
	}

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}

	public static DailyKpiSheetsJob getInstance() {
		return instance;
	}

	// 取得本期出货累计实绩校正数
	private String getCurrentPlannedAmount(Date dPeriodStartDate,
			DailyKpiMapper mapper) {
		String inStr = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(dPeriodStartDate);
		for (int i = 0;i < 6;i++) {
			inStr += (",(" + cal.get(Calendar.YEAR) + "," + (cal.get(Calendar.MONTH) + 1) + ")");
			cal.add(Calendar.MONTH, 1);
		}
		inStr = inStr.substring(1);

		String getsum = mapper.getPlanAmountOfPeriod(inStr);

		if (CommonStringUtil.isEmpty(getsum)) {
			return "(未定义)";
		} else {
			return getsum;
		}
	}
}
