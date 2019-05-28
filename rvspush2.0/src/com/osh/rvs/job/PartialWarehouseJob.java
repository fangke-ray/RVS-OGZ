package com.osh.rvs.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.PartialWarehouseEntity;
import com.osh.rvs.mapper.push.PartialWarehouseMapper;
import com.osh.rvs.mapper.push.UserDefineCodesMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class PartialWarehouseJob implements Job {
	public static Logger _log = Logger.getLogger("PartialWarehouseJob");

	private final String MIDDLE_LINE = "一";

	/** E1：NS 工程出库 **/
	private final Integer NS_STANDARD_TIME = 6;
	/** E2：分解工程出库 **/
	private final Integer DEC_STANDARD_TIME = 9;
	
	/** 每日工作时间（475分钟） **/
	private final Integer WORK_TIME = 475;
	/** 每日工作+加班时间（565分钟） **/
	private final Integer WORK_OVER_TIME = 565;
	/** 一分钟（60000毫秒）**/
	private final Integer ONE_MINUTE_MILLISECOND = 60000;
	/** 十分钟（600000毫秒）**/
	private final Integer TEN_MINUTE_MILLISECOND = ONE_MINUTE_MILLISECOND * 10;
	/** 四十五分钟（2700000毫秒） **/
	private final Integer FORTY_FIVE_MINUTE_MILLISECOND = ONE_MINUTE_MILLISECOND * 45;
	/** 一小时（3600000毫秒）**/
	private final Integer ONE_HOUR_MILLISECOND = ONE_MINUTE_MILLISECOND * 60;
	/** 4位小数精度 **/
	private final Integer SCALE_FOUR = 4;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar monthStart = Calendar.getInstance();

		_log.info("PartialWarehouseJob: " + jobKey + " executing at " + monthStart);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		try {
			partialWarehouseReport(monthStart, conn);
		} catch (Exception e) {
			_log.error("partialWarehouseReport:" + e.getMessage());
		}

	}

	private void partialWarehouseReport(Calendar monthStart, SqlSession conn) {
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);
		UserDefineCodesMapper userDefineCodesMapper = conn.getMapper(UserDefineCodesMapper.class);

		List<PartialWarehouseEntity> listBeans = dao.searchMonthWorkRecord(DateUtil.toString(RvsUtils.getMonthStartDate(monthStart), DateUtil.DATE_PATTERN));
		int length = listBeans.size();

		// 模板文件
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "零件出入库工时月报表模板.xlsx";
		// 生成文件的文件名称
		String cacheFilename = "零件出入库工时月报表" + DateUtil.toString(monthStart.getTime(), "yyyy-MM") + ".xlsx";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\works\\" + cacheFilename;

		InputStream in = null;
		OutputStream out = null;

		try {
			if (length > 0) {
				try {
					FileUtils.copyFile(new File(path), new File(cachePath));
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}

				// 读取文件
				in = new FileInputStream(cachePath);

				// 创建xls文件
				Workbook work = new XSSFWorkbook(in);

				// 取得出入库明细Sheet
				Sheet detailSheet = work.getSheet("出入库明细");

				// 取得每日汇总Sheet
				Sheet dailyCollectSheet = work.getSheet("高雁梅每日汇总");
				Sheet dailyCollectSheet2 = work.getSheet("叶昭杏每日汇总");

				// 获取用户自定义参数
				Map<String, BigDecimal> userDefineMap = getUserDefineCodes(userDefineCodesMapper);

				// 创建样式
				Map<String, CellStyle> styleMap = this.createCellStyle(work);

				// 计算公式
				FormulaEvaluator formulaEvaluator = work.getCreationHelper().createFormulaEvaluator();
				
				// 当月最后一天
				int lastDay = monthStart.getActualMaximum(Calendar.DATE);
				// 当天
				int curDay = monthStart.get(Calendar.DATE);
				
				// 月底标记
				boolean isMonthEnd = false;
				// 月底
				if(curDay == lastDay){
					isMonthEnd = true;
				}
				
				// 出入库明细
				Map<String,Map<String, LinkedHashMap<String, Double>>> rateMap = setDetailList(isMonthEnd,listBeans, userDefineMap.get("bdPartialReceptMoveCost"), detailSheet, styleMap, formulaEvaluator, dao);

				// 月底
				if(isMonthEnd){
					// 高雁梅每日汇总
					setDailyCollect(monthStart, "197",rateMap,styleMap, userDefineMap, dailyCollectSheet, dao);

					// 叶昭杏每日汇总
					setDailyCollect(monthStart, "198",rateMap,styleMap, userDefineMap, dailyCollectSheet2, dao);
				}else{
					// 删除Sheet
					work.removeSheetAt(work.getSheetIndex(dailyCollectSheet));
					work.removeSheetAt(work.getSheetIndex(dailyCollectSheet2));
				}

				work.setForceFormulaRecalculation(true);

				out = new FileOutputStream(cachePath);
				work.write(out);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 每日汇总
	 *
	 * @param listBeans
	 * @param sheet
	 */
	private void setDailyCollect(Calendar monthStart,String operatorId, Map<String,Map<String, LinkedHashMap<String, Double>>> rateMap,Map<String, CellStyle> styleMap, Map<String, BigDecimal> userDefineMap, Sheet sheet, PartialWarehouseMapper dao) {
		Row row = null;
		Row row2 = null;
		Cell cell = null;
		PartialWarehouseEntity partialWarehouseEntity = null;
		List<PartialWarehouseEntity> dailyWorkRecordList = null;
		
		// 出入库明细Sheet统计的当天负荷率和当天能率
		Map<String, LinkedHashMap<String, Double>> dailyLoadRateMap = rateMap.get("dailyLoadRate");
		Map<String, LinkedHashMap<String, Double>> dailyEnergyRateMap = rateMap.get("dailyEnergyRate");
		
		String jobNo = "";
		if("197".equals(operatorId)){
			jobNo = "00056";
		}else if("198".equals(operatorId)){
			jobNo = "30301";
		}
		
		// 每个人每日合计负荷率
		LinkedHashMap<String, Double> personalDailyRateMap = dailyLoadRateMap.get(jobNo);
		// 每个人每日合计能率
		LinkedHashMap<String, Double> personalDailyEnergyRateMap = dailyEnergyRateMap.get(jobNo);
		
		// 仓管人员负荷率警报标志下线
		double lowLever = userDefineMap.get("strLowLever").divide(new BigDecimal(100)).doubleValue();
		
		// 仓管人员能率警报标志下线
		double efLowLever =  userDefineMap.get("efLowLever").divide(new BigDecimal(100)).doubleValue();

		int colIndex = 2;

		// 每天每种作业类型用时
		List<PartialWarehouseEntity> list = dao.countDailySpendTime(operatorId,DateUtil.toString(RvsUtils.getMonthStartDate(monthStart), DateUtil.DATE_PATTERN));

		Map<Date, List<PartialWarehouseEntity>> map = new LinkedHashMap<Date, List<PartialWarehouseEntity>>();
		
		// 每天未达成目标合计负荷率
		Map<Date, Double> dailyUnLoadRateMap = new LinkedHashMap<Date, Double>();
		
		//每天未达成目标合计能率
		Map<Date,Double> dailyUnEnergyRateMap = new LinkedHashMap<Date,Double>();

		for (int i = 0; i < list.size(); i++) {
			partialWarehouseEntity = list.get(i);

			if (map.containsKey(partialWarehouseEntity.getFinish_time())) {
				map.get(partialWarehouseEntity.getFinish_time()).add(partialWarehouseEntity);
			} else {
				List<PartialWarehouseEntity> ls = new ArrayList<PartialWarehouseEntity>();
				ls.add(partialWarehouseEntity);
				map.put(partialWarehouseEntity.getFinish_time(), ls);
			}
		}

		BigDecimal monthWorkTime = new BigDecimal(0);

		for (Date finishTime : map.keySet()) {
			// 总计用时（分钟）
			BigDecimal totalSpendTime = new BigDecimal(0);
			// 标准用时（分钟）
			BigDecimal totalStandardTime = new BigDecimal(0);

			// 加班标记
			boolean flg = false;

			// 获取第一行
			row = sheet.getRow(0);

			cell = row.createCell(colIndex);
			cell.setCellValue(finishTime);
			cell.setCellStyle(styleMap.get("dayStyle"));

			// 设置列宽
			sheet.setColumnWidth(colIndex, 256 * 11);

			// 创建第二行到第二十行单元格
			for (int rowIndex = 1; rowIndex <= 19; rowIndex++) {
				row = sheet.getRow(rowIndex);
				// 创建单元格对象
				CellUtil.createCell(row, colIndex, "一", row.getCell(colIndex - 1).getCellStyle());
			}

			List<PartialWarehouseEntity> ls = map.get(finishTime);
			for (int i = 0; i < ls.size(); i++) {
				BigDecimal standardTime = new BigDecimal(0);
				partialWarehouseEntity = ls.get(i);

				// 作业内容
				Integer productionType = partialWarehouseEntity.getProduction_types();

				// 实际用时
				Integer spendTime = partialWarehouseEntity.getSpendTime();

				// 加班标记
				Integer overtimeFlg = partialWarehouseEntity.getOvertime_flg();

				if (flg == false && overtimeFlg > 0) {
					flg = true;
				}

				dailyWorkRecordList = dao.searchDailyWorkRecord(operatorId,productionType.toString(), DateUtil.toString(finishTime, DateUtil.DATE_PATTERN));

				switch (productionType) {
				case 10://A：收货
					row = sheet.getRow(1);
					row2 = sheet.getRow(2);

					standardTime = userDefineMap.get("bdPartialReceptMoveCost").multiply(new BigDecimal(dailyWorkRecordList.size()));
					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						String key = entity.getKey();
						if (!CommonStringUtil.isEmpty(key)) {
							// 收货标准工时
							BigDecimal receptStandardTime = dao.countReceptStandardTime(key);
							standardTime = standardTime.add(receptStandardTime);
						}
					}

					break;
				case 20://B1：核对+上架
					row = sheet.getRow(3);
					row2 = sheet.getRow(4);

					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						BigDecimal collationStandardTime = dao.countCollationStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collationStandardTime);
						
						// 拆盒
						BigDecimal collectCase = dao.countCollectCaseStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collectCase);
					}

					break;
				case 21://B2：核对
					row = sheet.getRow(5);
					row2 = sheet.getRow(6);

					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						BigDecimal collationStandardTime = dao.countCollationStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collationStandardTime);
					}

					break;
				case 30://C：分装
					row = sheet.getRow(7);
					row2 = sheet.getRow(8);

					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						BigDecimal collationStandardTime = dao.countUnPackStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collationStandardTime);
					}

					break;
				case 40://D：上架
					row = sheet.getRow(9);
					row2 = sheet.getRow(10);

					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						BigDecimal collationStandardTime = dao.countOnShelfStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collationStandardTime);
					}
					break;
				case 50://E1：NS 工程出库
					row = sheet.getRow(11);
					row2 = sheet.getRow(12);

					standardTime = new BigDecimal(dailyWorkRecordList.size() * NS_STANDARD_TIME);
					break;
				case 51://E2：分解工程出库
					row = sheet.getRow(13);
					row2 = sheet.getRow(14);

					standardTime = new BigDecimal(dailyWorkRecordList.size() * DEC_STANDARD_TIME);
					break;
				case 52://E3：其他维修出库
					row = sheet.getRow(15);
					row2 = sheet.getRow(16);

					standardTime = new BigDecimal(spendTime);//标准时间和实际时间一样
					break;
				case 99://O：其它
					row = sheet.getRow(17);
					standardTime = new BigDecimal(spendTime);//标准时间和实际时间一样
					break;
				default:
					break;
				}

				// 用时
				row.getCell(colIndex).setCellValue(spendTime);

				// 能率
				if (productionType != 99) {
					double percent = standardTime.divide(new BigDecimal(spendTime), SCALE_FOUR, RoundingMode.HALF_UP).doubleValue();
					row2.getCell(colIndex).setCellValue(percent);
				}

				totalSpendTime = totalSpendTime.add(new BigDecimal(spendTime));
				totalStandardTime = totalStandardTime.add(standardTime);
			}

			// 一天工作时间(分钟)
			BigDecimal workTime = null;

			double percent = 0;

			if (flg) {// 加班
				workTime = new BigDecimal(WORK_OVER_TIME);
			} else {
				workTime = new BigDecimal(WORK_TIME);
			}

			monthWorkTime = monthWorkTime.add(workTime);

			// 合计负荷率
			percent = personalDailyRateMap.get(DateUtil.toString(finishTime, DateUtil.DATE_PATTERN));
			sheet.getRow(18).getCell(colIndex).setCellValue(percent);
			
			//合计负荷率低于“仓管人员负荷率警报标志下线”
			if(percent < lowLever){
				dailyUnLoadRateMap.put(finishTime, percent);
			}

			// 合计能率
			percent = personalDailyEnergyRateMap.get(DateUtil.toString(finishTime, DateUtil.DATE_PATTERN));
			sheet.getRow(19).getCell(colIndex).setCellValue(percent);
			
			//合计能率低于“仓管人员能率警报标志下线”
			if(percent < efLowLever){
				dailyUnEnergyRateMap.put(finishTime, percent);
			}

			//负荷率警报标志下线
			row = sheet.getRow(20);
			if(row == null) row = sheet.createRow(20);
			cell = row.createCell(colIndex);
			cell.setCellValue(lowLever);
			cell.setCellStyle(styleMap.get("percentStyle"));

			//能率警报标志下线
			row = sheet.getRow(21);
			if(row == null) row = sheet.createRow(21);
			cell = row.createCell(colIndex);
			cell.setCellValue(efLowLever);
			cell.setCellStyle(styleMap.get("percentStyle"));

			row = sheet.getRow(22);
			if(row == null) row = sheet.createRow(22);
			cell = row.createCell(colIndex);

			//未记录时间
			double noRecordTime = workTime.subtract(totalSpendTime).doubleValue();
			if(noRecordTime < 0) noRecordTime = 0;
			cell.setCellValue(noRecordTime);

			colIndex++;
		}


		//隐藏列
		for(int i = colIndex;i <= 32;i++){
			// 设置列宽为零
			sheet.setColumnWidth(i, 0);
		}

		row = sheet.getRow(24);
		if(row == null) row = sheet.createRow(24);
		cell = row.createCell(11);
		//总计工作时间
		cell.setCellValue(monthWorkTime.doubleValue());
		
		//（日次）负荷率未达成目标跟踪分析
		dailyUnReachLowLever(sheet, styleMap, userDefineMap, dailyUnLoadRateMap);
		
		//（日次）能率未达成目标跟踪分析
		dailyUnReachEFLowLever(sheet, styleMap, userDefineMap, dailyUnEnergyRateMap);
	}
	
	/**
	 * （日次）负荷率未达成目标跟踪分析
	 * @param sheet
	 * @param styleMap 样式
	 * @param userDefineMap 自定义参数
	 * @param map 每日未达成目标数据集
	 */
	private void dailyUnReachLowLever(Sheet sheet, Map<String, CellStyle> styleMap,Map<String, BigDecimal> userDefineMap,Map<Date, Double> map){
		Row row = null;
		Cell cell = null;
		
		//不存在负荷率未达成目标
		if(map.isEmpty()){
			for(int i = 108;i <= 147;i++){
				row = sheet.getRow(i);
				row.setZeroHeight(true);
			}
			return;
		}
		
		sheet.getRow(109).getCell(0).setCellValue("目标："+ userDefineMap.get("strLowLever").doubleValue() + "%");
		
		//（日次）负荷率未达成目标跟踪分析开始行索引
		int rowIndex = 111;
		//结束行索引
		int rowEndIndex = rowIndex + 30;
		
		//（日次）负荷率未达成目标跟踪分析
		for(Date strDate :map.keySet()){
			row = sheet.getRow(rowIndex);
			
			//发生日
			cell = row.getCell(0);
			cell.setCellValue(strDate);
			cell.setCellStyle(styleMap.get("dayStyle"));
			
			//负荷率
			cell = row.getCell(1);
			cell.setCellValue(map.get(strDate));
			cell.setCellStyle(styleMap.get("percentStyle"));
			rowIndex ++;
		}
		
		for(int i = rowIndex;i <= rowEndIndex;i++){
			row = sheet.getRow(i);
			//隐藏行
			row.setZeroHeight(true);
		}
	}
	
	/**
	 * （日次）能率未达成目标跟踪分析
	 * @param sheet
	 * @param styleMap 样式
	 * @param userDefineMap 自定义参数
	 * @param map 每日未达成目标数据集
	 */
	private void dailyUnReachEFLowLever(Sheet sheet, Map<String, CellStyle> styleMap,Map<String, BigDecimal> userDefineMap,Map<Date, Double> map){
		Row row = null;
		Cell cell = null;
		
		//不存在能率未达成目标
		if(map.isEmpty()){
			for(int i = 150;i <= 189;i++){
				row = sheet.getRow(i);
				row.setZeroHeight(true);
			}
			return;
		}
		
		sheet.getRow(151).getCell(0).setCellValue("目标："+ userDefineMap.get("efLowLever").doubleValue() + "%");
		
		//（日次）能率未达成目标跟踪分析开始行索引
		int rowIndex = 153;
		//结束行索引
		int rowEndIndex = rowIndex + 30;

		//（日次）能率未达成目标跟踪分析
		for(Date strDate :map.keySet()){
			row = sheet.getRow(rowIndex);
			
			//发生日
			cell = row.getCell(0);
			cell.setCellValue(strDate);
			cell.setCellStyle(styleMap.get("dayStyle"));
			
			//能率
			cell = row.getCell(1);
			cell.setCellValue(map.get(strDate));
			cell.setCellStyle(styleMap.get("percentStyle"));
			rowIndex ++;
		}
		
		for(int i = rowIndex;i <= rowEndIndex;i++){
			row = sheet.getRow(i);
			//隐藏行
			row.setZeroHeight(true);
		}
	}
	

	/**
	 * 获取用户自定义参数
	 * @param userDefineCodesMapper
	 * @return
	 */
	private Map<String, BigDecimal> getUserDefineCodes(UserDefineCodesMapper userDefineCodesMapper) {
		Map<String, BigDecimal> listResponse = new HashMap<String, BigDecimal>();

		// 收货搬运移动标准工时
		String value = userDefineCodesMapper.getValue("PARTIAL_RECEPT_MOVE_COST");
		BigDecimal bdPartialReceptMoveCost = null;
		try {
			bdPartialReceptMoveCost = new BigDecimal(value);
		} catch (Exception e) {
			bdPartialReceptMoveCost = new BigDecimal(12);
		}

		listResponse.put("bdPartialReceptMoveCost", bdPartialReceptMoveCost);

		// 仓管人员能率警报标志下线
		value = userDefineCodesMapper.getValue("FACT_PROCESS_EF_LOW_LEVER");
		BigDecimal efLowLever = null;
		try {
			efLowLever = new BigDecimal(value);
		} catch (Exception e) {
			efLowLever = new BigDecimal(80.5);
		}
		listResponse.put("efLowLever", efLowLever);


		// 仓管人员负荷率警报标志下线
		value = userDefineCodesMapper.getValue("FACT_PROCESS_STR_LOW_LEVER");
		BigDecimal strLowLever = null;
		try {
			strLowLever = new BigDecimal(value);
		} catch (Exception e) {
			strLowLever = new BigDecimal(50);
		}
		listResponse.put("strLowLever", strLowLever);

		return listResponse;
	}

	/**
	 * 出入库明细
	 *
	 * @param isMonthEnd 月底标记
	 * @param listBeans 明细数据
	 * @param moveCost 搬箱移动时间
	 * @param sheet
	 * @param styleMap 样式
	 * @param formulaEvaluator 公式计算
	 * @param partialWarehouseMapper
	 */
	private Map<String,Map<String, LinkedHashMap<String, Double>>> setDetailList(boolean isMonthEnd, List<PartialWarehouseEntity> listBeans, BigDecimal moveCost, Sheet sheet, Map<String, CellStyle> styleMap, FormulaEvaluator formulaEvaluator,
			PartialWarehouseMapper partialWarehouseMapper) {

		Row row = null;
		Cell cell = null;

		BigDecimal standardTime = null;

		// 单位
		String unit = "";

		// 其他备注
		String comment = "";

		// 规格种别名称
		String kindName = "";

		// 当天负荷率
		Map<String, Map<Integer, PartialWarehouseEntity>> loadRateMap = new LinkedHashMap<String, Map<Integer, PartialWarehouseEntity>>();

		// 当天能率
		Map<String, Map<Integer, PartialWarehouseEntity>> energyRateMap = new LinkedHashMap<String, Map<Integer, PartialWarehouseEntity>>();

		for (int i = 0; i < listBeans.size(); i++) {
			PartialWarehouseEntity entity = listBeans.get(i);

			String factPfKey = entity.getFact_pf_key();

			// 入库单KEY
			String key = entity.getKey();

			// 作业内容
			Integer productionType = entity.getProduction_type();

			// 工号
			String jobNo = entity.getJob_no();

			// 作业结束时间
			Date finishTime = entity.getFinish_time();

			// 行索引
			int rowIndex = i + 1;

			// 行号
			int rowNum = i + 2;

			// 列索引
			int colIndex = 0;

			// 创建行
			row = sheet.createRow(rowIndex);

			// 日期
			cell = row.createCell(colIndex);
			cell.setCellValue(finishTime);
			cell.setCellStyle(styleMap.get("dateStyle"));

			// 作业者
			CellUtil.createCell(row, ++colIndex, entity.getOperator_name(), styleMap.get("alignLeftStyle"));

			// 工号
			CellUtil.createCell(row, ++colIndex, jobNo, styleMap.get("alignLeftStyle"));

			// 开始
			cell = row.createCell(++colIndex);
			cell.setCellValue(entity.getAction_time());
			cell.setCellStyle(styleMap.get("timeStyle"));

			// 结束
			cell = row.createCell(++colIndex);
			cell.setCellValue(finishTime);
			cell.setCellStyle(styleMap.get("timeStyle"));

			// 进行时间（M）
			cell = row.createCell(++colIndex);
			cell.setCellFormula("ROUND((E" + rowNum + "-" + "D" + rowNum + ")*24*60,1)");
			cell.setCellStyle(styleMap.get("alignRightStyle"));

			// 标准时间 （M）
			cell = row.createCell(++colIndex, Cell.CELL_TYPE_NUMERIC);
			if (productionType == 10) {// A：收货
				// 标准时间
				standardTime = partialWarehouseMapper.countReceptStandardTime(key);
				standardTime = standardTime.add(moveCost);

				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 11) {
				// 标准时间=实际时间
				cell.setCellValue(formulaEvaluator.evaluate(row.getCell(5)).getNumberValue());
			} else if (productionType == 20) {// B1：核对+上架
				standardTime = partialWarehouseMapper.countCollationAndOnShelfStandardTime(factPfKey);
				// 拆盒
				BigDecimal collectCase = partialWarehouseMapper.countCollectCaseStandardTime(factPfKey);
				standardTime = standardTime.add(collectCase);
				
				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 21) {// B2：核对
				standardTime = partialWarehouseMapper.countCollationStandardTime(factPfKey);
				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 30) {// C：分装
				standardTime = partialWarehouseMapper.countUnPackStandardTime(factPfKey);
				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 40) {// D：上架
				standardTime = partialWarehouseMapper.countOnShelfStandardTime(factPfKey);
				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 99) {// O：其它
				cell.setCellValue(MIDDLE_LINE);
			} else if (productionType == 50) {// E1：NS 工程出库
				cell.setCellValue(NS_STANDARD_TIME);
			} else if (productionType == 51) {// E2：分解工程出库
				cell.setCellValue(DEC_STANDARD_TIME);
			} else if (productionType == 52) {// E3：其他维修出库
				cell.setCellValue(MIDDLE_LINE);
			}
			cell.setCellStyle(styleMap.get("alignRightStyle"));
			entity.setStanardtime(CopyByPoi.getStringCellValue((XSSFCell) cell));

			// 能率
			cell = row.createCell(++colIndex);
			if (productionType == 99 || productionType == 52) {// O：其它，E3：其他维修出库
				cell.setCellValue(1);
			} else {
				cell.setCellFormula("IFERROR(IF(G" + rowNum + "=\"\",\"\",(G" + rowNum + "/" + "F" + rowNum + ")),\"一\")");
			}
			cell.setCellStyle(styleMap.get("percentStyle"));

			// DN编号/修理单号
			cell = row.createCell(++colIndex);
			cell.setCellValue(entity.getWarehouse_no());
			cell.setCellStyle(styleMap.get("alignLeftStyle"));

			// 作业内容
			cell = row.createCell(++colIndex);
			cell.setCellValue(CodeListUtils.getValue("fact_production_type", productionType.toString()));
			cell.setCellStyle(styleMap.get("alignLeftStyle"));

			// 作业详细
			cell = row.createCell(++colIndex);
			if (productionType == 99) {// O：其它
				comment = partialWarehouseMapper.getComment(factPfKey);
				cell.setCellValue(comment);
			} else if (productionType == 50 || productionType == 51 || productionType == 52) {
			} else {
				List<PartialWarehouseEntity> list = new ArrayList<PartialWarehouseEntity>();
				if (productionType == 10 || productionType == 11) {
					list = partialWarehouseMapper.countReceptBox(key);
					unit = "箱";
				} else if (productionType == 20) {// B1：核对+上架
					list = partialWarehouseMapper.countCollectAndOnShelfQuantity(factPfKey);
					unit = "点";
				} else if(productionType == 21){//B2：核对
					list = partialWarehouseMapper.countCollectQuantity(factPfKey);
					unit = "包";
				} else if (productionType == 30) {// C：分装
					list = partialWarehouseMapper.countUnPackAndOnShelfQuantity(factPfKey);
					unit = "袋";
				} else if(productionType == 40){//D：上架
					list = partialWarehouseMapper.countUnPackAndOnShelfQuantity(factPfKey);
					unit = "包";
				}

				StringBuffer buffer = new StringBuffer();
				for (PartialWarehouseEntity temp : list) {
					kindName = CodeListUtils.getValue("partial_spec_kind", temp.getSpec_kind().toString());
					if (buffer.length() == 0) {
						buffer.append(kindName + ":" + temp.getQuantity() + unit);
					} else {
						buffer.append(",\r\n" + kindName + ":" + temp.getQuantity() + unit);
					}
				}
				cell.setCellValue(buffer.toString());
			}
			cell.setCellStyle(styleMap.get("alignLeftStyle"));

			// 当天负荷率
			cell = row.createCell(++colIndex);
			cell.setCellStyle(styleMap.get("percentStyle"));

			// 当天能率
			cell = row.createCell(++colIndex);
			cell.setCellStyle(styleMap.get("percentStyle"));

			if ("00056".equals(jobNo) || "30301".equals(jobNo)) {
				key = DateUtil.toString(finishTime, DateUtil.DATE_PATTERN) + "-" + jobNo;

				if (!loadRateMap.containsKey(key)) {
					Map<Integer, PartialWarehouseEntity> map = new LinkedHashMap<Integer, PartialWarehouseEntity>();
					map.put(rowIndex, entity);
					loadRateMap.put(key, map);
				} else {
					Map<Integer, PartialWarehouseEntity> map = loadRateMap.get(key);
					map.put(rowIndex, entity);
					loadRateMap.put(key, map);
				}

				if (!energyRateMap.containsKey(key)) {
					Map<Integer, PartialWarehouseEntity> map = new LinkedHashMap<Integer, PartialWarehouseEntity>();
					map.put(rowIndex,entity);
					energyRateMap.put(key, map);
				} else {
					Map<Integer, PartialWarehouseEntity> map = energyRateMap.get(key);
					map.put(rowIndex, entity);
					energyRateMap.put(key, map);
				}
			}

		}

		// 当天负荷率
		Map<String, LinkedHashMap<String, Double>> dailyLoadRateMap = this.setCurrentLoadRate(isMonthEnd,sheet, loadRateMap);
		// 当天能率
		Map<String, LinkedHashMap<String, Double>> dailyEnergyRateMap = this.setCurrentEnergyRate(isMonthEnd,sheet,energyRateMap);
		
		Map<String,Map<String, LinkedHashMap<String, Double>>> respMap = new HashMap<String,Map<String, LinkedHashMap<String, Double>>>();
		respMap.put("dailyLoadRate", dailyLoadRateMap);
		respMap.put("dailyEnergyRate", dailyEnergyRateMap);

		return respMap;
	}

	/**
	 * 当天能率
	 * @param isMonthEnd 
	 *
	 * @param sheet
	 * @param map
	 * @return 
	 */
	private Map<String, LinkedHashMap<String, Double>> setCurrentEnergyRate(boolean isMonthEnd, Sheet sheet,Map<String, Map<Integer, PartialWarehouseEntity>> map) {
		Row row = null;
		PartialWarehouseEntity entity  = null;
		Map<String, LinkedHashMap<String, Double>> respMap = new HashMap<String,LinkedHashMap<String, Double>>(16);
		for (String key : map.keySet()) {
			Map<Integer, PartialWarehouseEntity> rowMap = map.get(key);

			// 总标准时间（M）
			BigDecimal totalStandTime = new BigDecimal(0);

			// 总计毫秒数
			long longTime = 0;
			for (Integer rowIndex : rowMap.keySet()) {
				entity = rowMap.get(rowIndex);
				// 进行时间（毫秒）
				longTime += entity.getFinish_time().getTime() - entity.getAction_time().getTime();
			}
			//总进行时间（M）（总计毫秒数转换成分钟，不满足一分钟当作一分钟计算）
			BigDecimal totalSpendTime = (new BigDecimal(longTime)).divide(new BigDecimal(ONE_MINUTE_MILLISECOND), BigDecimal.ROUND_UP);
			
			longTime = 0;
			for (Integer rowIndex : rowMap.keySet()) {
				entity = rowMap.get(rowIndex);
				
				// 标准时间（M）
				String cellValue = entity.getStanardtime();
				if (!CommonStringUtil.isEmpty(cellValue) && !MIDDLE_LINE.equals(cellValue)) {
					totalStandTime = totalStandTime.add(new BigDecimal(cellValue));
				} else if (MIDDLE_LINE.equals(cellValue)) {//O：其它, E3：其他维修出库  标准时间==实际时间
					// 进行时间（毫秒）
					longTime += entity.getFinish_time().getTime() - entity.getAction_time().getTime();
				}
			}
			totalStandTime = totalStandTime.add((new BigDecimal(longTime)).divide(new BigDecimal(ONE_MINUTE_MILLISECOND), BigDecimal.ROUND_UP));
			
			double value = totalStandTime.divide(totalSpendTime,SCALE_FOUR, BigDecimal.ROUND_HALF_UP).doubleValue();

			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);
				// 当天能率
				row.getCell(12).setCellValue(value);
			}
			
			if(isMonthEnd){
				String arr[] = key.split("-");
				// 工作日期
				String day = arr[0];
				// 工号
				String jobNo = arr[1];
				
				//每天能率
				//例如：{30301={2019/04/01=0.9958, 2019/04/02=1.0063}}
				LinkedHashMap<String, Double> dailyRate = null;
				if(respMap.containsKey(jobNo)){
					dailyRate = respMap.get(jobNo);
					dailyRate.put(day, value);
				}else{
					dailyRate = new LinkedHashMap<String, Double>();
					dailyRate.put(day, value);
				}
				respMap.put(jobNo, dailyRate);
			}
		}
		
		return respMap;
	}

	/**
	 * 创建单元格样式
	 *
	 * @param work 工作簿
	 * @return
	 */
	private Map<String, CellStyle> createCellStyle(Workbook work) {
		Map<String, CellStyle> styleMap = new HashMap<String, CellStyle>();

		Font font = work.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("微软雅黑");

		// 数据格式化
		DataFormat format = work.createDataFormat();

		/* 设置单元格样式 */
		CellStyle baseStyle = work.createCellStyle();
		baseStyle.setBorderLeft(CellStyle.BORDER_THIN);
		baseStyle.setBorderTop(CellStyle.BORDER_THIN);
		baseStyle.setBorderRight(CellStyle.BORDER_THIN);
		baseStyle.setBorderBottom(CellStyle.BORDER_THIN);
		baseStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		baseStyle.setWrapText(true);
		baseStyle.setFont(font);

		/* 右对齐 */
		CellStyle alignRightStyle = work.createCellStyle();
		alignRightStyle.cloneStyleFrom(baseStyle);
		alignRightStyle.setAlignment(CellStyle.ALIGN_RIGHT);

		/* 左对齐 */
		CellStyle alignLeftStyle = work.createCellStyle();
		alignLeftStyle.cloneStyleFrom(baseStyle);
		alignLeftStyle.setAlignment(CellStyle.ALIGN_LEFT);
		alignLeftStyle.setDataFormat(format.getFormat("@"));

		/* 日期格式化 */
		CellStyle dateStyle = work.createCellStyle();
		dateStyle.cloneStyleFrom(baseStyle);
		dateStyle.setDataFormat(format.getFormat("yyyy/mm/dd"));

		CellStyle dayStyle = work.createCellStyle();
		dayStyle.cloneStyleFrom(baseStyle);
		dayStyle.setAlignment(CellStyle.ALIGN_CENTER);
		dayStyle.setDataFormat(format.getFormat("d日"));

		/* 时间格式化 */
		CellStyle timeStyle = work.createCellStyle();
		timeStyle.cloneStyleFrom(baseStyle);
		timeStyle.setDataFormat(format.getFormat("hh:mm"));

		/* 百分比格式化 */
		CellStyle percentStyle = work.createCellStyle();
		percentStyle.cloneStyleFrom(baseStyle);
		percentStyle.setDataFormat(format.getFormat("0.00%"));

		styleMap.put("alignRightStyle", alignRightStyle);
		styleMap.put("alignLeftStyle", alignLeftStyle);
		styleMap.put("dateStyle", dateStyle);
		styleMap.put("dayStyle", dayStyle);
		styleMap.put("timeStyle", timeStyle);
		styleMap.put("percentStyle", percentStyle);

		return styleMap;
	}

	/**
	 * 当天负荷率
	 * @param isMonthEnd 
	 * @param sheet
	 * @param map
	 */
	private Map<String, LinkedHashMap<String, Double>> setCurrentLoadRate(boolean isMonthEnd, Sheet sheet, Map<String, Map<Integer, PartialWarehouseEntity>> map) {
		Row row = null;
		Calendar halfPastFivePM = Calendar.getInstance();
		
		Map<String, LinkedHashMap<String, Double>> respMap = new HashMap<String,LinkedHashMap<String, Double>>(16);
		
		for (String key : map.keySet()) {
			Map<Integer, PartialWarehouseEntity> rowMap = map.get(key);
			
			BigDecimal workTime = new BigDecimal(WORK_TIME);
			
			/** 判断是否加班 **/ 
			for(Integer rowIndex : rowMap.keySet()){
				PartialWarehouseEntity entity = rowMap.get(rowIndex);
				// 结束时间
				Date finishTime = entity.getFinish_time();
				
				// 当日下午5:30
				halfPastFivePM.setTime(finishTime);
				halfPastFivePM.set(Calendar.HOUR_OF_DAY, 17);
				halfPastFivePM.set(Calendar.MINUTE, 30);
				halfPastFivePM.set(Calendar.SECOND, 0);
				halfPastFivePM.set(Calendar.MILLISECOND, 0);

				// 结束时间与下午5:30比较
				long compare = finishTime.getTime() - halfPastFivePM.getTimeInMillis();
				if (compare > 0) {
					// 超过5:30，加上90分钟
					workTime = new BigDecimal(WORK_OVER_TIME);
					break;
				}
			}

			// 总计毫秒数
			long longTime = 0;
			for (Integer rowIndex : rowMap.keySet()) {
				PartialWarehouseEntity entity = rowMap.get(rowIndex);
				
				// 开始时间
				Date actionTime = entity.getAction_time();
				// 结束时间
				Date finishTime = entity.getFinish_time();
				
				// 进行时间（毫秒）
				longTime += finishTime.getTime() - actionTime.getTime();
				
				longTime -= subtractRestTime(actionTime, finishTime);
			}
			
			//总进行时间（M）（总计毫秒数转换成分钟，不满足一分钟当作一分钟计算）
			BigDecimal totalSpendTime = (new BigDecimal(longTime)).divide(new BigDecimal(ONE_MINUTE_MILLISECOND), BigDecimal.ROUND_UP);

			double value = totalSpendTime.divide(workTime,SCALE_FOUR,BigDecimal.ROUND_HALF_UP).doubleValue();
			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);
				// 当天负荷率
				row.getCell(11).setCellValue(value);
			}
			
			if(isMonthEnd){
				String arr[] = key.split("-");
				// 工作日期
				String day = arr[0];
				// 工号
				String jobNo = arr[1];
				
				//每天负荷率
				//例如：{30301={2019/04/01=0.9958, 2019/04/02=1.0063}}
				LinkedHashMap<String, Double> dailyRate = null;
				if(respMap.containsKey(jobNo)){
					dailyRate = respMap.get(jobNo);
					dailyRate.put(day, value);
				}else{
					dailyRate = new LinkedHashMap<String, Double>();
					dailyRate.put(day, value);
				}
				respMap.put(jobNo, dailyRate);
			}
		}
		return respMap;
	}

	/**
	 * 移除休息时间
	 * @param restPointMap
	 * @param actionTime
	 * @param finishTime
	 * @return
	 */
	private Long subtractRestTime(Date actionTime,Date finishTime){
		long restTime = 0;
		
		// 8:00:00:000
		Calendar pointOneCal = Calendar.getInstance();
		pointOneCal.setTime(actionTime);
		pointOneCal.set(Calendar.HOUR_OF_DAY, 8);
		pointOneCal.set(Calendar.MINUTE, 0);
		pointOneCal.set(Calendar.SECOND, 0);
		pointOneCal.set(Calendar.MILLISECOND, 0);
		
		// 10:00:00:000
		Calendar pointTwoCal = Calendar.getInstance();
		pointTwoCal.setTime(actionTime);
		pointTwoCal.set(Calendar.HOUR_OF_DAY, 10);
		pointTwoCal.set(Calendar.MINUTE, 0);
		pointTwoCal.set(Calendar.SECOND, 0);
		pointTwoCal.set(Calendar.MILLISECOND, 0);
		
		// 10:10:00:000
		Calendar pointThreeCal = Calendar.getInstance();
		pointThreeCal.setTime(actionTime);
		pointThreeCal.set(Calendar.HOUR_OF_DAY, 10);
		pointThreeCal.set(Calendar.MINUTE, 10);
		pointThreeCal.set(Calendar.SECOND, 0);
		pointThreeCal.set(Calendar.MILLISECOND, 0);
		
		// 12:00:00:000
		Calendar pointFourCal = Calendar.getInstance();
		pointFourCal.setTime(actionTime);
		pointFourCal.set(Calendar.HOUR_OF_DAY, 12);
		pointFourCal.set(Calendar.MINUTE, 0);
		pointFourCal.set(Calendar.SECOND, 0);
		pointFourCal.set(Calendar.MILLISECOND, 0);
		
		// 13:00:00:000
		Calendar pointFiveCal = Calendar.getInstance();
		pointFiveCal.setTime(actionTime);
		pointFiveCal.set(Calendar.HOUR_OF_DAY, 13);
		pointFiveCal.set(Calendar.MINUTE, 0);
		pointFiveCal.set(Calendar.SECOND, 0);
		pointFiveCal.set(Calendar.MILLISECOND, 0);
		
		// 15:00:00:000
		Calendar pointSixCal = Calendar.getInstance();
		pointSixCal.setTime(actionTime);
		pointSixCal.set(Calendar.HOUR_OF_DAY, 15);
		pointSixCal.set(Calendar.MINUTE, 0);
		pointSixCal.set(Calendar.SECOND, 0);
		pointSixCal.set(Calendar.MILLISECOND, 0);

		// 15:10:00:000
		Calendar pointSevenCal = Calendar.getInstance();
		pointSevenCal.setTime(actionTime);
		pointSevenCal.set(Calendar.HOUR_OF_DAY, 15);
		pointSevenCal.set(Calendar.MINUTE, 10);
		pointSevenCal.set(Calendar.SECOND, 0);
		pointSevenCal.set(Calendar.MILLISECOND, 0);
		
		// 17:15:00:000
		Calendar poinNineCal = Calendar.getInstance();
		poinNineCal.setTime(actionTime);
		poinNineCal.set(Calendar.HOUR_OF_DAY, 17);
		poinNineCal.set(Calendar.MINUTE, 15);
		poinNineCal.set(Calendar.SECOND, 0);
		poinNineCal.set(Calendar.MILLISECOND, 0);
		
		// 18:00:00:000
		Calendar poinTenCal = Calendar.getInstance();
		poinTenCal.setTime(actionTime);
		poinTenCal.set(Calendar.HOUR_OF_DAY, 18);
		poinTenCal.set(Calendar.MINUTE, 00);
		poinTenCal.set(Calendar.SECOND, 0);
		poinTenCal.set(Calendar.MILLISECOND, 0);
		
		long eightOclock = pointOneCal.getTimeInMillis();
		long tenOclock = pointTwoCal.getTimeInMillis();
		long tenTenOclock = pointThreeCal.getTimeInMillis();
		long twelveOclock = pointFourCal.getTimeInMillis();
		long thirteenOclock = pointFiveCal.getTimeInMillis();
		long fifteenOclock = pointSixCal.getTimeInMillis();
		long fifteenTenOclock = pointSevenCal.getTimeInMillis();
		long seventeenFifteenOclock = poinNineCal.getTimeInMillis();
		long eighteenOclock = poinTenCal.getTimeInMillis();
		
		long start = actionTime.getTime();
		long end = finishTime.getTime();
		
		/** 1、完成时间在00:00 - 8:00之间（包含8:00） **/
		if (end <= eightOclock){
			restTime = end - start;
		} else if (end <= tenOclock){
			/** 2、完成时间在8:00 - 10:00之间（包含10:00） **/
			if(start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
			}
		} else if (end <= tenTenOclock){
			/** 3、完成时间在10:00 - 10:10之间（包含10:10） **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				
				// 结束点到10:00的时间
				restTime += end - tenOclock;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 结束点到10:00的时间
				restTime = end - tenOclock;
			} else if (start >= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				restTime = end - start;
			}
		} else if (end <= twelveOclock){
			/** 4、完成时间在10:10 - 12:00之间（包含12:00） **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10分的时间
				restTime = TEN_MINUTE_MILLISECOND;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				restTime = tenTenOclock - start;
			}
		} else if (end <= thirteenOclock){
			/** 5、完成时间在12:00 - 13:00之间（包含13:00） **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到12:00的时间
				restTime += end - twelveOclock;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 结束点到12:00的时间
				restTime += end - twelveOclock;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 结束点到12:00的时间
				restTime += end - twelveOclock;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 结束点到12:00的时间
				restTime = end - twelveOclock;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				restTime = end - start;
			}
		} else if (end <= fifteenOclock){
			/** 6、完成时间在13:00 - 15:00之间（包含15:00） **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 12:00到13:00的时间
				restTime = ONE_HOUR_MILLISECOND;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				// 开始点到13:00的时间
				restTime = thirteenOclock - start;
			}
		} else if(end <= fifteenTenOclock){
			/** 7、完成时间在15:00 - 15:10之间（包含15:10） **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 结束点到15:00时间
				restTime += end - fifteenOclock;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 结束点到15:00时间
				restTime += end - fifteenOclock;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 结束点到15:00时间
				restTime += end - fifteenOclock;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 12:00到13:00的时间
				restTime = ONE_HOUR_MILLISECOND;
				// 结束点到15:00时间
				restTime += end - fifteenOclock;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				// 开始点到13:00的时间
				restTime = thirteenOclock - start;
				// 结束点到15:00时间
				restTime += end - fifteenOclock;
			} else if (start <= fifteenOclock){// ⑥开始时间在13:00 - 15:00之间（包含15:00）
				// 结束点到15:00时间
				restTime = end - fifteenOclock;
			} else if (start <= fifteenTenOclock){// ⑥开始时间在15:00 - 15:10之间（包含15:10）
				restTime = end - start;
			}
		} else if (end <= seventeenFifteenOclock){
			/** 8、完成时间在15:10-17:15之间 **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 12:00到13:00的时间
				restTime = ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				// 开始点到13:00的时间
				restTime = thirteenOclock - start;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
			} else if (start <= fifteenOclock){// ⑥开始时间在13:00 - 15:00之间（包含15:00）
				// 15:00到15:10时间
				restTime = TEN_MINUTE_MILLISECOND;
			} else if (start <= fifteenTenOclock){// ⑦开始时间在15:00 - 15:10之间（包含15:10）
				// 开始点到15:10的时间
				restTime = fifteenTenOclock - start;
			} 
		} else if (end <= eighteenOclock){
			/** 8、完成时间在17:15-18:00之间 **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 12:00到13:00的时间
				restTime = ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间4
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				// 开始点到13:00的时间
				restTime = thirteenOclock - start;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= fifteenOclock){// ⑥开始时间在13:00 - 15:00之间（包含15:00）
				// 15:00到15:10时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= fifteenTenOclock){// ⑦开始时间在15:00 - 15:10之间（包含15:10）
				// 开始点到15:10的时间
				restTime = fifteenTenOclock - start;
				// 结束点到17:15的时间
				restTime += end - seventeenFifteenOclock;
			} else if (start <= seventeenFifteenOclock){// ⑧开始时间在15:10 - 17:15之间（包含17:15）
				// 结束点到17:15的时间
				restTime = end - seventeenFifteenOclock;
			} else if (start <= eighteenOclock){// ⑨开始时间在17:15 - 18:00之间（包含18:00）
				restTime = end - start;
			} 
		} else {
			/** 8、完成时间在18:00之后 **/
			if (start <= eightOclock){// ①开始时间在00:00 - 8:00之间（包含8:00）
				// 8:00之前的时间
				restTime = eightOclock - start;
				// 10:00到10:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= tenOclock){// ②开始时间在8:00 - 10:00之间（包含10:00）
				// 10:00到10:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= tenTenOclock){// ③开始时间在10:00 - 10:10之间（包含10:10）
				// 开始点到10:10的时间
				restTime = tenTenOclock - start;
				// 12:00到13:00的时间
				restTime += ONE_HOUR_MILLISECOND;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= twelveOclock){// ④开始时间在10:10 - 12:00之间（包含12:00）
				// 12:00到13:00的时间
				restTime = ONE_HOUR_MILLISECOND;
				// 15:00到15:10的时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= thirteenOclock){// ⑤开始时间在12:00 - 13:00之间（包含13:00）
				// 开始点到13:00的时间
				restTime = thirteenOclock - start;
				// 15:00到15:10时间
				restTime += TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= fifteenOclock){// ⑥开始时间在13:00 - 15:00之间（包含15:00）
				// 15:00到15:10的时间
				restTime = TEN_MINUTE_MILLISECOND;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= fifteenTenOclock){// ⑦开始时间在15:00 - 15:10之间（包含15:10）
				// 开始点到15:10的时间
				restTime = fifteenTenOclock - start;
				// 17:15到18:00的时间
				restTime += FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= seventeenFifteenOclock){// ⑧开始时间在15:10 - 17:15之间（包含17:15）
				// 结束点到17:15的时间
				restTime = FORTY_FIVE_MINUTE_MILLISECOND;
			} else if (start <= eighteenOclock){// ⑨开始时间在17:15 - 18:00之间（包含18:00）
				restTime = eighteenOclock - start;
			} 
		}

		return restTime;
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
		// today.set(Calendar.YEAR, 2018);
		today.set(Calendar.MONTH, 3);
		today.set(Calendar.DATE, 30);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		PathConsts.BASE_PATH = "D:\\rvsG";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
		PathConsts.PCS_TEMPLATE = "\\PcsTemplates";
		PathConsts.PROPERTIES = "\\PROPERTIES";
		;
		PathConsts.REPORT = "\\Reports";
		PathConsts.IMAGES = "\\images";

		PathConsts.load();

		PartialWarehouseJob job = new PartialWarehouseJob();
		job.partialWarehouseReport(today, conn);
	}

	private static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}

}
