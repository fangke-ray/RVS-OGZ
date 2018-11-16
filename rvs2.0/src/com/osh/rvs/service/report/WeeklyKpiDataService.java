package com.osh.rvs.service.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.report.WeeklyKpiDataEntity;
import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReportUtils;
import com.osh.rvs.form.report.WeeklyKpiDataForm;
import com.osh.rvs.mapper.report.WeeklyKpiDataMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class WeeklyKpiDataService {
	
	private static final double L_INLINE_COMPLETE_RATE = 95; // TODO getbY period 大修纳期6天
	private static final double L_PART_ARRIVE_COMPLETE_RATE = 98; // 零件到达4天出货
	private static final double U_WORK_LEAD_TIME = 3.5; // 平均工作周期
	private static final double U_ONE_DAY_BO_RATE = 6; // 当天零件BO率
	private static final double U_THREE_DAYS_BO_RATE = 4; // 三天零件BO率
	private static final double L_INLINE_PASSTHROUGH_RATE = 96; // 直行率
	private static final double L_FINAL_INSPECT_PASS_RATE = 99.8; // 最终检查合格率
	private Logger _log = Logger.getLogger(getClass());

	public List<WeeklyKpiDataForm> searchAll(SqlSession conn) {
		WeeklyKpiDataMapper dao = conn.getMapper(WeeklyKpiDataMapper.class);

		List<WeeklyKpiDataEntity> list = dao.searchAll();

		List<WeeklyKpiDataForm> returnFormList = new ArrayList<WeeklyKpiDataForm>();
		
		List<Map<String, String>> fileList = searchFileName(PathConsts.BASE_PATH + PathConsts.REPORT +"\\weeks");
		
		if (list != null && list.size() > 0) {
			
			BeanUtil.copyToFormList(list, returnFormList,null, WeeklyKpiDataForm.class);
			
			WeeklyKpiDataForm weeklyKpiDataForm = null;
			for(int i = 0;i < returnFormList.size();i++){
				weeklyKpiDataForm = returnFormList.get(i);
				
				Date count_date_start = DateUtil.toDate(weeklyKpiDataForm.getCount_date_start(), DateUtil.DATE_PATTERN);
				Date count_date_end = DateUtil.toDate(weeklyKpiDataForm.getCount_date_end(), DateUtil.DATE_PATTERN);
				
				String key = "（" + DateUtil.toString(count_date_start, "MM月dd日") + "~" +DateUtil.toString(count_date_end, "MM月dd日") +"） "+ weeklyKpiDataForm.getWeekly_of_year() +"W";
				
				for(int j = 0;j< fileList.size();j++){
					Map<String, String> map = fileList.get(j);
					if(map.get("fileDayTime").equals(key)){//存在周报
						returnFormList.get(i).setFileName(map.get("fileName"));//系统生成
						returnFormList.get(i).setConfirmfilename(map.get("confirmfilename"));//确认完成
					}
				}
			}
			
		}

		return returnFormList;
	}
	
	//查询所有周报
	private List<Map<String, String>> searchFileName(String filepath) {
		List<Map<String, String>> fileList = new ArrayList<Map<String, String>>();
		File file = new File(filepath);
		if (file.exists()) {
			File[] fs = file.listFiles();
			// 遍历文件
			for (int i = 0; i < fs.length; i++) {
				Map<String, String> fileMap = new HashMap<String, String>();
				if (!fs[i].isDirectory()) {
					String filename = fs[i].getName();
					// 文件名字
					fileMap.put("fileName", filename);
					// 周报时间
					fileMap.put("fileDayTime",filename.replaceAll(".*(（\\d{2}月\\d{2}日~\\d{2}月\\d{2}日） \\d{2}W).*", "$1"));
					File readfile = new File(filepath + "\\confirm\\" + filename);
					if (readfile.exists()) {
						fileMap.put("confirmfilename", filename);
					} else {
						fileMap.put("confirmfilename", "");
					}
					fileList.add(fileMap);
				}
			}
			
		}

		return fileList;
	}

	/**
	 * 查询周报KPI数据
	 * @param form
	 * @param listResponse
	 * @param conn
	 */
	public void getDetailsForPage(ActionForm form,
			Map<String, Object> listResponse, SqlSession conn) {
		WeeklyKpiDataMapper mapper = conn.getMapper(WeeklyKpiDataMapper.class);
		WeeklyKpiDataEntity condition = new WeeklyKpiDataEntity();

		BeanUtil.copyToBean(form, condition, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 画面取得周数
		condition.setWeekly_of_year(8);

		List<WeeklyKpiDataForm> formList = new ArrayList<WeeklyKpiDataForm>();
		List<WeeklyKpiDataEntity> dList = mapper.searchDetails(condition);

		// 选择周
		Date choosedCountDateEnd = null;

		for(WeeklyKpiDataEntity detail : dList) {
			WeeklyKpiDataForm retForm = new WeeklyKpiDataForm();
			BeanUtil.copyToForm(detail, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);

			// 归并月
			retForm.setCount_date_end(DateUtil.toString(detail.getCount_date_end(), "yyyy年M月"));

			// 目标
			retForm.setTarget(
				PathConsts.SCHEDULE_SETTINGS.getProperty("target." + DateUtil.toString(detail.getCount_date_end(), "yyyy")
					+ "." + detail.getWeekly_of_year() + "W"));

			formList.add(retForm);

			if (choosedCountDateEnd == null) choosedCountDateEnd = detail.getCount_date_end();
		}

		// 取得下一周的目标
		if (choosedCountDateEnd != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(choosedCountDateEnd);
			cal.add(Calendar.DATE, 7);

			WeeklyKpiDataForm retForm = new WeeklyKpiDataForm();

			// 归并月
			retForm.setCount_date_end(DateUtil.toString(cal.getTime(), "yyyy年M月"));
			retForm.setWeekly_of_year("" + cal.get(Calendar.WEEK_OF_YEAR)); // TODO OGZ规则

			// 目标
			retForm.setTarget(
				PathConsts.SCHEDULE_SETTINGS.getProperty("target." + DateUtil.toString(cal.getTime(), "yyyy")
					+ "." + retForm.getWeekly_of_year() + "W"));

			formList.add(0, retForm);
		}

		listResponse.put("details", formList);
	}
	
	public void update(ActionForm form,SqlSessionManager conn){
		WeeklyKpiDataMapper mapper = conn.getMapper(WeeklyKpiDataMapper.class);
		WeeklyKpiDataEntity condition = new WeeklyKpiDataEntity();

		BeanUtil.copyToBean(form, condition, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		mapper.update(condition);
	}
	
	public void createWeekReport(ActionForm form, SqlSession conn) {
		WeeklyKpiDataMapper mapper = conn.getMapper(WeeklyKpiDataMapper.class);
		WeeklyKpiDataEntity condition = new WeeklyKpiDataEntity();

		BeanUtil.copyToBean(form, condition, CopyOptions.COPYOPTIONS_NOEMPTY);
		int weekly_of_year = condition.getWeekly_of_year();
		// 选择周
		Date choosedCountDateEnd = condition.getCount_date_end();
		
		condition.setWeekly_of_year(20000);
		
		//模板文件
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "OGZ内视镜修理周报模板.xls";
		//生成文件的文件名称
		String cacheFilename = "OGZ内视镜修理周报（" + DateUtil.toString(condition.getCount_date_start(), "MM月dd日") + "~" + 
													   DateUtil.toString(condition.getCount_date_end(), "MM月dd日") +"） " + 
													   weekly_of_year + "W.xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\weeks\\" + cacheFilename;
	
		//查询
		List<WeeklyKpiDataEntity> listBeans = mapper.searchDetails(condition);
		
		// 取得下一周的目标
		if (choosedCountDateEnd != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(choosedCountDateEnd);
			cal.add(Calendar.DATE, 7);
			
			WeeklyKpiDataEntity nextWeek = new WeeklyKpiDataEntity();
	
			// 归并月
			nextWeek.setCount_date_end(cal.getTime());
			nextWeek.setWeekly_of_year(cal.get(Calendar.WEEK_OF_YEAR)); // TODO OGZ规则
	
			listBeans.add(0, nextWeek);
		}
		
		int length = listBeans.size();
		InputStream in = null;
		OutputStream out = null;
		
		try{
			if (length > 0) {
				try {
					FileUtils.copyFile(new File(path), new File(cachePath));
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
				
				in = new FileInputStream(cachePath);//读取文件 
				HSSFWorkbook work = new HSSFWorkbook(in);//创建xls文件
				HSSFSheet sheet = work.getSheetAt(0);//取得第一个Sheet
				
				//基本字体
				HSSFFont basefont=work.createFont();
				basefont.setFontHeightInPoints((short)9);
				basefont.setFontName("MS PMincho");
				
				//加粗字体
				HSSFFont boldfont=work.createFont();
				boldfont.setFontHeightInPoints((short)10);
				boldfont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				boldfont.setFontName("MS PMincho");
				
				//灰色字体
				HSSFFont greyfont=work.createFont();
				greyfont.setFontHeightInPoints((short)9);
				greyfont.setFontName("MS PMincho");
				greyfont.setColor(HSSFColor.GREY_50_PERCENT.index);
				
				//红色字体
				HSSFFont redfont=work.createFont();
				redfont.setFontHeightInPoints((short)9);
				redfont.setFontName("MS PMincho");
				redfont.setColor(HSSFColor.RED.index);
				
				//黑色字体
				HSSFFont blackfont=work.createFont();
				blackfont.setFontHeightInPoints((short)9);
				blackfont.setFontName("MS PMincho");
				blackfont.setColor(HSSFColor.BLACK.index);
				
				//基本样式
				HSSFCellStyle baseStyle = work.createCellStyle();
				baseStyle.setBorderLeft(HSSFCellStyle.BORDER_DASHED);
				baseStyle.setBorderTop(HSSFCellStyle.BORDER_DASHED); 
				baseStyle.setBorderRight(HSSFCellStyle.BORDER_DASHED);
				baseStyle.setBorderBottom(HSSFCellStyle.BORDER_DASHED);
				baseStyle.setLeftBorderColor(HSSFColor.GREY_50_PERCENT.index);
				baseStyle.setTopBorderColor(HSSFColor.GREY_50_PERCENT.index);
				baseStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
				baseStyle.setBottomBorderColor(HSSFColor.GREY_50_PERCENT.index);
				baseStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				baseStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				baseStyle.setFont(basefont);
				
				//月份样式
				HSSFCellStyle yearMonthStyle = work.createCellStyle();
				yearMonthStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
				yearMonthStyle.setBorderTop(HSSFCellStyle.BORDER_THIN); 
				yearMonthStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
				yearMonthStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				yearMonthStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				yearMonthStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				yearMonthStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				yearMonthStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
				yearMonthStyle.setFont(boldfont);
				
				//周数样式
				HSSFCellStyle weeklyOfYearStyle = work.createCellStyle();
				weeklyOfYearStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
				weeklyOfYearStyle.setBorderTop(HSSFCellStyle.BORDER_THIN); 
				weeklyOfYearStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
				weeklyOfYearStyle.setBorderBottom(HSSFCellStyle.BORDER_DASHED);
				weeklyOfYearStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				weeklyOfYearStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				weeklyOfYearStyle.setFont(greyfont);
				weeklyOfYearStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				weeklyOfYearStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
				
				//灰色背景
				HSSFCellStyle greyBackGroundStyle = work.createCellStyle();
				greyBackGroundStyle.cloneStyleFrom(baseStyle);
				greyBackGroundStyle.setFont(greyfont);
				greyBackGroundStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				greyBackGroundStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
				
				//灰色背景红色字体
				HSSFCellStyle greyBackGroundRedFontStyle = work.createCellStyle();
				greyBackGroundRedFontStyle.cloneStyleFrom(baseStyle);
				greyBackGroundRedFontStyle.setFont(redfont);
				greyBackGroundRedFontStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				greyBackGroundRedFontStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
				
				//白色背景
				HSSFCellStyle whiteBackGroundStyle = work.createCellStyle();
				whiteBackGroundStyle.cloneStyleFrom(baseStyle);
				whiteBackGroundStyle.setFont(greyfont);
				
				//绿色背景
				HSSFCellStyle greenBackGroundStyle = work.createCellStyle();
				greenBackGroundStyle.cloneStyleFrom(baseStyle);
				greenBackGroundStyle.setFont(greyfont);
				greenBackGroundStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				greenBackGroundStyle.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
				
				//黄色背景
				HSSFCellStyle yelloBackGroundStyle = work.createCellStyle();
				yelloBackGroundStyle.cloneStyleFrom(baseStyle);
				yelloBackGroundStyle.setFont(greyfont);
				yelloBackGroundStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				yelloBackGroundStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
				
				//红色背景
				HSSFCellStyle redBackGroundStyle = work.createCellStyle();
				redBackGroundStyle.cloneStyleFrom(baseStyle);
				redBackGroundStyle.setFont(greyfont);
				redBackGroundStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				redBackGroundStyle.setFillForegroundColor(HSSFColor.RED.index);
				
				HSSFRow row = null;
				HSSFCell cell = null;
				CellRangeAddress region = null;
				
				int columnIndex = 3;
				int curWeekcolumnIndex = 0;
				int lastcolumnIndex = columnIndex + length +1;//最后一列
				
				//最后一列备注处理
				for(int i = 0;i<=13;i++){
					row = sheet.getRow(i);
					HSSFCell srcCell = row.getCell(4);
					HSSFCell distCell = row.createCell(lastcolumnIndex);
					CopyByPoi.copyCell(srcCell, distCell, true);
					row.removeCell(srcCell);
				}
				
				for(int i = 17;i<=26;i++){
					row = sheet.getRow(i);
					HSSFCell srcCell = row.getCell(4);
					HSSFCell distCell = row.createCell(lastcolumnIndex);
					CopyByPoi.copyCell(srcCell, distCell, true);
					row.removeCell(srcCell);
				}
				
				sheet.getRow(1).getCell(lastcolumnIndex).setCellValue("日期：" + 
																		DateUtil.toString(condition.getCount_date_start(), "MM月dd日") + 
																		"--" + 
																		DateUtil.toString(condition.getCount_date_end(), "MM月dd日"));
				sheet.setColumnWidth(lastcolumnIndex, sheet.getColumnWidth(4));
				
				region = new CellRangeAddress(2, 3, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(8, 9, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(12, 13, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(17, 18, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(19, 20, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(21, 22, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				region = new CellRangeAddress(24, 26, lastcolumnIndex, lastcolumnIndex);
				sheet.addMergedRegion(region);
				
				//公式
				FormulaEvaluator evaluator = work.getCreationHelper().createFormulaEvaluator();
				
				//开始处理内容
				//月份map
				Map<String,List<Integer>> monthsMap = new TreeMap<String,List<Integer>>();
				
				for(int i = length -1;i >= 0;i--){
					columnIndex ++;
					
					if(i==1) curWeekcolumnIndex = columnIndex;
					
					WeeklyKpiDataEntity temp = listBeans.get(i);

					row = sheet.getRow(2);//月份
					cell = row.createCell(columnIndex);
					String yearMonth = DateUtil.toString(temp.getCount_date_end(), "yyyy年MM月");
					cell.setCellValue(yearMonth);
					cell.setCellStyle(yearMonthStyle);
					
					if(monthsMap.containsKey(yearMonth)){
						monthsMap.get(yearMonth).add(columnIndex);
					}else{
						List<Integer> colnumList = new ArrayList<Integer>();
						colnumList.add(columnIndex);
						monthsMap.put(yearMonth, colnumList);
					}
					
					
					row = sheet.getRow(3);//周数
					cell = row.createCell(columnIndex);
					cell.setCellValue(temp.getWeekly_of_year()+"W");
					cell.setCellStyle(weeklyOfYearStyle);
					
					row = sheet.getRow(4);//见后
					cell = row.createCell(columnIndex);
					cell.setCellValue(PathConsts.SCHEDULE_SETTINGS.getProperty("target." + DateUtil.toString(temp.getCount_date_end(), "yyyy") + "." + temp.getWeekly_of_year() + "W"));
					cell.setCellStyle(greyBackGroundStyle);
					
					row = sheet.getRow(5);//到货受理数
					cell = row.createCell(columnIndex);
					if(temp.getRegistration()!=null){
						cell.setCellValue(temp.getRegistration());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(6);//受理数差异（（目标数+返回数）比）
					cell = row.createCell(columnIndex);
					cell.setCellFormula("IF(ISERROR(" + ReportUtils.getPosition(columnIndex, 6) + "-" + ReportUtils.getPosition(columnIndex, 10) + "-" + ReportUtils.getPosition(columnIndex, 5) + "),\"-\","+ReportUtils.getPosition(columnIndex, 6) + "-" + ReportUtils.getPosition(columnIndex, 10) + "-" + ReportUtils.getPosition(columnIndex, 5)+")");
					int evaluatedCellType = evaluator.evaluateFormulaCell(cell);//结果的返回类型
				 	if(evaluatedCellType == HSSFCell.CELL_TYPE_NUMERIC){
				 		if(cell.getNumericCellValue() < 0) {
				 			cell.setCellStyle(greyBackGroundRedFontStyle);
				 		}else{
				 			cell.setCellStyle(greyBackGroundStyle);
				 		}
				 	}else{
				 		cell.setCellStyle(greyBackGroundStyle);
				 	}
					
					row = sheet.getRow(7);//修理同意数
					cell = row.createCell(columnIndex);
					if(temp.getUser_agreement()!=null){
						cell.setCellValue(temp.getUser_agreement());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(8);//返回ＯＳＨ修理
					cell = row.createCell(columnIndex);
					if(temp.getReturn_to_osh()!=null){
						cell.setCellValue(temp.getReturn_to_osh());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(9);//未修理返回
					cell = row.createCell(columnIndex);
					if(temp.getUnrepair()!=null){
						cell.setCellValue(temp.getUnrepair());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(10);//出货总数
					cell = row.createCell(columnIndex);
					if(temp.getShipment()!=null){
						cell.setCellValue(temp.getShipment());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(11);//出货数差异（目标数比）
					cell = row.createCell(columnIndex);
					cell.setCellFormula("IF(ISERROR(" + ReportUtils.getPosition(columnIndex, 11) + "-" + ReportUtils.getPosition(columnIndex, 5) + "),\"-\","+ReportUtils.getPosition(columnIndex, 11) + "-" + ReportUtils.getPosition(columnIndex, 5) +")");
					evaluatedCellType = evaluator.evaluateFormulaCell(cell);//结果的返回类型
				 	if(evaluatedCellType == HSSFCell.CELL_TYPE_NUMERIC){
				 		if(cell.getNumericCellValue() < 0) {
				 			cell.setCellStyle(greyBackGroundRedFontStyle);
				 		}else{
				 			cell.setCellStyle(greyBackGroundStyle);
				 		}
				 	}else{
				 		cell.setCellStyle(greyBackGroundStyle);
				 	}
					
					
					row = sheet.getRow(12);//WIP在修数
					cell = row.createCell(columnIndex);
					if(temp.getWork_in_process()!=null){
						cell.setCellValue(temp.getWork_in_process());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(13);//WIP总数
					cell = row.createCell(columnIndex);
					if(temp.getWork_in_storage()!=null){
						cell.setCellValue(temp.getWork_in_storage());
					}else{
						cell.setCellValue("-");
					}
					cell.setCellStyle(whiteBackGroundStyle);
					
					row = sheet.getRow(17);//大修理LT
					cell = row.createCell(columnIndex);
					if(temp.getIntime_complete_rate()!=null){
						checkLower(cell, temp.getIntime_complete_rate().toString(), L_INLINE_COMPLETE_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(18);//平均修理周期
					cell = row.createCell(columnIndex);
					if(temp.getAverage_repair_lt()!=null){
						checkUpper(cell, temp.getAverage_repair_lt().toString(), 6, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(19);//零件到达后4天内出货比率
					cell = row.createCell(columnIndex);
					if(temp.getIntime_work_out_rate()!=null){
						checkLower(cell, temp.getIntime_work_out_rate().toString(), L_PART_ARRIVE_COMPLETE_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(20);//平均工作周期WL
					cell = row.createCell(columnIndex);
					if(temp.getAverage_work_lt()!=null){
						checkUpper(cell, temp.getAverage_work_lt().toString(), U_WORK_LEAD_TIME, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(21);//当天零件BO率
					cell = row.createCell(columnIndex);
					if(temp.getBo_rate()!=null){
						checkUpper(cell, temp.getBo_rate().toString(), U_ONE_DAY_BO_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(22);//三天零件BO率
					cell = row.createCell(columnIndex);
					if(temp.getBo_3day_rate()!=null){
						checkUpper(cell, temp.getBo_3day_rate().toString(), U_THREE_DAYS_BO_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(23);//工程内直行率
					cell = row.createCell(columnIndex);
					if(temp.getInline_passthrough_rate()!=null){
						checkLower(cell, temp.getInline_passthrough_rate().toString(), L_INLINE_PASSTHROUGH_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(24);//最终检查不合格件数
					cell = row.createCell(columnIndex);
					if(temp.getFinal_check_forbid_count()!=null){
						checkUpper(cell, temp.getFinal_check_forbid_count().toString(), 0, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(25);//最终检查合格率
					cell = row.createCell(columnIndex);
					if(temp.getFinal_inspect_pass_rate()!=null){
						checkLower(cell, temp.getFinal_inspect_pass_rate().toString(), L_FINAL_INSPECT_PASS_RATE, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
					
					row = sheet.getRow(26);//内镜保修期内返品率（含新品不良）
					cell = row.createCell(columnIndex);
					if(temp.getService_repair_back_rate()!=null){
						checkUpper(cell, temp.getService_repair_back_rate().toString(), 0.9, greenBackGroundStyle, yelloBackGroundStyle, redBackGroundStyle);
					}else{
						cell.setCellValue("-");
						cell.setCellStyle(greenBackGroundStyle);
					}
										
					//设置列宽
					sheet.setColumnWidth(columnIndex, 256*6);
				}
				
				//当前周字体变黑
				for(int i = 3;i<=13;i++){
					cell = sheet.getRow(i).getCell(curWeekcolumnIndex);
					HSSFCellStyle style = cell.getCellStyle();
					
					HSSFCellStyle newStyle = work.createCellStyle();
					newStyle.cloneStyleFrom(style);
					if(i == 3){
						newStyle.setFont(boldfont);
					}else if(i == 6 || i == 11){//受理数差异（（目标数+返回数）比）、出货数差异（目标数比）
					 	int evaluatedCellType = evaluator.evaluateFormulaCell(cell);//结果的返回类型
					 	if(evaluatedCellType == HSSFCell.CELL_TYPE_NUMERIC){
					 		if(cell.getNumericCellValue() < 0) {
					 			newStyle.setFont(redfont);
					 		}else{
					 			newStyle.setFont(blackfont);
					 		}
					 	}else{
					 		newStyle.setFont(blackfont);
					 	}
					}else{
						newStyle.setFont(blackfont);
					}
					cell.setCellStyle(newStyle);
				}
				
				for(int i = 17;i<=26;i++){
					cell = sheet.getRow(i).getCell(curWeekcolumnIndex);
					HSSFCellStyle style = cell.getCellStyle();
					HSSFCellStyle newStyle = work.createCellStyle();
					newStyle.cloneStyleFrom(style);
					newStyle.setFont(blackfont);
					cell.setCellStyle(newStyle);
				}
				
				//水平分割实线
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(13 , 13, 4, columnIndex), sheet, work);
				HSSFRegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(13 , 13, 4, columnIndex), sheet, work);
				
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(22 , 22, 4, columnIndex), sheet, work);
				HSSFRegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(22 , 22, 4, columnIndex), sheet, work);
				
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(26 , 26, 4, columnIndex), sheet, work);
				HSSFRegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(26 , 26, 4, columnIndex), sheet, work);
				
				//合并月份
				Set<String> yearMonthSet = monthsMap.keySet();
				Iterator<String> keys = yearMonthSet.iterator();
				while(keys.hasNext()){
					List<Integer> list = monthsMap.get(keys.next());
					int firstCol = list.get(0);
					int lastCol = list.get(list.size()-1);
					region = new CellRangeAddress(2 , 2, firstCol, lastCol);
					sheet.addMergedRegion(region);
					//边框黑线
					HSSFRegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(4 , 26, lastCol, lastCol), sheet, work);
					HSSFRegionUtil.setRightBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(4 , 26, lastCol, lastCol), sheet, work);
				}
				
				//合并单元格（特别説明事項、来週予定）
				for(int i = 27;i<=39;i++){
					region = new CellRangeAddress(i , i, 1, lastcolumnIndex);
					sheet.addMergedRegion(region);
				}
				
				HSSFRegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(27 , 39, lastcolumnIndex, lastcolumnIndex), sheet, work);
				HSSFRegionUtil.setRightBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(27 , 39, lastcolumnIndex, lastcolumnIndex), sheet, work);
				
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(34 , 34, 4, lastcolumnIndex), sheet, work);
				HSSFRegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(34 , 34, 4, lastcolumnIndex), sheet, work);
				
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, new CellRangeAddress(39 , 39, 4, lastcolumnIndex), sheet, work);
				HSSFRegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, new CellRangeAddress(39 , 39, 4, lastcolumnIndex), sheet, work);
				
				//隐藏列
				if(length > 13){
					int startIndex = 4;
					for(int i= 0;i<length - 13;i++){
						sheet.setColumnHidden(startIndex, true);
						startIndex ++;
					}
				}
				
				out = new FileOutputStream(cachePath);
				work.write(out);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void checkUpper(HSSFCell cell,String cellValue,double upperValue,HSSFCellStyle greenStyle,HSSFCellStyle yelloStyle,HSSFCellStyle redStyle){
		double nUpperClose = upperValue * 1.1;
		cell.setCellValue(cellValue);
		double thisVal = Double.parseDouble(cellValue);
		
		if (thisVal > nUpperClose) {
			cell.setCellStyle(redStyle);
		} else if (thisVal > upperValue) {
			cell.setCellStyle(yelloStyle);
		}else{
			cell.setCellStyle(greenStyle);
		}
	}
	
	private void checkLower(HSSFCell cell,String cellValue,double lowerValue,HSSFCellStyle greenStyle,HSSFCellStyle yelloStyle,HSSFCellStyle redStyle){
		double nLowerClose = lowerValue * 0.9;
		cell.setCellValue(cellValue);
		double thisVal = Double.parseDouble(cellValue);
		
		if (thisVal < nLowerClose) {
			cell.setCellStyle(redStyle);
		} else if (thisVal < lowerValue) {
			cell.setCellStyle(yelloStyle);
		}else{
			cell.setCellStyle(greenStyle);
		}
	}
	
	public void checkCountDateEndIsExist(ActionForm form,List<MsgInfo> errors,SqlSession conn){
		WeeklyKpiDataEntity entity = new WeeklyKpiDataEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		WeeklyKpiDataMapper dao = conn.getMapper(WeeklyKpiDataMapper.class);
		int num = dao.checkCountDateEndExist(entity);
		
		if(num > 0){
			MsgInfo info = new MsgInfo();
			info.setErrcode("dbaccess.recordDuplicated");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", DateUtil.toString(entity.getCount_date_end(), DateUtil.DATE_PATTERN)));
			errors.add(info);
		}
	}
	
}
