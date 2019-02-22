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

	private final Integer NS_STANDARD_TIME = 6;
	private final Integer DEC_STANDARD_TIME = 9;

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
		PartialWarehouseMapper partialWarehouseMapper = conn.getMapper(PartialWarehouseMapper.class);
		UserDefineCodesMapper userDefineCodesMapper = conn.getMapper(UserDefineCodesMapper.class);

		List<PartialWarehouseEntity> listBeans = partialWarehouseMapper.searchMonthWorkRecord(DateUtil.toString(RvsUtils.getMonthStartDate(monthStart), DateUtil.DATE_PATTERN));
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

				// 出入库明细
				setDetailList(listBeans, userDefineMap.get("bdPartialReceptMoveCost"), detailSheet, styleMap, formulaEvaluator, partialWarehouseMapper);

				// 当月最后一天
				int lastDay = monthStart.getActualMaximum(Calendar.DATE);

				// 当天
				int curDay = monthStart.get(Calendar.DATE);

				// 月底
				if(curDay == lastDay){
					// 高雁梅每日汇总
					setDailyCollect(monthStart, "197",styleMap, userDefineMap, dailyCollectSheet, partialWarehouseMapper);

					// 叶昭杏每日汇总
					setDailyCollect(monthStart, "198",styleMap, userDefineMap, dailyCollectSheet2, partialWarehouseMapper);

					work.setForceFormulaRecalculation(true);
				}else{
					// 删除Sheet
					work.removeSheetAt(work.getSheetIndex(dailyCollectSheet));
					work.removeSheetAt(work.getSheetIndex(dailyCollectSheet2));
				}

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
	private void setDailyCollect(Calendar monthStart,String operatorId, Map<String, CellStyle> styleMap, Map<String, BigDecimal> userDefineMap, Sheet sheet, PartialWarehouseMapper dao) {
		Row row = null;
		Row row2 = null;
		Cell cell = null;
		PartialWarehouseEntity partialWarehouseEntity = null;
		List<PartialWarehouseEntity> dailyWorkRecordList = null;

		int colIndex = 2;

		// 每天每种作业类型用时
		List<PartialWarehouseEntity> list = dao.countDailySpendTime(operatorId,DateUtil.toString(RvsUtils.getMonthStartDate(monthStart), DateUtil.DATE_PATTERN));

		Map<Date, List<PartialWarehouseEntity>> map = new LinkedHashMap<Date, List<PartialWarehouseEntity>>();

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

			// 创建第二行到第十八行单元格
			for (int rowIndex = 1; rowIndex <= 17; rowIndex++) {
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

							// 收货标准工时
							BigDecimal collectCase = dao.countCollectCaseStandardTime(key);
							standardTime = standardTime.add(collectCase);
						}
					}

					break;
				case 20://B1：核对+上架
					row = sheet.getRow(3);
					row2 = sheet.getRow(4);

					for (PartialWarehouseEntity entity : dailyWorkRecordList) {
						BigDecimal collationStandardTime = dao.countCollationStandardTime(entity.getFact_pf_key());
						standardTime = standardTime.add(collationStandardTime);
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
				case 99://O：其它
					row = sheet.getRow(15);
					standardTime = new BigDecimal(spendTime);//标准时间和实际时间一样
					break;
				default:
					break;
				}

				// 用时
				row.getCell(colIndex).setCellValue(spendTime);

				// 能率
				if (productionType != 99) {
					double percent = standardTime.divide(new BigDecimal(spendTime), 4, RoundingMode.HALF_UP).doubleValue();
					row2.getCell(colIndex).setCellValue(percent);
				}

				totalSpendTime = totalSpendTime.add(new BigDecimal(spendTime));
				totalStandardTime = totalStandardTime.add(standardTime);
			}

			// 一天工作时间(分钟)
			BigDecimal workTime = null;

			double percent = 0;

			if (flg) {// 加班
				workTime = new BigDecimal(565);
			} else {
				workTime = new BigDecimal(475);
			}

			monthWorkTime = monthWorkTime.add(workTime);

			// 合计负荷率
			percent = totalSpendTime.divide(workTime, 4, RoundingMode.HALF_UP).doubleValue();
			sheet.getRow(16).getCell(colIndex).setCellValue(percent);

			// 合计能率
			percent = totalStandardTime.divide(totalSpendTime, 4, RoundingMode.HALF_UP).doubleValue();
			sheet.getRow(17).getCell(colIndex).setCellValue(percent);

			//负荷率警报标志下线
			row = sheet.getRow(18);
			if(row == null) row = sheet.createRow(18);

			cell = row.createCell(colIndex);
			cell.setCellValue(userDefineMap.get("strLowLever").divide(new BigDecimal(100)).doubleValue());
			cell.setCellStyle(styleMap.get("percentStyle"));

			//能率警报标志下线
			row = sheet.getRow(19);
			if(row == null) row = sheet.createRow(19);

			cell = row.createCell(colIndex);
			cell.setCellValue(userDefineMap.get("efLowLever").divide(new BigDecimal(100)).doubleValue());
			cell.setCellStyle(styleMap.get("percentStyle"));


			row = sheet.getRow(20);
			if(row == null) row = sheet.createRow(20);
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

		row = sheet.getRow(22);
		if(row == null) row = sheet.createRow(22);
		cell = row.createCell(10);
		//总计工作时间
		cell.setCellValue(monthWorkTime.doubleValue());

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
	 * @param listBeans 明细数据
	 * @param moveCost 搬箱移动时间
	 * @param sheet
	 * @param styleMap 样式
	 * @param formulaEvaluator 公式计算
	 * @param partialWarehouseMapper
	 */
	private void setDetailList(List<PartialWarehouseEntity> listBeans, BigDecimal moveCost, Sheet sheet, Map<String, CellStyle> styleMap, FormulaEvaluator formulaEvaluator,
			PartialWarehouseMapper partialWarehouseMapper) {

		Row row = null;
		Cell cell = null;

		BigDecimal standardTime = null;

		Calendar cal = null;

		// 单位
		String unit = "";

		// 其他备注
		String comment = "";

		// 规格种别名称
		String kindName = "";

		// 当天负荷率
		Map<String, Map<Integer, Integer>> loadRateMap = new LinkedHashMap<String, Map<Integer, Integer>>();

		// 当天能率
		Map<String, Map<Integer, Double>> energyRateMap = new LinkedHashMap<String, Map<Integer, Double>>();

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
			if (productionType == 10 || productionType == 11) {// A：收货
				// 标准时间
				standardTime = partialWarehouseMapper.countReceptStandardTime(key);
				// 拆盒
				BigDecimal collectCase = partialWarehouseMapper.countCollectCaseStandardTime(key);

				standardTime = standardTime.add(collectCase);
				standardTime = standardTime.add(moveCost);

				cell.setCellValue(standardTime.doubleValue());
			} else if (productionType == 20) {// B1：核对+上架
				standardTime = partialWarehouseMapper.countCollationAndOnShelfStandardTime(factPfKey);
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
			}
			cell.setCellStyle(styleMap.get("alignRightStyle"));

			// 能率
			cell = row.createCell(++colIndex);
			if (productionType == 99) {// O：其它
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
			} else if (productionType == 50 || productionType == 51) {
			} else {
				List<PartialWarehouseEntity> list = new ArrayList<PartialWarehouseEntity>();
				if (productionType == 10 || productionType == 11) {
					list = partialWarehouseMapper.countReceptBox(key);
					unit = "箱";
				} else if (productionType == 20 || productionType == 21) {// B1：核对+上架、B2：核对
					list = partialWarehouseMapper.countCollectQuantity(factPfKey);
					unit = "点";
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

				// 当日下午5:30
				cal = Calendar.getInstance();
				cal.setTime(finishTime);
				cal.set(Calendar.HOUR_OF_DAY, 17);
				cal.set(Calendar.MINUTE, 30);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);

				long compare = finishTime.getTime() - cal.getTimeInMillis();
				int time = 475;
				if (compare > 0) {// 超过5:30，加上90分钟
					time += 90;
				}

				if (!loadRateMap.containsKey(key)) {
					Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
					map.put(rowIndex, time);
					loadRateMap.put(key, map);
				} else {
					Map<Integer, Integer> map = loadRateMap.get(key);
					map.put(rowIndex, time);
					loadRateMap.put(key, map);
				}

				if (!energyRateMap.containsKey(key)) {
					Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
					map.put(rowIndex, formulaEvaluator.evaluate(row.getCell(5)).getNumberValue());
					energyRateMap.put(key, map);
				} else {
					Map<Integer, Double> map = energyRateMap.get(key);
					map.put(rowIndex, formulaEvaluator.evaluate(row.getCell(5)).getNumberValue());
					energyRateMap.put(key, map);
				}
			}

		}

		// 当天负荷率
		this.setCurrentLoadRate(sheet, formulaEvaluator, loadRateMap);

		// 当天能率
		this.setCurrentEnergyRate(sheet, energyRateMap);

	}

	/**
	 * 当天能率
	 *
	 * @param sheet
	 * @param formulaEvaluator
	 * @param map
	 */
	private void setCurrentEnergyRate(Sheet sheet, Map<String, Map<Integer, Double>> map) {
		Row row = null;
		for (String key : map.keySet()) {
			Map<Integer, Double> rowMap = map.get(key);

			// 总进行时间（M）
			BigDecimal totalSpendTime = new BigDecimal(0);

			// 总标准时间（M）
			BigDecimal totalStandTime = new BigDecimal(0);

			for (Integer rowIndex : rowMap.keySet()) {
				totalSpendTime = totalSpendTime.add(new BigDecimal(rowMap.get(rowIndex)));
			}

			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);

				// 标准时间（M）
				String cellValue = CopyByPoi.getStringCellValue((XSSFCell) row.getCell(6));

				if (!CommonStringUtil.isEmpty(cellValue) && !MIDDLE_LINE.equals(cellValue)) {
					totalStandTime = totalStandTime.add(new BigDecimal(cellValue));
				}
			}

			double value = totalStandTime.divide(totalSpendTime, 4, BigDecimal.ROUND_HALF_UP).doubleValue();

			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);

				// 当天能率
				row.getCell(12).setCellValue(value);
			}
		}
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
	 *
	 * @param sheet
	 * @param formulaEvaluator
	 * @param map
	 */
	private void setCurrentLoadRate(Sheet sheet, FormulaEvaluator formulaEvaluator, Map<String, Map<Integer, Integer>> map) {
		Row row = null;

		for (String key : map.keySet()) {
			Map<Integer, Integer> rowMap = map.get(key);

			BigDecimal workTime = new BigDecimal(475);

			// 判断是否加班
			if (rowMap.containsValue(565))
				workTime = new BigDecimal(565);

			//总进行时间（M）
			BigDecimal totalSpendTime = new BigDecimal(0);

			double value = 0;

			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);

				// 进行时间（M）
				value = formulaEvaluator.evaluate(row.getCell(5)).getNumberValue();
				totalSpendTime = totalSpendTime.add(new BigDecimal(value));
			}

			value = totalSpendTime.divide(workTime, BigDecimal.ROUND_HALF_UP).doubleValue();
			for (Integer rowIndex : rowMap.keySet()) {
				row = sheet.getRow(rowIndex);
				// 当天负荷率
				row.getCell(11).setCellValue(value);
			}
		}
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
		today.set(Calendar.MONTH, 0);

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
