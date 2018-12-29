package com.osh.rvs.job;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTextbox;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class DailyWorkSheetsJob implements Job {

	public static Logger _log = Logger.getLogger("DailyWorkSheetsJob");

	private static DailyWorkSheetsJob instance = new DailyWorkSheetsJob();

	private static Map<String,String> disinfectManageCodeMap = new HashMap<String,String>();
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar monthStart = Calendar.getInstance();

		_log.info("DailyWorkSheetsJob: " + jobKey + " executing at " + monthStart);

		monthStart.set(Calendar.DATE, 1);
		monthStart.set(Calendar.HOUR_OF_DAY, 0);
		monthStart.set(Calendar.MINUTE, 0);
		monthStart.set(Calendar.SECOND, 0);
		monthStart.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();
		
//		List<DevicesEntity> list = conn.getMapper(DevicesMapper.class).searchDisinfectManageCode();
//		
//		for(DevicesEntity entity:list){
//			disinfectManageCodeMap.put(entity.getDevices_manage_id(), entity.getManage_code());
//		}
		// get from codelist TODO
		disinfectManageCodeMap.put("00000000000", "(手动)");
		
		Calendar today = Calendar.getInstance();
		
		try{
			acceptAndDisinfectAndSterilize(today,conn);
		}catch(Exception e){
			_log.error("inline:" + e.getMessage());
		}
		
		try{
			inline(today,conn);
		}catch(Exception e){
			_log.error("inline:" + e.getMessage());
		}
		
		try{
			shipping(today,conn);
		}catch(Exception e){
			_log.error("shipping:" + e.getMessage());
		}
		
		int day =  today.get(Calendar.DAY_OF_WEEK);
		if(day == Calendar.FRIDAY){//周五
			try{
				unRepairAdjustRecort(today, conn);
			}catch(Exception e){
				_log.error("unRepairAdjustRecort:" + e.getMessage());
			}
			
		}

		//当月最后一天
		int lastDay = today.getActualMaximum(Calendar.DATE);
		//当天
		int curDay = today.get(Calendar.DATE);
		if(curDay==lastDay){
			Calendar curMon = Calendar.getInstance();
			curMon.set(Calendar.DATE, 1);
			curMon.set(Calendar.HOUR_OF_DAY, 0);
			curMon.set(Calendar.MINUTE, 0);
			curMon.set(Calendar.SECOND, 0);
			curMon.set(Calendar.MILLISECOND, 0);
			try{
				advancedRecoveryRecord(curMon,conn);
			}catch(Exception e){
				_log.error("advancedRecoveryRecord:" + e.getMessage());
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

		today.set(Calendar.YEAR, 2018);
		today.set(Calendar.MONTH, Calendar.JANUARY);
		today.set(Calendar.DATE, 3);
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();
		
		/*List<DevicesEntity> list = conn.getMapper(DevicesMapper.class).searchDisinfectManageCode();
		
		for(DevicesEntity entity:list){
			disinfectManageCodeMap.put(entity.getDevices_manage_id(), entity.getManage_code());
		}
		disinfectManageCodeMap.put("00000000000", "(手动)");*/

		PathConsts.BASE_PATH = "E:\\rvsG";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
		PathConsts.PCS_TEMPLATE = "\\PcsTemplates";
		PathConsts.PROPERTIES = "\\PROPERTIES";
		;
		PathConsts.REPORT = "\\Reports";
		PathConsts.IMAGES = "\\images";

		PathConsts.load();

		DailyWorkSheetsJob job = new DailyWorkSheetsJob();
		
		job.inline(today,conn);
		job.shipping(today,conn);
		job.unRepairAdjustRecort(today, conn);
		job.advancedRecoveryRecord(today,conn);
		job.acceptAndDisinfectAndSterilize(today, conn);

	}

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}
	
	/**
	 * 受理消毒灭菌
	 * @param today
	 * @param conn
	 */
	public void acceptAndDisinfectAndSterilize(Calendar today, SqlSession conn) {
		//模板文件
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "MS0101-8内镜受理记录表.xls";
		//生成文件的文件名称
		String cacheFilename = "MS0101-8内镜受理记录表-" + DateUtil.toString(today.getTime(), "yyyy-MM-dd") + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\accept\\"+ DateUtil.toString(today.getTime(), "yyyyMM") + "\\" + cacheFilename;
		
		MaterialService service = new MaterialService();
		List<MaterialEntity> listBeans = service.acceptAndDisinfectAndSterilize(DateUtil.toString(today.getTime(),DateUtil.DATE_PATTERN),conn);
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

				int pageNum = 0;
				
				//设置分页区域
				sheet.setRowBreak(28);
				sheet.setColumnBreak(13);
				
				if(length > 17){
					//需要复制的页数(不包含第一页)
					pageNum=(length-17) % 17 == 0 ? (length - 17) / 17 : (length - 17) / 17 + 1;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					//图形
					HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
					List<HSSFShape> shapeList = patriarch.getChildren();
					int pictuteNums = shapeList.size();
					
					//生成行
					for(int j = 0;j < pageNum; j++){
						HSSFRow fromRow = null;
						HSSFRow toRow = null;
						
						int lastRowNum = sheet.getLastRowNum()+1;//最后一行索引+1
						
						//复制普通单元格
						for(int index = 0;index <= 28; index++){
							int dex = lastRowNum + index;
							fromRow = sheet.getRow(index);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow,  work,true);
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {  
							CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
							int firstRow = cellRangeAddress.getFirstRow();
							int lastRow = cellRangeAddress.getLastRow();
							int firstCol = cellRangeAddress.getFirstColumn();
							int lastCol = cellRangeAddress.getLastColumn();
							
							CellRangeAddress  region = new CellRangeAddress(firstRow +lastRowNum, lastRow+lastRowNum, firstCol, lastCol);
							sheet.addMergedRegion(region);
						}
						
						//复制图行
						for (int i = 0; i < pictuteNums; i++) {
							HSSFShape shape = shapeList.get(i);
							HSSFClientAnchor anchor = (HSSFClientAnchor)shape.getAnchor();
							if (shape instanceof HSSFPicture) {//图片
								HSSFPicture picture = (HSSFPicture)shape;

								int dx1 = anchor.getDx1();
								int dy1 = anchor.getDy1();
								int dx2 = anchor.getDx2();
								if(dx2 > 1023) dx2 = 1023;
								int dy2 = anchor.getDy2();
								if(dy2 > 255) dy2 = 255;
								int row1 = anchor.getRow1();
								short col1 = anchor.getCol1();
								int row2 = anchor.getRow2();
								short col2 = anchor.getCol2();
								
								anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum, col2, row2+lastRowNum);
								anchor.setAnchorType(2);
								
								patriarch = sheet.createDrawingPatriarch();
								patriarch.createPicture(anchor,work.addPicture(picture.getPictureData().getData(), HSSFWorkbook.PICTURE_TYPE_PNG));
							}
						}
						sheet.setRowBreak(sheet.getLastRowNum());
					}
				}
				//重新设置打印区域
				work.setPrintArea(0, 0, 13, 0, sheet.getLastRowNum());
				sheet.setPrintGridlines(false);
				
				//往Excel填充数据
				for(int i = 0;i < length; i++){
					MaterialEntity entity = listBeans.get(i);
					
					int iRow = i/17 * 12 + 6 + i;
					HSSFRow  row = sheet.getRow(iRow);
					
					row.getCell(0).setCellValue(DateUtil.toString(entity.getAccept_finish_time(),"HH:mm"));//时间
					
					String operator_name = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + entity.getAccept_job_no();
					insertImage(work,sheet,1,iRow,operator_name);//担当
					
					row.getCell(2).setCellValue(entity.getModel_name());//型号
					
					row.getCell(3).setCellValue(entity.getSerial_no());//机身号
					
					String pcs_inputs = entity.getPcs_inputs();
					if (pcs_inputs != null) {
						@SuppressWarnings("unchecked")
						Map<String, String> input_pcs = JSON.decode(pcs_inputs, Map.class);
						String manageNO = input_pcs.get("ER12101");
						if ("B075".equals(manageNO)) {
							row.getCell(5).setCellValue("√");
						} else if("B076".equals(manageNO)){
							row.getCell(6).setCellValue("√");
						}
						
						manageNO = input_pcs.get("ER12102");
						if ("B006".equals(manageNO)) {
							row.getCell(7).setCellValue("√");
						} else if("B007".equals(manageNO)){
							row.getCell(8).setCellValue("√");
						}
						
						manageNO = input_pcs.get("ER13101");
						if ("1号".equals(manageNO)) {
							row.getCell(9).setCellValue("√");
						} else if("2号".equals(manageNO)){
							row.getCell(10).setCellValue("√");
						}
					} 
					
					row.getCell(11).setCellValue(DateUtil.toString(entity.getDisinfect_finish_time(),"HH:mm"));//时间
					
					operator_name = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + entity.getDisinfect_job_no();
					insertImage(work,sheet,12,iRow,operator_name);//担当
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

	private SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");

	public void inline(Calendar calendar,SqlSession conn) {
		String path = PathConsts.BASE_PATH+PathConsts.REPORT_TEMPLATE+"\\"+"内镜投线记录表.xls";
		String cachePath = PathConsts.BASE_PATH+PathConsts.REPORT+"\\inline\\"+DateUtil.toString(calendar.getTime(), "yyyyMM")+"\\"+"内镜投线记录表-" + DateUtil.toString(calendar.getTime(), "yyyy-MM-dd") + ".xls";
		
		SimpleDateFormat agreeDateFormat=new SimpleDateFormat("MM-dd ");
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);	
		List<MaterialEntity> lResultBean = dao.searchInlineMaterial(DateUtil.toString(calendar.getTime(),DateUtil.DATE_PATTERN));
		if(lResultBean.size()==0) return;

		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		OutputStream out = null;
		InputStream in = null;
		try {
			in = new FileInputStream(cachePath);
			HSSFWorkbook work = new HSSFWorkbook(in);
			HSSFSheet sheet = work.getSheetAt(0);	
			
				//设置字体大小
				HSSFFont font  = work.createFont();
				font.setFontHeightInPoints((short) 10);
				font.setFontName("宋体");
			/*设置单元格内容居中显示*/
			HSSFCellStyle styleAlignCenter = work.createCellStyle();
			styleAlignCenter.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignCenter.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderRight(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styleAlignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignCenter.setWrapText(true);
				styleAlignCenter.setFont(font);
			/*设置单元格内容居左显示*/
			HSSFCellStyle styleAlignLeft = work.createCellStyle();
			styleAlignLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignLeft.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAlignLeft.setWrapText(true); 
				styleAlignLeft.setFont(font);
			
			int index = 0;
			for (int i = 0; i < lResultBean.size(); i++) {					
				index++;
				MaterialEntity materialEntity = lResultBean.get(i);
				HSSFRow row = sheet.createRow(index);	
				
				//序列号
				HSSFCell indexCell = row.createCell(0);
				indexCell.setCellValue(index);
				indexCell.setCellStyle(styleAlignCenter);
                
				//投线时间
				HSSFCell inlineTimeCell= row.createCell(1);
				inlineTimeCell.setCellValue(timeFormat.format(materialEntity.getInline_time()));
				inlineTimeCell.setCellStyle(styleAlignCenter);
				
				//SORC NO.
				HSSFCell sorcNoCell = 	row.createCell(2);
				sorcNoCell.setCellValue(materialEntity.getSorc_no());
				sorcNoCell.setCellStyle(styleAlignLeft);

				//型号
				HSSFCell modelNameCell = row.createCell(3);
				modelNameCell.setCellValue(materialEntity.getModel_name());
				modelNameCell.setCellStyle(styleAlignLeft);
				
				//机身号
				HSSFCell serialNoCell = row.createCell(4);
				serialNoCell.setCellValue(materialEntity.getSerial_no());
				serialNoCell.setCellStyle(styleAlignLeft);
				
				//等级
				HSSFCell levelCell = row.createCell(5);
				Integer level = materialEntity.getLevel();
				String levelData = CodeListUtils.getValue("material_level_all",level.toString());
				levelCell.setCellValue(levelData);
				levelCell.setCellStyle(styleAlignCenter);
			
				//客户同意日
				HSSFCell agreedDateCell = row.createCell(6);
				agreedDateCell.setCellValue(agreeDateFormat.format(materialEntity.getAgreed_date()));
				agreedDateCell.setCellStyle(styleAlignCenter);
				
				//预定日期内投线
				HSSFCell twoDaysOfLinesCell = row.createCell(7);
				if ("0".equals(dao.getTwoDaysOfLines(materialEntity.getMaterial_id()))) {
					HSSFCellStyle style = work.createCellStyle();
						HSSFFont fontjudge  = work.createFont();
						fontjudge.setColor(HSSFColor.RED.index);
					style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
					style.setBorderTop(HSSFCellStyle.BORDER_THIN); 
					style.setBorderRight(HSSFCellStyle.BORDER_THIN); 
					style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
					style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
					style.setWrapText(true);
						style.setFont(fontjudge);
					twoDaysOfLinesCell.setCellValue("╳");
					twoDaysOfLinesCell.setCellStyle(style);
				} else{					
					HSSFCellStyle style = work.createCellStyle();
						HSSFFont fontjudge  = work.createFont();
						fontjudge.setColor(HSSFColor.GREEN.index);
					style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
					style.setBorderTop(HSSFCellStyle.BORDER_THIN); 
					style.setBorderRight(HSSFCellStyle.BORDER_THIN); 
					style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
					style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
					style.setWrapText(true);
						style.setFont(fontjudge);	
					twoDaysOfLinesCell.setCellValue("√");
					twoDaysOfLinesCell.setCellStyle(styleAlignCenter);
				}		
			}
			out = new FileOutputStream(cachePath);
			try {
				work.write(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			_log.error(e.getMessage(), e);
		} catch (IOException e) {
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
	
	/**
	 * 出货
	 * 
	 * @param month
	 * @param conn
	 */
	public void shipping(Calendar calendar, SqlSession conn) {
		MaterialService service = new MaterialService();
		List<MaterialEntity> listBeans = service.getShippingTodayMaterialDetail(DateUtil.toString(calendar.getTime(),DateUtil.DATE_PATTERN),conn);
		
		int length = listBeans.size();
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "MS0101-16内镜出货记录表.xls";
		String cacheFilename = "MS0101-16内镜出货记录表-" + DateUtil.toString(calendar.getTime(), "yyyy-MM-dd") + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\shipping\\"+ DateUtil.toString(calendar.getTime(), "yyyyMM") + "\\" + cacheFilename;
		
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
				
				int pageNum = 0;
				
				//设置分页区域
				sheet.setRowBreak(36);
				sheet.setColumnBreak(6);
				
				if(length > 27){
					//需要复制的页数(不包含第一页)
					pageNum=(length-27) % 27 == 0 ? (length - 27) / 27 : (length - 27) / 27 + 1;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					//图形
					HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
					List<HSSFShape> shapeList = patriarch.getChildren();
					int pictuteNums = shapeList.size();
					
					//生成行
					for(int j = 0;j < pageNum; j++){
						HSSFRow fromRow = null;
						HSSFRow toRow = null;
						
						int lastRowNum = sheet.getLastRowNum()+1;//最后一行索引+1
						
						//复制普通单元格
						for(int index = 0;index <= 36; index++){
							int dex = lastRowNum + index;
							fromRow = sheet.getRow(index);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow,  work,true);
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {  
							CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
							int firstRow = cellRangeAddress.getFirstRow();
							int lastRow = cellRangeAddress.getLastRow();
							int firstCol = cellRangeAddress.getFirstColumn();
							int lastCol = cellRangeAddress.getLastColumn();
							
							CellRangeAddress  region = new CellRangeAddress(firstRow +lastRowNum, lastRow+lastRowNum, firstCol, lastCol);
							sheet.addMergedRegion(region);
						}
						
						//复制图行
						for (int i = 0; i < pictuteNums; i++) {
							HSSFShape shape = shapeList.get(i);
							HSSFClientAnchor anchor = (HSSFClientAnchor)shape.getAnchor();
							if (shape instanceof HSSFPicture) {	//图片
								HSSFPicture picture = (HSSFPicture)shape;

								int dx1 = anchor.getDx1();
								int dy1 = anchor.getDy1();
								int dx2 = anchor.getDx2();
								if(dx2 > 1023) dx2 = 1023;
								int dy2 = anchor.getDy2();
								if(dy2 > 255) dy2 = 255;
								int row1 = anchor.getRow1();
								short col1 = anchor.getCol1();
								int row2 = anchor.getRow2();
								short col2 = anchor.getCol2();
								anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum, col2, row2+lastRowNum);
								
								patriarch = sheet.createDrawingPatriarch();
								anchor.setAnchorType(2);
								patriarch.createPicture(anchor,work.addPicture(picture.getPictureData().getData(), HSSFWorkbook.PICTURE_TYPE_PNG));
							}
						}
						sheet.setRowBreak(sheet.getLastRowNum());
					}
				}
				
				//重新设置打印区域
				work.setPrintArea(0, 0, 6, 0, sheet.getLastRowNum());
				sheet.setPrintGridlines(false);
				
				//往Excel填充数据
				for(int i = 0;i < length; i++){
					MaterialEntity entity = listBeans.get(i);
					
					int iRow = i/27 * 10 + 5 + i;
					HSSFRow  row = sheet.getRow(iRow);
					
					row.getCell(0).setCellValue(DateUtil.toString(entity.getFinish_time(),"HH:mm"));//时间
					
					row.getCell(1).setCellValue(entity.getOmr_notifi_no());//通知单
					
					row.getCell(2).setCellValue(entity.getModel_name());//型号
					
					row.getCell(3).setCellValue(entity.getSerial_no());//机身号
					
					String operator_name = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + entity.getJob_no();
					insertImage(work,sheet,4,iRow,operator_name);//捆包员
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

		shippingRegist(calendar, listBeans);
	}

	private static final String CUSTOMOR_SELF = "Olympus社内";

	/**
	 * 出货登记
	 * @param calendar
	 * @param listBeans
	 */
	public void shippingRegist(Calendar calendar,List<MaterialEntity> listBeans){
		Collections.sort(listBeans, new Comparator<MaterialEntity>() {
			@Override
			public int compare(MaterialEntity o1, MaterialEntity o2) {
				// 华北最优先
				if (o1.getOcm() == 2 && o2.getOcm() != 2) {
					return -1;
				}
				if (o1.getOcm() != 2 && o2.getOcm() == 2) {
					return 1;
				}

				// 华东最靠后
				if (o1.getOcm() == 1 && o2.getOcm() != 1) {
					return 1;
				}
				if (o1.getOcm() != 1 && o2.getOcm() == 1) {
					return -1;
				}

				// 其他比较省份
				if (o1.getBound_out_ocm() != null && o2.getBound_out_ocm() != null) {
					// 备品优先
					int o1Bound_out_ocm = o1.getBound_out_ocm();
					int o2Bound_out_ocm = o2.getBound_out_ocm();
					if (CUSTOMOR_SELF.equals(o1.getCustomer_name())) {
						o1Bound_out_ocm = 0;
					}
					if (CUSTOMOR_SELF.equals(o2.getCustomer_name())) {
						o2Bound_out_ocm = 0;
					}
					return o1Bound_out_ocm - o2Bound_out_ocm;
				}
				return 0;
			}
		});

		int length = listBeans.size();
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "出货登记表.xls";
		String cacheFilename = "出荷地区明细表-" + DateUtil.toString(calendar.getTime(), "yyyy-MM-dd") + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\shipping\\"+ DateUtil.toString(calendar.getTime(), "yyyyMM") + "\\" + cacheFilename;
		
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
				
				HSSFFont font  = work.createFont();
				font.setFontHeightInPoints((short) 11);
				font.setFontName("宋体");

				HSSFCellStyle style = work.createCellStyle();
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
				style.setBorderTop(HSSFCellStyle.BORDER_THIN); 
				style.setBorderRight(HSSFCellStyle.BORDER_THIN); 
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setWrapText(true);
				style.setFont(font);
				
				HSSFRow row = null;
				HSSFCell cell = null;
				MaterialEntity entity = null;
				
				int index = 1;
				//往Excel填充数据
				for(int i = 0;i < length; i++){
					index ++;
					entity = listBeans.get(i);
					
					row = sheet.createRow(index);

					cell = row.createCell(0);//序号
					cell.setCellValue(index);
					cell.setCellStyle(style);
					
					cell = row.createCell(0);//维修单号
					cell.setCellValue(entity.getOmr_notifi_no());
					cell.setCellStyle(style);
					
					cell = row.createCell(1);//型号
					cell.setCellValue(entity.getModel_name());
					cell.setCellStyle(style);
					
					cell = row.createCell(2);//机身号
					cell.setCellValue(entity.getSerial_no());
					cell.setCellStyle(style);
					
					cell = row.createCell(3);//出荷日
					cell.setCellValue(DateUtil.toString(calendar.getTime(), DateUtil.ISO_DATE_PATTERN));
					cell.setCellStyle(style);

					String sCustomerName = entity.getCustomer_name();
					cell = row.createCell(4);//医院名
					cell.setCellValue(sCustomerName);
					cell.setCellStyle(style);

					cell = row.createCell(7);//状态
					if (entity.getBreak_back_flg() == 2) 
					{
						cell.setCellValue("不修理返还");
					} else {
						cell.setCellValue("修理完成");
					}
					cell.setCellStyle(style);
					
					cell = row.createCell(8);//备注
					cell.setCellValue("");
					cell.setCellStyle(style);

					Integer bound_out_ocm = entity.getBound_out_ocm();
					Integer ocm = entity.getOcm();
					if (bound_out_ocm == null || ocm == null) {
						continue;
					}
					Integer area = entity.getArea();
					String sOcm = CodeListUtils.getValue("material_ocm", ocm.toString());
					String sBoundOutOcm= CodeListUtils.getValue("material_direct_area", bound_out_ocm.toString());
					String sLargeArea = CodeListUtils.getValue("material_large_area", area.toString()) + "销售本部";

					cell = row.createCell(5);//所属省份
					cell.setCellStyle(style);
					
					String areaAddress = "";//
					
					if(CUSTOMOR_SELF.equals(sCustomerName)){//备品
						areaAddress = PathConsts.DIRECTAREA_SETTINGS.getProperty(sOcm);
						if(CommonStringUtil.isEmpty(areaAddress)){//按ocm取不到
							areaAddress = PathConsts.DIRECTAREA_SETTINGS.getProperty(CUSTOMOR_SELF);
							cell.setCellValue("番禺");
						} else {
							cell.setCellValue(sLargeArea);
						}
					}else{
						areaAddress = PathConsts.DIRECTAREA_SETTINGS.getProperty(sBoundOutOcm);
						if(CommonStringUtil.isEmpty(areaAddress) && ocm != null){//按省份取不到
							areaAddress = PathConsts.DIRECTAREA_SETTINGS.getProperty(sOcm);
							cell.setCellValue(sLargeArea);
						} else {
							cell.setCellValue(sBoundOutOcm);
						}
					}
					
					cell = row.createCell(6);//返送地址
					cell.setCellValue(areaAddress);
					cell.setCellStyle(style);
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
	
	/**
	 * 未修理翻修品清点状况记录
	 * @param calendar
	 * @param conn
	 */
	public void unRepairAdjustRecort(Calendar calendar, SqlSession conn){
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\未修理翻修品清点状况记录表-模板.xls";
		String cacheFilename = "未修理翻修品清点状况记录-" + DateUtil.toString(calendar.getTime(), "yyyy-MM-dd") + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\wip\\"+ DateUtil.toString(calendar.getTime(), "yyyyMM") + "\\" + cacheFilename;
		
		MaterialService service = new MaterialService();
		List<MaterialEntity> listBeans = service.unRepairAdjust(conn);
		int length = listBeans.size();//数据长度
		
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
				work.setSheetName(0, cacheFilename.replace(".xls", ""));
				
				HSSFSheet sheet = work.getSheetAt(0);//取得第一个Sheet
				HSSFRow row  = sheet.getRow(4);
				HSSFCell cell = row.getCell(5);
				cell.setCellValue(DateUtil.toString(calendar.getTime(), DateUtil.DATE_PATTERN));//清点日期
				
				int pageNum = 0;
				int margin = 2;//每页间隔
				
				if(length > 47){
					//需要复制的页数(不包含第一页)
					pageNum=(length-47)%47==0 ? (length-47)/47 :(length-47)/47+1;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					//图形
					HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
					List<HSSFShape> shapeList = patriarch.getChildren();
					int pictuteNums = shapeList.size();
					
					//生成行
					for(int j = 0;j < pageNum; j++){
						HSSFRow fromRow = null;
						HSSFRow toRow = null;
						
						int lastRowNum = sheet.getLastRowNum()+1;//最后一行索引+1
						
						//复制普通单元格
						for(int index = 0;index <= 57; index++){
							int dex = lastRowNum + index + margin;
							fromRow = sheet.getRow(index);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow,  work,true);
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {  
							CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
							int firstRow = cellRangeAddress.getFirstRow();
							int lastRow = cellRangeAddress.getLastRow();
							int firstCol = cellRangeAddress.getFirstColumn();
							int lastCol = cellRangeAddress.getLastColumn();
							
							CellRangeAddress  region = new CellRangeAddress(firstRow +lastRowNum+margin, lastRow+lastRowNum+margin, firstCol, lastCol);
							sheet.addMergedRegion(region);
						}
						
						//复制图片
						for (int i = 0; i < pictuteNums; i++) {
							HSSFShape shape = shapeList.get(i);
							if (shape instanceof HSSFPicture) {
								HSSFClientAnchor anchor = (HSSFClientAnchor)shape.getAnchor();
								HSSFPicture picture = (HSSFPicture)shape;
								
								int dx1 = anchor.getDx1();
								int dy1 = anchor.getDy1();
								int dx2 = anchor.getDx2();
								int dy2 = anchor.getDy2();
								int row1 = anchor.getRow1();
								short col1 = anchor.getCol1();
								int row2 = anchor.getRow2();
								short col2 = anchor.getCol2();
								
								anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum+margin, col2, row2+lastRowNum+margin);
								anchor.setAnchorType(2);
								
								patriarch = sheet.createDrawingPatriarch();
								patriarch.createPicture(anchor,work.addPicture(picture.getPictureData().getData(), HSSFWorkbook.PICTURE_TYPE_PNG));
								
							}
						}
						
					}
				}
				
				for(int i = 0;i < length; i++){
					MaterialEntity entity = listBeans.get(i);
					
					int iRow = i/47 * 13 + 6 + i;
					row = sheet.getRow(iRow);
					
					row.getCell(0).setCellValue(i+1);//序号
					
					row.getCell(1).setCellValue(entity.getSorc_no());//Order No.
					
					row.getCell(2).setCellValue(CodeListUtils.getValue("material_ocm", entity.getOcm()+""));//委托处（维修站）
					
					row.getCell(3).setCellValue(entity.getModel_name());//型号
					
					row.getCell(4).setCellValue(entity.getSerial_no());//机身号
					
					row.getCell(5).setCellValue("√");//确认结果
					
					//确认者
//					String operator_name = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\DE100096";
//					insertImage(work,sheet,6,iRow,operator_name);
					
					row.getCell(7).setCellValue(entity.getWip_location());//确认结果
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
	
	
	/**
	 * 先端回收记录表
	 * @param today
	 * @param conn
	 */
	private void advancedRecoveryRecord(Calendar curMon,SqlSession conn) {
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		Date minFinishTime = dao.getMinFinishTime(DateUtil.toString(curMon.getTime(), DateUtil.ISO_DATE_PATTERN));
		
		if(minFinishTime==null) return;
		
		Calendar monthStart = Calendar.getInstance();
		monthStart.setTime(minFinishTime);
		monthStart.set(Calendar.DATE, 1);
		monthStart.set(Calendar.HOUR_OF_DAY, 0);
		monthStart.set(Calendar.MINUTE, 0);
		monthStart.set(Calendar.SECOND, 0);
		monthStart.set(Calendar.MILLISECOND, 0);
		
		//月份集
		List<String> monthList = new ArrayList<String>();
		while(monthStart.compareTo(curMon) <= 0){
			monthList.add(DateUtil.toString(monthStart.getTime(), DateUtil.ISO_DATE_PATTERN));
			monthStart.add(Calendar.MONTH, 1);
		}
		
		//模板文件
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\QR-B31002-55 先端回收记录表.xls";
		
		MaterialService service = new MaterialService();
		
		//按月生成先端回收记录表
		for(int num = 0;num < monthList.size(); num++){
			String strDate = monthList.get(num);
			String cacheFilename = "QR-B31002-55 先端回收记录表-" + strDate.substring(0,strDate.length()-3) + ".xls";//生成文件的文件名称
			String cachePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\snout\\"+ cacheFilename;
			
			InputStream in = null;
			OutputStream out = null;
			
			List<MaterialEntity> listBeans = service.searchAdvancedRecovery(strDate,conn);
			int length = listBeans.size();//数据长度

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
					
					int pageNum = 0;
					int margin = 0;//每页间隔
					
					//设置分页区域
					sheet.setRowBreak(36);
					sheet.setColumnBreak(8);
					
					if(length > 26){
						//需要复制的页数(不包含第一页)
						pageNum=(length-26)%26==0 ? (length-26)/26 :(length-26)/26+1;
						
						//当前sheet合并单元格总数
						int sheetMergerCount =  sheet.getNumMergedRegions();
						
						//图形
						HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
						List<HSSFShape> shapeList = patriarch.getChildren();
						int pictuteNums = shapeList.size();
						
						//生成行
						for(int j = 0;j < pageNum; j++){
							HSSFRow fromRow = null;
							HSSFRow toRow = null;
							
							int lastRowNum = sheet.getLastRowNum()+1;//最后一行索引+1
							
							//复制普通单元格
							for(int index = 0;index <= 36; index++){
								int dex = lastRowNum + index + margin;
								fromRow = sheet.getRow(index);
								toRow = sheet.createRow(dex);
								CopyByPoi.copyRow(fromRow, toRow,  work,true);
							}
							
							//复制合并单元格
							for (int i = 0; i < sheetMergerCount; i++) {  
								CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
								int firstRow = cellRangeAddress.getFirstRow();
								int lastRow = cellRangeAddress.getLastRow();
								int firstCol = cellRangeAddress.getFirstColumn();
								int lastCol = cellRangeAddress.getLastColumn();
								
								CellRangeAddress  region = new CellRangeAddress(firstRow +lastRowNum+margin, lastRow+lastRowNum+margin, firstCol, lastCol);
								sheet.addMergedRegion(region);
							}
							
							//复制图行
							for (int i = 0; i < pictuteNums; i++) {
								HSSFShape shape = shapeList.get(i);
								HSSFClientAnchor anchor = (HSSFClientAnchor)shape.getAnchor();
								if (shape instanceof HSSFPicture) {	//图片
									HSSFPicture picture = (HSSFPicture)shape;
									
									int dx1 = anchor.getDx1();
									int dy1 = anchor.getDy1();
									int dx2 = anchor.getDx2();
									int dy2 = anchor.getDy2();
									int row1 = anchor.getRow1();
									short col1 = anchor.getCol1();
									int row2 = anchor.getRow2();
									short col2 = anchor.getCol2();
									anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum+margin, col2, row2+lastRowNum+margin);
									
									patriarch = sheet.createDrawingPatriarch();
									patriarch.createPicture(anchor,work.addPicture(picture.getPictureData().getData(), HSSFWorkbook.PICTURE_TYPE_PNG));
								}else if(shape instanceof HSSFTextbox){	//文本框
									HSSFTextbox textBox = (HSSFTextbox)shape;
									HSSFRichTextString str =  textBox.getString();//文字
									int lineStyle = textBox.getLineStyle();
									
									int dx1 = textBox.getAnchor().getDx1();
									int dy1 = textBox.getAnchor().getDy1();
									int dx2 = textBox.getAnchor().getDx2();
									int dy2 = textBox.getAnchor().getDy2();
									int row1 = anchor.getRow1();
									short col1 = anchor.getCol1();
									int row2 = anchor.getRow2();
									short col2 = anchor.getCol2();
									anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum+margin, col2, row2+lastRowNum+margin);
									
									textBox = patriarch.createTextbox(anchor);
									textBox.setLineStyle(lineStyle);
									textBox.setNoFill(true);
									textBox.setString(str);
								}else if(shape instanceof HSSFSimpleShape){//简单图形
									HSSFSimpleShape simpleShape = (HSSFSimpleShape)shape;
									int shapeType = simpleShape.getShapeType();
									
									int dx1 = simpleShape.getAnchor().getDx1();
									int dy1 = simpleShape.getAnchor().getDy1();
									int dx2 = simpleShape.getAnchor().getDx2();
									int dy2 = simpleShape.getAnchor().getDy2();
									int row1 = anchor.getRow1();
									short col1 = anchor.getCol1();
									int row2 = anchor.getRow2();
									short col2 = anchor.getCol2();
									anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum+margin, col2, row2+lastRowNum+margin);
									
									simpleShape = patriarch.createSimpleShape(anchor);
									simpleShape.setShapeType(shapeType);
								}
							}
							
							sheet.setRowBreak(sheet.getLastRowNum());
						}
					}
					
					//重新设置打印区域
					work.setPrintArea(0, 0, 8, 0, sheet.getLastRowNum());
					
					
					//往Excel填充数据
					for(int i = 0;i < length; i++){
						MaterialEntity entity = listBeans.get(i);
						
						int iRow = i/26 * 11 + 6 + i;
						HSSFRow  row = sheet.getRow(iRow);
						
						row.getCell(0).setCellValue(DateUtil.toString(entity.getFinish_time(),DateUtil.ISO_DATE_PATTERN));//日期
						
						row.getCell(1).setCellValue(entity.getModel_name());//型号
						
						row.getCell(2).setCellValue(entity.getSerial_no());//机身号
						
						row.getCell(3).setCellValue(entity.getRepair_notifi_no());//修理编号
						
						row.getCell(4).setCellValue(entity.getManage_serial_no());//管理编号

						row.getCell(7).setCellType(HSSFCell.CELL_TYPE_STRING);
						row.getCell(7).setCellValue(entity.getOmr_notifi_no());//用于内镜SORC NO.
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
	}
	
	/**
	  * 插入图片
	  * @param work Excel表格
	  * @param sheet 
	  * @param iCol 列位子
	  * @param iLine 行位子
	  * @param fileName 图片名称
	  */
	 public void insertImage(HSSFWorkbook work,HSSFSheet sheet,int iCol,int iLine,String fileName){
	  try{
	   BufferedImage bufferImg=ImageIO.read(new File(fileName));
	   ByteArrayOutputStream byteArrayOut=new ByteArrayOutputStream();
	   ImageIO.write(bufferImg, "jpg",byteArrayOut);
	   HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
	   if (patriarch == null) {
	    patriarch = sheet.createDrawingPatriarch();
	   }
	   HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255, (short) iCol, iLine, (short) iCol, iLine);
	   anchor.setAnchorType(2);
	   patriarch.createPicture(anchor,work.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG)).resize(1);
	  }catch(Exception e){
	   _log.error("图片文件不存在"+fileName, e);
	  }
	 }

	public static DailyWorkSheetsJob getInstance() {
		return instance;
	}

	public void tempMake(String position_work) {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		if ("accept".equals(position_work)) {
			this.acceptAndDisinfectAndSterilize(today, conn);
		} else if ("inline".equals(position_work)) {
			this.inline(today, conn);
		} else if ("shipping".equals(position_work)) {
			this.shipping(today, conn);
		}
		
		
	}
}
