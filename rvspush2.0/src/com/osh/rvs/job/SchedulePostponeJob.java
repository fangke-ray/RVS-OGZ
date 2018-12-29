package com.osh.rvs.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.ScheduleHistoryEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.mapper.statistics.ScheduleHistoryMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

public class SchedulePostponeJob implements Job {

	private static final int SCHEDULE_WORK_REPORT_START_LINE = 1;
	public static Logger _log = Logger.getLogger("SchedulePostponeJob");

	public static final int PERIOD_A_THROUGH = 120;
	public static final int PERIOD_B_THROUGH = 195;
	public static final int PERIOD_C_THROUGH = 315;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		_log.info("SchedulePostponeJob: " + jobKey + " executing at " + today);

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try {
			conn.startManagedSession(false);
			// 明天
			HolidayMapper hMapper = conn.getMapper(HolidayMapper.class);
			Date nextDay = hMapper.getNextWorkDate(today.getTime());

			// 当天记录
			collectNextDay(today.getTime(), nextDay, conn);

			// 顺延
			makePostpone(today.getTime(), nextDay, conn);

			sortToPeriod(nextDay, conn);

			// 建立文档
			createReports(today, conn);

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

	private void createReports(Calendar today, SqlSessionManager conn) {
		// 取得全部需要处理的日期信息
		ScheduleHistoryMapper shMapper = conn.getMapper(ScheduleHistoryMapper.class);
		List<Date> treatDates = shMapper.getUnfinishedDates(today.getTime());

		for (Date treatDate : treatDates) {
			// 取得每天的数据一览
			List<ScheduleHistoryEntity> shis = shMapper.getScheduleHistory(treatDate);
			makeFile(today, treatDate, shis, shMapper);
		}
	}

	private void makeFile(Calendar today, Date treatDate, List<ScheduleHistoryEntity> shis, ScheduleHistoryMapper shMapper) {
		int length = shis.size();
		int firstLength = 0;
		int avalibleLength = 0;
		int completeLength = 0;

		String model_path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "计划报告书模板.xls";

		String cacheFilename = "计划报告书-" + DateUtil.toString(treatDate, "yyyy-MM-dd") + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\schedule\\"
				+ DateUtil.toString(treatDate, "yyyyMM") + "\\" + cacheFilename;

		// 建立文件
		try {
			FileUtils.copyFile(new File(model_path), new File(cachePath));
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
			return;
		}

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(cachePath);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet count_sheet = work.getSheetAt(0);// 取得第一个Sheet
			HSSFSheet detail_sheet = work.getSheetAt(1);// 取得第二个Sheet

			// 设置字体大小
			HSSFFont font = work.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("微软雅黑");
			/* 设置单元格内容居中显示 */
			HSSFCellStyle styleAlignCenter = work.createCellStyle();
			styleAlignCenter.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setBorderTop(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styleAlignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignCenter.setWrapText(true);
			styleAlignCenter.setFont(font);
			/* 设置单元格内容居左显示 */
			HSSFCellStyle styleAlignLeft = work.createCellStyle();
			styleAlignLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderTop(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAlignLeft.setWrapText(true);
			styleAlignLeft.setFont(font);

			HSSFRow row = detail_sheet.getRow(0);
			String enDate = DateUtil.toString(treatDate, "M月d日");
			row.getCell(11).setCellValue(enDate + "当时\r\n入库预定日");
			row.getCell(13).setCellValue(enDate + "当时\r\n加急状态");

			for (int i = 0; i < length; i++) {
				ScheduleHistoryEntity scheduleHistory = shis.get(i);
				int iLine = (i + SCHEDULE_WORK_REPORT_START_LINE);// 行号索引
				if (setExcelRowValue(treatDate, scheduleHistory, iLine, detail_sheet, styleAlignCenter, styleAlignLeft, shMapper)) {
					completeLength++;
				}

				if (scheduleHistory.getIn_schedule_means() == null) scheduleHistory.setIn_schedule_means(3);//TODO
				if (1 == scheduleHistory.getRemove_flg() || 1 == scheduleHistory.getIn_schedule_means())
					firstLength++;

				if (scheduleHistory.getRemove_flg() == 0)
					avalibleLength++;
			}

			row = count_sheet.getRow(1);
			// 计划工程
			row.getCell(2).setCellValue("总组");
			// 在线数量
			row.getCell(5).setCellValue(shMapper.getAtline(treatDate));

			row = count_sheet.getRow(2);
			// 计划安排日期
			row.getCell(2).setCellValue(treatDate);
			// 同意数量
			row.getCell(5).setCellValue(shMapper.getAgreed(treatDate));

			row = count_sheet.getRow(3);
			// 初始计划排入数
			row.getCell(2).setCellValue(firstLength);
			// 投线数量
			row.getCell(5).setCellValue(shMapper.getInline(treatDate));

			row = count_sheet.getRow(4);
			// 最终计划排入数
			row.getCell(2).setCellValue(avalibleLength);
			// 延误数量
			row.getCell(5).setCellValue(shMapper.getDelay(treatDate));

			row = count_sheet.getRow(5);
			// 当天计划完成数
			row.getCell(2).setCellValue(completeLength);
			// BO数量
			row.getCell(5).setCellValue(shMapper.getBo(treatDate));

			row = count_sheet.getRow(6);
			// 当天计划完成率
			// LT达成率
			Double dLt = shMapper.getLt(treatDate);
			if (dLt == null) {
				row.getCell(5).setCellValue(" - ");
			} else {
				row.getCell(5).setCellValue(dLt);
			}

			row = count_sheet.getRow(7);
			// 本表最后更新日期
			row.getCell(2).setCellValue(today);

			count_sheet.setForceFormulaRecalculation(true);

			// 保存文件
			out = new FileOutputStream(cachePath);
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
	}

	private boolean setExcelRowValue(Date treatDate, ScheduleHistoryEntity scheduleHistory, int iLine, HSSFSheet sheet,
			HSSFCellStyle styleAlignCenter, HSSFCellStyle styleAlignLeft, ScheduleHistoryMapper shMapper) {
		boolean completed = false;

		HSSFRow row = sheet.createRow(iLine);
		HSSFCell cell = row.createCell(0);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(iLine);

		cell = row.createCell(1);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(scheduleHistory.getSorc_no());

		cell = row.createCell(2);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(scheduleHistory.getCategory_name());

		cell = row.createCell(3);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(scheduleHistory.getModel_name());

		cell = row.createCell(4);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(scheduleHistory.getSerial_no());

		cell = row.createCell(5);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(CodeListUtils.getValue("in_schedule_means", "" + scheduleHistory.getIn_schedule_means()));

		cell = row.createCell(6);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(scheduleHistory.getSection_name());

		cell = row.createCell(7);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(CodeListUtils.getValue("materail_level", "" + scheduleHistory.getLevel()));

		cell = row.createCell(8);
		cell.setCellStyle(styleAlignLeft);
		cell.setCellValue(CodeListUtils.getValue("material_ocm", "" + scheduleHistory.getOcm()));

		Date outline_date = scheduleHistory.getOutline_date();
		if (outline_date == null)
			outline_date = treatDate;
		Boolean scheduledExpire = DateUtil.compareDate(outline_date, scheduleHistory.getScheduled_expire_date()) > 0;
		Integer comparePartialExpireDate = DateUtil.compareDate(outline_date, scheduleHistory.getPartial_expire_date());
		Boolean partialExpire = comparePartialExpireDate != null && comparePartialExpireDate > 0;
		Boolean planExpire = DateUtil.compareDate(outline_date, scheduleHistory.getScheduled_date()) > 0;
		completed = !planExpire;

		cell = row.createCell(9);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(getDateFormat(scheduleHistory.getAgreed_date()));

		cell = row.createCell(10);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(getDateFormat(scheduleHistory.getScheduled_expire_date()));

		cell = row.createCell(11);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(getDateFormat(scheduleHistory.getArrival_plan_date()));

		cell = row.createCell(12);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(getDateFormat(scheduleHistory.getPartial_expire_date()));

		cell = row.createCell(13);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(scheduleHistory.getScheduled_expedited() == 0 ? "" : "加急");

		cell = row.createCell(14);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(Boolean.TRUE.equals(planExpire) ? "计划未完成" : "计划完成");

		String textExpire = "";
		if (Boolean.TRUE.equals(scheduledExpire)) {
			textExpire += RvsConsts.TIME_LIMIT + "天延迟";
		}
		if (Boolean.TRUE.equals(partialExpire)) {
			textExpire += (textExpire.length() > 0 ? "\r\n" : "") + "入库4天延迟";
		}

		cell = row.createCell(15);
		cell.setCellStyle(styleAlignCenter);
		cell.setCellValue(textExpire);

		if (planExpire) {
			MaterialEntity mEntity =shMapper.getNewStatusOfMaterial(scheduleHistory.getMaterial_id());
			// 最新计划安排日
			cell = row.createCell(16);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(DateUtil.toString(mEntity.getScheduled_assign_date(), DateUtil.DATE_PATTERN));
			// 进展工位
			cell = row.createCell(17);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(mEntity.getProcessing_position());
			// 故障理由
			cell = row.createCell(18);
			cell.setCellStyle(styleAlignLeft);
			cell.setCellValue(mEntity.getScheduled_manager_comment());
			// 最新入库预定日
			cell = row.createCell(19);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(DateUtil.toString(mEntity.getArrival_plan_date(), DateUtil.DATE_PATTERN));
			// 最新加急状态
			cell = row.createCell(20);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(mEntity.getScheduled_expedited() == 0 ? "" : "加急");
		} else {
			cell = row.createCell(16);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(" - ");
			cell = row.createCell(17);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(" - ");
			cell = row.createCell(18);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(" - ");
			cell = row.createCell(19);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(" - ");
			cell = row.createCell(20);
			cell.setCellStyle(styleAlignCenter);
			cell.setCellValue(" - ");
		}

		return completed;
	}

	private String getDateFormat(Date date) {
		if (date == null)
			return " - ";
		return DateUtil.toString(date, "MM-dd");
	}

	private void collectNextDay(Date today, Date nextDay, SqlSessionManager conn) {

		// 今天不是工作日Pass
		if (RvsUtils.isHoliday(today, conn))
			return;

		ScheduleHistoryMapper dao = conn.getMapper(ScheduleHistoryMapper.class);
		// 删除掉第二天记录中,已经完成的维修对象
		dao.removeCompleteNow(nextDay);

		dao.setFirstSchedule(nextDay);
	}

	/**
	 * 顺延
	 * 
	 * @param today
	 * @param conn
	 */
	private void makePostpone(Date today, Date nextDay, SqlSessionManager conn) {

		// 自动顺延
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		int c = dao.setSchedulePostpone(today, nextDay);
		_log.info(c + " Updated！");
		ScheduleHistoryMapper shMapper = conn.getMapper(ScheduleHistoryMapper.class);
		try {
			shMapper.setPostponeSchedule(nextDay);
		}catch(Exception e) {
			_log.error(e.getMessage());
		}
	}

	public static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}

	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

//		today.set(Calendar.MONTH, Calendar.APRIL);
		today.set(Calendar.DATE, 29);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		PathConsts.BASE_PATH = "D:\\rvs";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
		;
		PathConsts.REPORT = "\\Reports";

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		SchedulePostponeJob job = new SchedulePostponeJob();

		// 明天
		HolidayMapper hMapper = conn.getMapper(HolidayMapper.class);
		Date nextDay = hMapper.getNextWorkDate(today.getTime());

		// 当天记录
		job.collectNextDay(today.getTime(), nextDay, conn);

		// 顺延
		job.makePostpone(today.getTime(), nextDay, conn);

		// 排时段计划
		job.sortToPeriod(nextDay, conn);

		// 建立文档
//		job.createReports(today, conn);
	}

	private void sortToPeriod(Date nextDay, SqlSessionManager conn) {
		
		ScheduleHistoryMapper mapper = conn.getMapper(ScheduleHistoryMapper.class);
		List<Map<String, Object>> rets = mapper.getScheduleSort(nextDay);
		// 每个工程里的总维修独享列表
		Map<String, List<Map<String, Object>>> sectionSorts = new HashMap<String, List<Map<String, Object>>>(); 
		Map<String, Set<ScheduleMaterial>> sectionSortResults1 = new HashMap<String, Set<ScheduleMaterial>>(); 
		Map<String, Set<ScheduleMaterial>> sectionSortResults2 = new HashMap<String, Set<ScheduleMaterial>>(); 
		Map<String, Set<ScheduleMaterial>> sectionSortResults3 = new HashMap<String, Set<ScheduleMaterial>>(); 
		Map<String, Set<ScheduleMaterial>> sectionSortResults4 = new HashMap<String, Set<ScheduleMaterial>>(); 

		for (Map<String, Object> ret : rets) {
			String section_id = ""+ret.get("section_id");
			if (!sectionSorts.containsKey(section_id)) {
				List<Map<String, Object>> neo = new ArrayList<Map<String, Object>>(); 
				sectionSorts.put(section_id, neo);
			}
			sectionSorts.get(section_id).add(ret);
		}

		List<Map<String, Object>> capacityRecords = mapper.getCapacity();
		Map<String, Integer> capacitiesOfSectionCategory = new HashMap<String, Integer>();
		Map<String, Integer> capacitiesOfSection = new HashMap<String, Integer>();
		// 取得产量
		for (Map<String, Object> capacityRecord : capacityRecords) {
			String section_id = "" + capacityRecord.get("section_id");
			String key = capacityRecord.get("category_id") + "|" + section_id;
			Integer val = (Integer)capacityRecord.get("upper_limit");
			capacitiesOfSectionCategory.put(key, val);
			if (capacitiesOfSection.containsKey(section_id)) {
				capacitiesOfSection.put(section_id, capacitiesOfSection.get(section_id) + val);
			} else {
				capacitiesOfSection.put(section_id, val);
			}
			_log.info(key +": " + val
					+ " 1: " + Math.ceil(val * 0.1)
					+ " 2: " + Math.ceil(val * 0.2)
					+ " 3: " + Math.ceil(val * 0.3)
					+ " 4: " + Math.ceil(val * 0.4))
					;
		}

//		for (String section_id : capacitiesOfSection.keySet()) {
//			Integer val = capacitiesOfSection.get(section_id);
//			_log.info(section_id +": " + val
//					+ " 1: " + Math.ceil(val * 0.1)
//					+ " 2: " + Math.ceil(val * 0.2)
//					+ " 3: " + Math.ceil(val * 0.3)
//					+ " 4: " + Math.ceil(val * 0.4))
//					;
//		}

		for (String section_id : sectionSorts.keySet()) {
			List<Map<String, Object>> sectionSort = sectionSorts.get(section_id);
			Integer val = sectionSort.size();
			_log.info(section_id +": " + val
					+ " 1: " + Math.ceil(val * 0.1)
					+ " 2: " + Math.ceil(val * 0.2)
					+ " 3: " + Math.ceil(val * 0.3)
					+ " 4: " + Math.ceil(val * 0.4))
					;
		}

		for (String section_id : sectionSorts.keySet()) {
			List<Map<String, Object>> sectionSort = sectionSorts.get(section_id);
			// Map<Integer, Map<String, Object>> tm = new TreeMap<Integer, Map<String, Object>>();
			Set<ScheduleMaterial> sectionSortResult1 = new TreeSet<ScheduleMaterial>();
			Set<ScheduleMaterial> sectionSortResult2 = new TreeSet<ScheduleMaterial>();
			Set<ScheduleMaterial> sectionSortResult3 = new TreeSet<ScheduleMaterial>();
			Set<ScheduleMaterial> sectionSortResult4 = new TreeSet<ScheduleMaterial>();

			for (int i=0;i<sectionSort.size();i++) {
				ScheduleMaterial smObj = new ScheduleMaterial();

				Map<String, Object> ret = sectionSort.get(i);
				int seq = (sectionSort.size() - i) * 10;

				Integer direct_flg = ((BigDecimal)ret.get("direct_flg")).intValue();
				Integer scheduled_expedited = ((Long)ret.get("scheduled_expedited")).intValue();
				if (direct_flg == 1 && scheduled_expedited == 1) {
					seq += sectionSort.size() * 4 + 15;
				} else if (direct_flg == 1) {
					seq += 15;
				} else if (scheduled_expedited == 1) {
					seq += 15;
				}

				Integer todaydiff = (Integer)ret.get("todaydiff");
				if (todaydiff == 0) {
					seq += 20;
				} else if (todaydiff > 0 && todaydiff < 3) {
					seq -= sectionSort.size() * 5;
				} else if (todaydiff < -3) {
					seq -= 20;
				}

				Integer reason = ((Long)ret.get("reason")).intValue();
				Integer append = ((BigDecimal)ret.get("append")).intValue();
				if (reason > 0) {
					seq -= 20;
				}
				if (append > 0) {
					seq -= 20;
				}

				String material_id = ret.get("material_id").toString();

				smObj.material_id = material_id;
				smObj.seq = seq;
				smObj.categroy_id = "" + ret.get("CATEGORY_ID");
				smObj.remain_time = ((Long)ret.get("remain_time")).intValue();

				if (smObj.remain_time <= PERIOD_A_THROUGH)
					sectionSortResult1.add(smObj);
				else if (smObj.remain_time <= PERIOD_B_THROUGH)
					sectionSortResult2.add(smObj);
				else if (smObj.remain_time <= PERIOD_C_THROUGH)
					sectionSortResult3.add(smObj);
				else
					sectionSortResult4.add(smObj);
			}
			sectionSortResults1.put(section_id, sectionSortResult1);
			sectionSortResults2.put(section_id, sectionSortResult2);
			sectionSortResults3.put(section_id, sectionSortResult3);
			sectionSortResults4.put(section_id, sectionSortResult4);
		}

		for (String section_id : sectionSortResults1.keySet()) {
			Set<ScheduleMaterial> sectionSortResult1 = sectionSortResults1.get(section_id);
			Set<ScheduleMaterial> sectionSortResult2 = sectionSortResults2.get(section_id);
			Set<ScheduleMaterial> sectionSortResult3 = sectionSortResults3.get(section_id);
			Set<ScheduleMaterial> sectionSortResult4 = sectionSortResults4.get(section_id);

			Set<ScheduleMaterial> mvs = new HashSet<ScheduleMaterial>();
			Map<String, Integer> caps = new HashMap<String, Integer>();

			int countEndoEye = 0;

			for (ScheduleMaterial smObj : sectionSortResult1) {
				if (!caps.containsKey(smObj.categroy_id)) {
					Integer capacity = capacitiesOfSectionCategory.get(smObj.categroy_id + "|" + section_id);
					if (capacity == null) capacity = 0;
					caps.put(smObj.categroy_id, (int) Math.ceil(capacity * 0.2));
					if (caps.get(smObj.categroy_id) == null) caps.put(smObj.categroy_id, 0);
				}
				// 超出型号产能
				if (caps.get(smObj.categroy_id) <= 0) {
					mvs.add(smObj);
					continue;
				}
				caps.put(smObj.categroy_id, caps.get(smObj.categroy_id) - 1);
				if (countEndoEye > 1) {
					mvs.add(smObj);
					continue;
				}
				if ("16".equals(smObj.categroy_id)) {
					countEndoEye++;
				}
			}
			for (ScheduleMaterial mv : mvs) {
				sectionSortResult1.remove(mv);
				sectionSortResult2.add(mv);
			}
			mvs.clear();

			int periodCap = (int) Math.ceil(sectionSorts.get(section_id).size() * 0.2);
			if (sectionSortResult1.size() > periodCap) {
				int i = 0;
				for (ScheduleMaterial smObj : sectionSortResult1) {
					if (i >= periodCap) mvs.add(smObj);
					i++;
				}
				for (ScheduleMaterial mv : mvs) {
					sectionSortResult1.remove(mv);
					sectionSortResult2.add(mv);
				}
				mvs.clear();
			}

			caps.clear();
			for (ScheduleMaterial smObj : sectionSortResult2) {
				if (!caps.containsKey(smObj.categroy_id)) {
					Integer capacity = capacitiesOfSectionCategory.get(smObj.categroy_id + "|" + section_id);
					if (capacity == null) capacity = 0;
					caps.put(smObj.categroy_id, (int) Math.ceil(capacity * 0.1));
					if (caps.get(smObj.categroy_id) == null) caps.put(smObj.categroy_id, 0);
				}
				// 超出型号产能
				if (caps.get(smObj.categroy_id) <= 0) {
					mvs.add(smObj);
					continue;
				}
				caps.put(smObj.categroy_id, caps.get(smObj.categroy_id) - 1);
				if (countEndoEye > 1) {
					mvs.add(smObj);
					continue;
				}
				if ("16".equals(smObj.categroy_id)) {
					countEndoEye++;
				}
			}
			for (ScheduleMaterial mv : mvs) {
				sectionSortResult2.remove(mv);
				sectionSortResult3.add(mv);
			}
			mvs.clear();
			periodCap = (int) Math.ceil(sectionSorts.get(section_id).size() * 0.1);
			if (sectionSortResult2.size() > periodCap) {
				int i = 0;
				for (ScheduleMaterial smObj : sectionSortResult2) {
					if (i >= periodCap) mvs.add(smObj);
					i++;
				}
				for (ScheduleMaterial mv : mvs) {
					sectionSortResult2.remove(mv);
					sectionSortResult3.add(mv);
				}
				mvs.clear();
			}

			caps.clear();
			for (ScheduleMaterial smObj : sectionSortResult3) {
				if (!caps.containsKey(smObj.categroy_id)) {
					Integer capacity = capacitiesOfSectionCategory.get(smObj.categroy_id + "|" + section_id);
					if (capacity == null) capacity = 0;
					caps.put(smObj.categroy_id, (int) Math.ceil(capacity * 0.3));
					if (caps.get(smObj.categroy_id) == null) caps.put(smObj.categroy_id, 0);
				}
				// 超出型号产能
				if (caps.get(smObj.categroy_id) <= 0) {
					mvs.add(smObj);
					continue;
				}
				caps.put(smObj.categroy_id, caps.get(smObj.categroy_id) - 1);
			}
			for (ScheduleMaterial mv : mvs) {
				sectionSortResult3.remove(mv);
				sectionSortResult4.add(mv);
			}
			mvs.clear();
			periodCap = (int) Math.ceil(sectionSorts.get(section_id).size() * 0.3);
			if (sectionSortResult3.size() > periodCap) {
				int i = 0;
				for (ScheduleMaterial smObj : sectionSortResult3) {
					if (i >= periodCap) mvs.add(smObj);
					i++;
				}
				for (ScheduleMaterial mv : mvs) {
					sectionSortResult3.remove(mv);
					sectionSortResult4.add(mv);
				}
				mvs.clear();
			}

			for (ScheduleMaterial smObj : sectionSortResult1) {
				_log.info("material_id: " + smObj.material_id + "\t" + smObj.seq + "\t"
				+ smObj.categroy_id + "\t"
				+ smObj.remain_time);
			}
			_log.info("===========++A++===========");
			for (ScheduleMaterial smObj : sectionSortResult2) {
				_log.info("material_id: " + smObj.material_id + "\t" + smObj.seq + "\t"
				+ smObj.categroy_id + "\t"
				+ smObj.remain_time);
			}
			_log.info("===========++B++===========");
			for (ScheduleMaterial smObj : sectionSortResult3) {
				_log.info("material_id: " + smObj.material_id + "\t" + smObj.seq + "\t"
				+ smObj.categroy_id + "\t"
				+ smObj.remain_time);
			}
			_log.info("===========++C++===========");
			for (ScheduleMaterial smObj : sectionSortResult4) {
				_log.info("material_id: " + smObj.material_id + "\t" + smObj.seq + "\t"
				+ smObj.categroy_id + "\t"
				+ smObj.remain_time);
			}
			_log.info("===========++"+section_id+"++===========");

			for (ScheduleMaterial smObj : sectionSortResult1) {
				ScheduleHistoryEntity entity = new ScheduleHistoryEntity();
				entity.setMaterial_id(smObj.material_id);
				entity.setScheduled_date(nextDay);
				entity.setPlan_day_period(1);
				mapper.updateScheduleSort(entity);
			}
			for (ScheduleMaterial smObj : sectionSortResult2) {
				ScheduleHistoryEntity entity = new ScheduleHistoryEntity();
				entity.setMaterial_id(smObj.material_id);
				entity.setScheduled_date(nextDay);
				entity.setPlan_day_period(2);
				mapper.updateScheduleSort(entity);
			}
			for (ScheduleMaterial smObj : sectionSortResult3) {
				ScheduleHistoryEntity entity = new ScheduleHistoryEntity();
				entity.setMaterial_id(smObj.material_id);
				entity.setScheduled_date(nextDay);
				entity.setPlan_day_period(3);
				mapper.updateScheduleSort(entity);
			}
			for (ScheduleMaterial smObj : sectionSortResult4) {
				ScheduleHistoryEntity entity = new ScheduleHistoryEntity();
				entity.setMaterial_id(smObj.material_id);
				entity.setScheduled_date(nextDay);
				entity.setPlan_day_period(4);
				mapper.updateScheduleSort(entity);
			}
		}
	}

	private class ScheduleMaterial implements Comparable<ScheduleMaterial> {
		private String material_id;
		private int seq = 0;
		private String categroy_id;
		private int remain_time;

		@Override
		public int compareTo(ScheduleMaterial o) {
			int cm = o.seq - this.seq;
			if (cm != 0)
				return cm;
			else
				return this.material_id.compareTo(o.material_id);
		}
		
	}
}
