package com.osh.rvs.service.partial;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.PostMessageEntity;
import com.osh.rvs.bean.partial.MaterialPartialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.partial.MaterialPartialForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.data.PostMessageMapper;
import com.osh.rvs.mapper.partial.MaterialPartialMapper;
import com.osh.rvs.service.PostMessageService;
import com.osh.rvs.service.inline.ForSolutionAreaService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;


public class MaterialPartialService {

	private static Logger _log = Logger.getLogger("MaterialPartialService");

	/**
	 * 取得维修对象零件订购信息
	 * @param conn
	 * @param id
	 * @param times
	 * @return
	 */
	public MaterialPartialForm loadMaterialPartial(SqlSession conn, String id, Integer times) {

		MaterialPartialEntity conditionBean = new MaterialPartialEntity();
		conditionBean.setMaterial_id(id);
		conditionBean.setOccur_times(times);
		
		MaterialPartialForm form = null;

		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		MaterialPartialEntity entity = null;
		if (times == null) {
			entity = dao.loadMaterialPartialGroup(conditionBean);
		} else {
			entity = dao.loadMaterialPartial(conditionBean);
		}

		if (entity != null && entity.getMaterial_id() != null) {
			form = new MaterialPartialForm();
			BeanUtil.copyToForm(entity, form, null);
		}

		return form;
	}
	
	public void updateMaterialPartial(ActionForm form, LoginData logindata, SqlSessionManager conn) throws Exception {
		//复制表单数据到对象
		MaterialPartialEntity conditionBean = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, conditionBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		ForSolutionAreaService fsoService = new ForSolutionAreaService();

		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);

		// 取得原始BO状态
		MaterialPartialEntity oldEntity = dao.loadMaterialPartial(conditionBean);
		Integer oldBoFlg = oldEntity.getBo_flg();

		Integer bo_flg = conditionBean.getBo_flg();
		//如果订单状态选择“无BO”和“BO解决”缺品发生工位、零件缺品 则为空
		if(bo_flg == 0 || bo_flg == 2){
			conditionBean.setBo_position(null);
			conditionBean.setBo_contents(null);
			if (oldBoFlg == 1) {
				fsoService.doBoFsaLeave(conditionBean.getMaterial_id(), logindata, conn);
			}
		}
		if(bo_flg == 1){
			if (oldBoFlg != 1) {
				fsoService.doBoFsaEnter(conditionBean.getMaterial_id(), "编辑为BO", conn);
			}
		}

		if(bo_flg == 7){
			conditionBean.setOrder_date(null);
			conditionBean.setArrival_date(null);
			conditionBean.setArrival_plan_date(null);
		}
		
		dao.updateMaterialPartial(conditionBean);

	}

	/**
	 * 建立维修对象零件订购信息
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void createMaterialPartialAtOrderPosition(ActionForm form, SqlSession conn) throws Exception {
		MaterialPartialEntity insertBean = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, insertBean, null);
		
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		dao.createMaterialPartialAtOrderPosition(insertBean);
	}
	
	public List<MaterialPartialForm> searchMaterial(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		MaterialPartialEntity conditionBean = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

//		// 现品人员显示
//		conditionBean.setType_of_bo_item(fact);
		// 从数据库中查询记录
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		List<MaterialPartialEntity> lResultBean = dao.searchMaterial(conditionBean);

		// 建立页面返回表单
		List<MaterialPartialForm> lResultForm = new ArrayList<MaterialPartialForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, MaterialPartialForm.class);

		return lResultForm;
	}
	public List<MaterialPartialForm> searchMaterialReport(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		MaterialPartialEntity conditionBean = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		// 从数据库中查询记录
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		List<MaterialPartialEntity> lResultBean = dao.searchMaterialReport(conditionBean);
		
		// 建立页面返回表单
		List<MaterialPartialForm> lResultForm = new ArrayList<MaterialPartialForm>();
		
		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, MaterialPartialForm.class);
		
		return lResultForm;
	}

	/**
	 * 更新入库预定日
	 * @param sSorc_no
	 * @param arrival_plan_date
	 * @param conn
	 * @throws Exception 
	 */
	public void updateReachDateBySorc(String sSorc_no, Date arrival_plan_date, SqlSession conn) throws Exception {
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("sorc_no", sSorc_no);
		paramMap.put("arrival_plan_date", arrival_plan_date);
		dao.updateReachDateBySorc(paramMap);
		
	}

	/**
	 * 计算时段内BO率
	 * @param from
	 * @param to
	 * @param conn
	 * @return
	 */
	public String[] getBoRate(Date from, Date to, SqlSession conn) {
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);

		Double todayBoRate = null;
		Double threedaysBoRate = null;

		// 如果条件有订购开始时期则按输入的查询
		if (from != null) {
			todayBoRate = dao.getTodayBoRate(from, to);
			threedaysBoRate = dao.get3daysBoRate(from, to);
		} else {
			
			// Integer totalBo = dao.getTotalBo();
			Date date[] = getWeekFromAndEnd();

			todayBoRate = dao.getTodayBoRate(date[0], date[1]);
			threedaysBoRate = dao.get3daysBoRate(date[0], date[1]);
		}

		String[] rate = new String[2];
		BigDecimal perty = new BigDecimal(100);
		rate[0] = (todayBoRate == null) ? "-" : new BigDecimal(todayBoRate).multiply(perty).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
		rate[1] = (threedaysBoRate == null) ? "-" : new BigDecimal(threedaysBoRate).multiply(perty).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
		
		return rate;
	}
	
	private Date[] getWeekFromAndEnd(){
		// Integer totalBo = dao.getTotalBo();
		Calendar thusday = Calendar.getInstance();
		Date endDate = null;
		int theday = thusday.get(Calendar.DAY_OF_WEEK);
		if (theday == Calendar.FRIDAY) { // 周五看上周信息
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(thusday.getTimeInMillis());
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);
			endDate = now.getTime();
		}
		int diff = theday % 7 + 1;
		thusday.set(Calendar.HOUR_OF_DAY, 0);
		thusday.set(Calendar.MINUTE, 0);
		thusday.set(Calendar.SECOND, 0);
		thusday.set(Calendar.MILLISECOND, 0);
		thusday.add(Calendar.DATE, -diff);
		
		Date date[] = {thusday.getTime(),endDate};
		
		return date;
		
	}
	
	/**
	 * *BO缺品零件表生成
	 * @param tempPath
	 * @param lResultForm
	 * @param othesResultForm
	 * @return
	 */
	public String makeBoFile(String tempPath, List<MaterialPartialForm> lResultForm, List<MaterialPartialForm> othesResultForm,List<MaterialPartialForm> fixLightForm) {
		try {
			File tempExl = new File(tempPath);
			FileInputStream ins = new FileInputStream(tempExl);
			HSSFWorkbook workbook = new HSSFWorkbook(ins);
			
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFSheet sheet2 = workbook.getSheetAt(1);
			HSSFSheet sheet3 = workbook.getSheetAt(2);
			
			int rowIndex = 1;
			int rowIndex2 = 1;
			int rowIndex3 = 1;
			String orderDate = "";

			HSSFCellStyle style = workbook.createCellStyle();
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			
			HSSFCellStyle styleAka = workbook.createCellStyle();
			styleAka.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderTop(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASHED);
			styleAka.setTopBorderColor(HSSFColor.RED.index);
			styleAka.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			HSSFPalette palette = workbook.getCustomPalette();
			palette.setColorAtIndex((short)60, (byte)(255), (byte)(255), (byte)(153));
			palette.setColorAtIndex((short)61, (byte)(255), (byte)(204), (byte)(0));
			palette.setColorAtIndex((short)62, (byte)(153), (byte)(204), (byte)(255));
			palette.setColorAtIndex((short)63, (byte)(153), (byte)(204), (byte)(0));

			HSSFCellStyle[] styles = new HSSFCellStyle[6];
			HSSFCellStyle[] stylesAka = new HSSFCellStyle[6];

			styles[0] = workbook.createCellStyle();
			styles[0].cloneStyleFrom(style);
			styles[0].setAlignment(HSSFCellStyle.ALIGN_LEFT);

			stylesAka[0] = workbook.createCellStyle();
			stylesAka[0].cloneStyleFrom(styleAka);
			stylesAka[0].setAlignment(HSSFCellStyle.ALIGN_LEFT);

			styles[1] = workbook.createCellStyle();
			styles[1].cloneStyleFrom(styles[0]);
			styles[1].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			styles[1].setFillForegroundColor((short)60);

			stylesAka[1] = workbook.createCellStyle();
			stylesAka[1].cloneStyleFrom(stylesAka[0]);
			stylesAka[1].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			stylesAka[1].setFillForegroundColor((short)60);

			styles[2] = workbook.createCellStyle();
			styles[2].cloneStyleFrom(styles[1]);
			styles[2].setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styles[2].setFillForegroundColor((short)61);

			stylesAka[2] = workbook.createCellStyle();
			stylesAka[2].cloneStyleFrom(stylesAka[1]);
			stylesAka[2].setAlignment(HSSFCellStyle.ALIGN_CENTER);
			stylesAka[2].setFillForegroundColor((short)61);

			styles[3] = workbook.createCellStyle();
			styles[3].cloneStyleFrom(styles[1]);
			styles[3].setAlignment(HSSFCellStyle.ALIGN_CENTER);

			stylesAka[3] = workbook.createCellStyle();
			stylesAka[3].cloneStyleFrom(stylesAka[1]);
			stylesAka[3].setAlignment(HSSFCellStyle.ALIGN_CENTER);

			styles[4] = workbook.createCellStyle();
			styles[4].cloneStyleFrom(styles[1]);
			styles[4].setFillForegroundColor((short)62);

			stylesAka[4] = workbook.createCellStyle();
			stylesAka[4].cloneStyleFrom(stylesAka[1]);
			stylesAka[4].setFillForegroundColor((short)62);

			styles[5] = workbook.createCellStyle();
			styles[5].cloneStyleFrom(styles[2]);
			styles[5].setFillForegroundColor((short)63);

			stylesAka[5] = workbook.createCellStyle();
			stylesAka[5].cloneStyleFrom(stylesAka[2]);
			stylesAka[5].setFillForegroundColor((short)63);

			for (MaterialPartialForm partialForm : lResultForm) {
				
				
				HSSFRow row = sheet.createRow(rowIndex);
				HSSFCell cellOrderDate = row.createCell(0);
				HSSFCell cellOrderNo = row.createCell(1);
				HSSFCell cellRandAndModel = row.createCell(2);
				HSSFCell cellTier = row.createCell(3);
				HSSFCell cellBoItem = row.createCell(4);
				HSSFCell cellTypeOfBoItem = row.createCell(5);
				HSSFCell cellLtIn3days = row.createCell(6);
				HSSFCell cellRemarks = row.createCell(7);
				HSSFCell cellSafety = row.createCell(8);
				
				boolean flag = !partialForm.getOrder_date().equals(orderDate);
				
				cellOrderDate.setCellValue(partialForm.getOrder_date());
				if ("1".equals(partialForm.getOccur_times())) {
					cellOrderNo.setCellValue(partialForm.getSorc_no());
				} else {
					cellOrderNo.setCellValue(partialForm.getSorc_no().concat("/").concat(partialForm.getOccur_times()));
				}
				cellRandAndModel.setCellValue(partialForm.getLevelName().concat(partialForm.getModel_name()));
				String echelon = partialForm.getEchelon();
				cellTier.setCellValue("1".equals(echelon) ? "1st Tier" : "2".equals(echelon) ? "2nd Tier" : "3".equals(echelon) ? "3rd Tier" : "4".equals(echelon) ? "4th Tier" : "#N/A");
				cellBoItem.setCellValue(partialForm.getBo_item());
				cellTypeOfBoItem.setCellValue("1".equals(partialForm.getType_of_bo_item()) ? "BOM" : "Non-Bom");
				cellLtIn3days.setCellValue("1".equals(partialForm.getBo_within_3days()) ? "√" : "×");
				cellRemarks.setCellValue("");
				cellSafety.setCellValue(partialForm.getSafety() == null || "".equals(partialForm.getSafety()) ? 0 : Integer.parseInt(partialForm.getSafety()));
				
				if (flag) {//日期不等
//					num = 1;
//					cellNo.setCellValue(num);
					orderDate = partialForm.getOrder_date();
				}

				HSSFCellStyle[] theStyles = null;
				//设置对齐和颜色样式
				if (flag) {//日期不等
					cellOrderDate.setCellStyle(styleAka);
					theStyles = stylesAka;
				} else {
					cellOrderDate.setCellStyle(style);
					theStyles = styles;
				}

				cellOrderNo.setCellStyle(theStyles[0]);
				
				cellRandAndModel.setCellStyle(theStyles[1]);
				
				cellTier.setCellStyle(theStyles[2]);
				
				cellBoItem.setCellStyle(theStyles[3]);
				cellSafety.setCellStyle(theStyles[3]);

				cellTypeOfBoItem.setCellStyle(theStyles[4]);
				
				cellLtIn3days.setCellStyle(theStyles[5]);
				cellRemarks.setCellStyle(theStyles[5]);
				
				rowIndex ++;
			}
			
			for (MaterialPartialForm partialForm : othesResultForm) {
				
				
				HSSFRow row = sheet2.createRow(rowIndex2);
				HSSFCell cellOrderDate = row.createCell(0);
				HSSFCell cellOrderNo = row.createCell(1);
				HSSFCell cellRandAndModel = row.createCell(2);
				HSSFCell cellTier = row.createCell(3);
				HSSFCell cellBoItem = row.createCell(4);
				HSSFCell cellTypeOfBoItem = row.createCell(5);
				HSSFCell cellLtIn3days = row.createCell(6);
				HSSFCell cellRemarks = row.createCell(7);
				HSSFCell cellSafety = row.createCell(8);
				
				boolean flag = !partialForm.getOrder_date().equals(orderDate);
				
				cellOrderDate.setCellValue(partialForm.getOrder_date());
				if ("1".equals(partialForm.getOccur_times())) {
					cellOrderNo.setCellValue(partialForm.getSorc_no());
				} else {
					cellOrderNo.setCellValue(partialForm.getSorc_no().concat("/").concat(partialForm.getOccur_times()));
				}
				cellRandAndModel.setCellValue(partialForm.getLevelName().concat(partialForm.getModel_name()));
				String echelon = partialForm.getEchelon();
				cellTier.setCellValue("1".equals(echelon) ? "1st Tier" : "2".equals(echelon) ? "2nd Tier" : "3".equals(echelon) ? "3rd Tier" : "4".equals(echelon) ? "4th Tier" : "#N/A");
				cellBoItem.setCellValue(partialForm.getBo_item());
				cellTypeOfBoItem.setCellValue("1".equals(partialForm.getType_of_bo_item()) ? "BOM" : "Non-Bom");
				cellLtIn3days.setCellValue("1".equals(partialForm.getBo_within_3days()) ? "√" : "×");
				cellRemarks.setCellValue("");
				cellSafety.setCellValue(partialForm.getSafety() == null || "".equals(partialForm.getSafety()) ? 0 : Integer.parseInt(partialForm.getSafety()));
				
				if (flag) {//日期不等
//					num = 1;
//					cellNo.setCellValue(num);
					orderDate = partialForm.getOrder_date();
				}

				HSSFCellStyle[] theStyles = null;
				//设置对齐和颜色样式
				if (flag) {//日期不等
					cellOrderDate.setCellStyle(styleAka);
					theStyles = stylesAka;
				} else {
					cellOrderDate.setCellStyle(style);
					theStyles = styles;
				}

				cellOrderNo.setCellStyle(theStyles[0]);
				
				cellRandAndModel.setCellStyle(theStyles[1]);
				
				cellTier.setCellStyle(theStyles[2]);
				
				cellBoItem.setCellStyle(theStyles[3]);
				cellSafety.setCellStyle(theStyles[3]);

				cellTypeOfBoItem.setCellStyle(theStyles[4]);
				
				cellLtIn3days.setCellStyle(theStyles[5]);
				cellRemarks.setCellStyle(theStyles[5]);
				
				rowIndex2 ++;
			}
			
			for (MaterialPartialForm partialForm : fixLightForm) {
				
				HSSFRow row = sheet3.createRow(rowIndex3);
				HSSFCell cellOrderDate = row.createCell(0);
				HSSFCell cellOrderNo = row.createCell(1);
				HSSFCell cellRandAndModel = row.createCell(2);
				HSSFCell cellTier = row.createCell(3);
				HSSFCell cellBoItem = row.createCell(4);
				HSSFCell cellTypeOfBoItem = row.createCell(5);
				HSSFCell cellLtIn3days = row.createCell(6);
				HSSFCell cellRemarks = row.createCell(7);
				HSSFCell cellSafety = row.createCell(8);
				
				boolean flag = !partialForm.getOrder_date().equals(orderDate);
				
				cellOrderDate.setCellValue(partialForm.getOrder_date());
				if ("1".equals(partialForm.getOccur_times())) {
					cellOrderNo.setCellValue(partialForm.getSorc_no());
				} else {
					cellOrderNo.setCellValue(partialForm.getSorc_no().concat("/").concat(partialForm.getOccur_times()));
				}
				cellRandAndModel.setCellValue(partialForm.getLevelName().concat(partialForm.getModel_name()));
				String echelon = partialForm.getEchelon();
				cellTier.setCellValue("1".equals(echelon) ? "1st Tier" : "2".equals(echelon) ? "2nd Tier" : "3".equals(echelon) ? "3rd Tier" : "4".equals(echelon) ? "4th Tier" : "#N/A");
				cellBoItem.setCellValue(partialForm.getBo_item());
				cellTypeOfBoItem.setCellValue("1".equals(partialForm.getType_of_bo_item()) ? "BOM" : "Non-Bom");
				cellLtIn3days.setCellValue("1".equals(partialForm.getBo_within_3days()) ? "√" : "×");
				cellRemarks.setCellValue("");
				cellSafety.setCellValue(partialForm.getSafety() == null || "".equals(partialForm.getSafety()) ? 0 : Integer.parseInt(partialForm.getSafety()));
				
				if (flag) {//日期不等
//					num = 1;
//					cellNo.setCellValue(num);
					orderDate = partialForm.getOrder_date();
				}

				HSSFCellStyle[] theStyles = null;
				//设置对齐和颜色样式
				if (flag) {//日期不等
					cellOrderDate.setCellStyle(styleAka);
					theStyles = stylesAka;
				} else {
					cellOrderDate.setCellStyle(style);
					theStyles = styles;
				}

				cellOrderNo.setCellStyle(theStyles[0]);
				
				cellRandAndModel.setCellStyle(theStyles[1]);
				
				cellTier.setCellStyle(theStyles[2]);
				
				cellBoItem.setCellStyle(theStyles[3]);
				cellSafety.setCellStyle(theStyles[3]);

				cellTypeOfBoItem.setCellStyle(theStyles[4]);
				
				cellLtIn3days.setCellStyle(theStyles[5]);
				cellRemarks.setCellStyle(theStyles[5]);
				
				rowIndex3 ++;
			}
			
			Date today = new Date();
			String folder = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
			File fMonthPath = new File(folder);
			if (!fMonthPath.exists()) {
				fMonthPath.mkdirs();
			}
			String outputFile = new Date().getTime() + ".xls";
			
			FileOutputStream fileOut = new FileOutputStream(folder + "\\" + outputFile);   
			workbook.write(fileOut);   
			fileOut.close(); 

			return outputFile;
		}catch (Exception e) {
			_log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 维修对象订购一览表导出
	 * @param tempPath
	 * @param lResultForm
	 * @param lResultItemForm
	 * @return
	 */
	public String makeOrderFile(String tempPath, List<MaterialPartialForm> lResultForm,List<MaterialPartialForm> lResultItemForm) {
		FileOutputStream fileOut = null;

		try {
			File tempExl = new File(tempPath);
			FileInputStream ins = new FileInputStream(tempExl);
			HSSFWorkbook workbook = new HSSFWorkbook(ins);
			
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			int num = 1;
			int rowIndex = 1;
			String orderDate = "";
			float totalTier = 0;
			Map<String, Integer> tiers = new HashMap<String, Integer>();
			
			HSSFCellStyle style = workbook.createCellStyle();
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);

			HSSFCellStyle styleAka = workbook.createCellStyle();
			styleAka.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderTop(HSSFCellStyle.BORDER_THIN);
			styleAka.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAka.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAka.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
			styleAka.setTopBorderColor(HSSFColor.RED.index);

			HSSFCellStyle style2 = workbook.createCellStyle();
			style2.cloneStyleFrom(style);
			//居中
			style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			HSSFCellStyle styleAka2 = workbook.createCellStyle();
			styleAka2.cloneStyleFrom(styleAka);
			//居中
			styleAka2.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			for (MaterialPartialForm partialForm : lResultForm) {
				
				
				HSSFRow row = sheet.createRow(rowIndex);
				HSSFCell cellNo = row.createCell(0);
				HSSFCell cellOrderDate = row.createCell(1);
				HSSFCell cellOrderNo = row.createCell(2);
				HSSFCell cellModel = row.createCell(3);
				HSSFCell cellRank = row.createCell(4);
				HSSFCell cellRandAndModel = row.createCell(5);
				HSSFCell cellTier = row.createCell(6);
				HSSFCell cellQty = row.createCell(7);
				HSSFCell cellBo = row.createCell(8);
				HSSFCell cellBoIn3days = row.createCell(9);

				boolean flag = !partialForm.getOrder_date().equals(orderDate);
				
				cellNo.setCellValue(num);
				cellOrderDate.setCellValue(partialForm.getOrder_date());
				if ("1".equals(partialForm.getOccur_times())) {
					cellOrderNo.setCellValue(partialForm.getSorc_no());
				} else {
					cellOrderNo.setCellValue(partialForm.getSorc_no().concat("/").concat(partialForm.getOccur_times()));
				}
				cellModel.setCellValue(partialForm.getModel_name());
				cellRank.setCellValue(partialForm.getLevelName());
				cellRandAndModel.setCellValue(partialForm.getLevelName().concat(partialForm.getModel_name()));
				String echelon = partialForm.getEchelon();
				if (echelon != null && !"".equals(echelon)) {
					totalTier+=1;
					if (tiers.containsKey(echelon)) {
						int value = tiers.get(echelon).intValue()+1;
						tiers.put(echelon, value);
					} else {
						tiers.put(echelon, 1);
					}
				}
				cellTier.setCellValue(CodeListUtils.getValue("echelon_report_code", partialForm.getEchelon()));
				boolean boFlag = "1".equals(partialForm.getBo_flg()) || "2".equals(partialForm.getBo_flg());
				cellBo.setCellValue(boFlag ? "√" : "");
				cellBoIn3days.setCellValue(boFlag && "1".equals(partialForm.getBo_within_3days()) ? "√" : boFlag ? "×" : "");

				if (flag) {//日期不等
					orderDate = partialForm.getOrder_date();
					num = 1;
					cellNo.setCellValue(num);

					cellOrderNo.setCellStyle(styleAka);
					cellModel.setCellStyle(styleAka);
					cellRandAndModel.setCellStyle(styleAka);
					cellTier.setCellStyle(styleAka);

					cellNo.setCellStyle(styleAka2);
					cellOrderDate.setCellStyle(styleAka2);
					cellRank.setCellStyle(styleAka2);
					cellQty.setCellStyle(styleAka2);
					cellBo.setCellStyle(styleAka2);
					cellBoIn3days.setCellStyle(styleAka2);
				} else {
					
					//左对齐
					cellOrderNo.setCellStyle(style);
					cellModel.setCellStyle(style);
					cellRandAndModel.setCellStyle(style);
					cellTier.setCellStyle(style);
					
					cellNo.setCellStyle(style2);
					cellOrderDate.setCellStyle(style2);
					cellRank.setCellStyle(style2);
					cellQty.setCellStyle(style2);
					cellBo.setCellStyle(style2);
					cellBoIn3days.setCellStyle(style2);
				}
				
				num ++;
				rowIndex ++;
			}
			
			createTotalRow(sheet, rowIndex + 5, "计数项:Tier", "", "", "", tiers, totalTier, workbook);
			createTotalRow(sheet, rowIndex + 6, "Tier", "汇总", "订购比率", "", tiers, totalTier, workbook);
			createTotalRow(sheet, rowIndex + 7, "1st Tier", "", "", "1", tiers, totalTier,  workbook);
			createTotalRow(sheet, rowIndex + 8, "2nd Tier", "", "", "2", tiers, totalTier,  workbook);
			createTotalRow(sheet, rowIndex + 9, "3rd Tier", "", "", "3", tiers, totalTier,  workbook);
			createTotalRow(sheet, rowIndex + 10, "4th Tier", "", "", "4", tiers, totalTier,  workbook);
			createTotalRow(sheet, rowIndex + 11, "总计", totalTier+"", "", "", tiers, totalTier,  workbook);
			
			
			HSSFSheet sheet2 = workbook.getSheetAt(1);
			HSSFPalette palette = workbook.getCustomPalette();
			palette.setColorAtIndex((short)60, (byte)(255), (byte)(255), (byte)(153));
			palette.setColorAtIndex((short)61, (byte)(255), (byte)(204), (byte)(0));
			palette.setColorAtIndex((short)62, (byte)(153), (byte)(204), (byte)(255));
			palette.setColorAtIndex((short)63, (byte)(153), (byte)(204), (byte)(0));

			HSSFCellStyle[] styles = new HSSFCellStyle[8];
			HSSFCellStyle[] stylesAka = new HSSFCellStyle[8];

			styles[0] = workbook.createCellStyle();
			styles[0].cloneStyleFrom(style);
			styles[0].setAlignment(HSSFCellStyle.ALIGN_LEFT);

			stylesAka[0] = workbook.createCellStyle();
			stylesAka[0].cloneStyleFrom(styleAka);
			stylesAka[0].setAlignment(HSSFCellStyle.ALIGN_LEFT);

			styles[1] = workbook.createCellStyle();
			styles[1].cloneStyleFrom(styles[0]);
			styles[1].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			styles[1].setFillForegroundColor((short)60);

			stylesAka[1] = workbook.createCellStyle();
			stylesAka[1].cloneStyleFrom(stylesAka[0]);
			stylesAka[1].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			stylesAka[1].setFillForegroundColor((short)60);

			styles[2] = workbook.createCellStyle();
			styles[2].cloneStyleFrom(styles[1]);
			styles[2].setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styles[2].setFillForegroundColor((short)61);

			stylesAka[2] = workbook.createCellStyle();
			stylesAka[2].cloneStyleFrom(stylesAka[1]);
			stylesAka[2].setAlignment(HSSFCellStyle.ALIGN_CENTER);
			stylesAka[2].setFillForegroundColor((short)61);

			styles[3] = workbook.createCellStyle();
			styles[3].cloneStyleFrom(styles[1]);
			styles[3].setAlignment(HSSFCellStyle.ALIGN_CENTER);

			stylesAka[3] = workbook.createCellStyle();
			stylesAka[3].cloneStyleFrom(stylesAka[1]);
			stylesAka[3].setAlignment(HSSFCellStyle.ALIGN_CENTER);

			styles[4] = workbook.createCellStyle();
			styles[4].cloneStyleFrom(styles[1]);
			styles[4].setFillForegroundColor((short)62);

			stylesAka[4] = workbook.createCellStyle();
			stylesAka[4].cloneStyleFrom(stylesAka[1]);
			stylesAka[4].setFillForegroundColor((short)62);

			styles[5] = workbook.createCellStyle();
			styles[5].cloneStyleFrom(styles[2]);
			styles[5].setFillForegroundColor((short)63);

			stylesAka[5] = workbook.createCellStyle();
			stylesAka[5].cloneStyleFrom(stylesAka[2]);
			stylesAka[5].setFillForegroundColor((short)63);

			styles[6] = workbook.createCellStyle();
			styles[6].cloneStyleFrom(styles[4]);
			styles[6].setAlignment(HSSFCellStyle.ALIGN_RIGHT);

			stylesAka[6] = workbook.createCellStyle();
			stylesAka[6].cloneStyleFrom(stylesAka[4]);
			stylesAka[6].setAlignment(HSSFCellStyle.ALIGN_RIGHT);

			styles[7] = workbook.createCellStyle();
			styles[7].cloneStyleFrom(styles[1]);
			styles[7].setAlignment(HSSFCellStyle.ALIGN_RIGHT);

			stylesAka[7] = workbook.createCellStyle();
			stylesAka[7].cloneStyleFrom(stylesAka[1]);
			stylesAka[7].setAlignment(HSSFCellStyle.ALIGN_RIGHT);

			rowIndex=1;
			for (MaterialPartialForm partialForm : lResultItemForm) {
				
				HSSFRow row = sheet2.createRow(rowIndex);
				HSSFCell cellOrderDate = row.createCell(0);
				HSSFCell cellOrderNo = row.createCell(1);
				HSSFCell cellRandAndModel = row.createCell(2);
				HSSFCell cellTier = row.createCell(3);
				HSSFCell cellBoItem = row.createCell(4);
				HSSFCell cellTypeOfBoItem = row.createCell(5);
				HSSFCell cellQty = row.createCell(6);
//				HSSFCell cellBo = row.createCell(6);
//				HSSFCell cellLTIn3days = row.createCell(7);
				HSSFCell cellRemarks = row.createCell(7);
				HSSFCell cellSafety = row.createCell(8);
				
				
				boolean flag = !partialForm.getOrder_date().equals(orderDate);
				
				cellOrderDate.setCellValue(partialForm.getOrder_date());
				if ("1".equals(partialForm.getOccur_times())) {
					cellOrderNo.setCellValue(partialForm.getSorc_no());
				} else {
					cellOrderNo.setCellValue(partialForm.getSorc_no().concat("/").concat(partialForm.getOccur_times()));
				}
				cellRandAndModel.setCellValue(partialForm.getLevelName().concat(partialForm.getModel_name()));
				String echelon = partialForm.getEchelon();
				cellTier.setCellValue(CodeListUtils.getValue("echelon_report_code", "" + echelon));
				cellBoItem.setCellValue(partialForm.getBo_item());
				cellTypeOfBoItem.setCellValue("1".equals(partialForm.getType_of_bo_item()) ? "BOM" : "Non-Bom");

				Integer qtyvalue = Integer.parseInt(partialForm.getQty());
				cellQty.setCellValue(qtyvalue);
//				boolean boFlag = "3".equals(partialForm.getBo_flg()) || "4".equals(partialForm.getBo_flg());
//				cellBo.setCellValue(boFlag ? "√" : "");
//				
//				cellLTIn3days.setCellValue(boFlag && "1".equals(partialForm.getBo_within_3days()) ? "√" : boFlag ? "×" : "");
				
				cellRemarks.setCellValue("");
				cellSafety.setCellValue(partialForm.getSafety() == null || "".equals(partialForm.getSafety()) ? 0 : Integer.parseInt(partialForm.getSafety()));
				
				if (flag) {//日期不等
					orderDate = partialForm.getOrder_date();
				}

				HSSFCellStyle[] theStyles = null;
				//设置对齐和颜色样式
				if (flag) {//日期不等
					cellOrderDate.setCellStyle(styleAka);
					theStyles = stylesAka;
				} else {
					cellOrderDate.setCellStyle(style);
					theStyles = styles;
				}

				cellOrderNo.setCellStyle(theStyles[0]);
				
				cellRandAndModel.setCellStyle(theStyles[1]);
				
				cellTier.setCellStyle(theStyles[2]);
				
				cellBoItem.setCellStyle(theStyles[3]);
				cellSafety.setCellStyle(theStyles[7]);

				cellTypeOfBoItem.setCellStyle(theStyles[4]);
				cellQty.setCellStyle(theStyles[6]);
//				cellBo.setCellStyle(theStyles[4]);
//				cellLTIn3days.setCellStyle(theStyles[4]);
				
				
				cellRemarks.setCellStyle(theStyles[5]);
				
				rowIndex ++;
			}

			fileOut = new FileOutputStream(tempPath);
			workbook.write(fileOut);   
			fileOut.close(); 
			
			return tempPath;
		}catch (Exception e) {
			_log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 零件追加明细表导出
	 * @param tempPath
	 * @param lResultForm
	 * @return
	 */
	public String makePartialAddtionalInfFile(String tempPath, List<MaterialPartialForm> lResultForm) {
		try {
			File tempExl = new File(tempPath);
			FileInputStream ins = new FileInputStream(tempExl);
			HSSFWorkbook workbook = new HSSFWorkbook(ins);
			
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			int num = 0;
			int rowIndex = 1;
			
			HSSFCellStyle style = workbook.createCellStyle();
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);

			HSSFCellStyle styleT = workbook.createCellStyle();
			styleT.cloneStyleFrom(style);

            HSSFDataFormat thdf= workbook.createDataFormat();
            styleT.setDataFormat(thdf.getFormat("@"));

			HSSFCellStyle style2 = workbook.createCellStyle();
			style2.cloneStyleFrom(style);

			//居中
			style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			HSSFCellStyle styleD = workbook.createCellStyle();
			styleD.cloneStyleFrom(style2);

            HSSFDataFormat dhdf= workbook.createDataFormat();
            styleD.setDataFormat(dhdf.getFormat("yyyy-m-d"));

			HSSFCellStyle styleN = workbook.createCellStyle();
			styleN.cloneStyleFrom(style);
			//居右
			styleN.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

			String sorc_no = "";
			
			for (MaterialPartialForm partialForm : lResultForm) {
				
				HSSFRow row = sheet.createRow(rowIndex);
				
				HSSFCell cellNo = row.createCell(0);
				HSSFCell cellOutlineDate = row.createCell(1);
				HSSFCell cellSorcNo = row.createCell(2);
				HSSFCell cellModelName = row.createCell(3);
				HSSFCell cellSerialNo = row.createCell(4);
				HSSFCell cellRank = row.createCell(5);
				HSSFCell cellBelongs = row.createCell(6);
				HSSFCell cellBad = row.createCell(7);			
				HSSFCell cellCode = row.createCell(8);
				HSSFCell cellQuantity = row.createCell(9);
				HSSFCell cellPrice = row.createCell(10);
				HSSFCell cellTotalPrice = row.createCell(11);
				HSSFCell cellComments = row.createCell(12);
				
				boolean flag = partialForm.getSorc_no().equals(sorc_no);
				//设置单元格的值
				cellNo.setCellValue(num);
				
				//SORC_NO不等
				if(!flag){
					num++;
					cellNo.setCellValue(num);
					sorc_no = partialForm.getSorc_no();
				}
				
				cellNo.setCellStyle(style2);
				cellOutlineDate.setCellStyle(styleD);
				cellSorcNo.setCellStyle(style);
				cellModelName.setCellStyle(style);
				cellSerialNo.setCellStyle(styleT);
				cellRank.setCellStyle(style2);
				cellBelongs.setCellStyle(style);
				cellBad.setCellStyle(style);
				cellCode.setCellStyle(style);
				cellQuantity.setCellStyle(style2);
				cellPrice.setCellStyle(styleN);
				cellTotalPrice.setCellStyle(styleN);
				cellComments.setCellStyle(style);
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				Date date= null;
				if (partialForm.getOutline_date_start() != null) {
					date = format.parse(partialForm.getOutline_date_start());
					cellOutlineDate.setCellValue(date);
				} else {
					cellOutlineDate.setCellValue("在线");
				}

				cellSorcNo.setCellValue(partialForm.getSorc_no());
				cellModelName.setCellValue(partialForm.getModel_name());
				cellSerialNo.setCellValue(partialForm.getSerial_no());
				cellRank.setCellValue(partialForm.getLevelName());
				cellBelongs.setCellValue(CodeListUtils.getValue("partial_append_belongs", partialForm.getBelongs()));
				cellBad.setCellValue("");
				cellCode.setCellValue(partialForm.getBo_item());

				cellQuantity.setCellValue(new BigInteger(partialForm.getQty()).intValue());
				String price = partialForm.getPrice();
				if (price == null) {
					cellPrice.setCellValue("-");
				} else {
					cellPrice.setCellValue(new BigDecimal(price).doubleValue());
				}

				String totalPrice = partialForm.getTotalPrice();
				if (totalPrice == null) {
					cellTotalPrice.setCellValue("-");
				} else {
					cellTotalPrice.setCellValue(new BigDecimal(totalPrice).doubleValue());
				}

				cellComments.setCellValue("");	
				
				rowIndex ++;
			}
			
			Date today = new Date();
			String folder = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
			File fMonthPath = new File(folder);
			if (!fMonthPath.exists()) {
				fMonthPath.mkdirs();
			}
			String outputFile = new Date().getTime() + ".xls";
			
			FileOutputStream fileOut = new FileOutputStream(folder + "\\" + outputFile);   
			workbook.write(fileOut);   
			fileOut.close(); 

			return outputFile;
		}catch (Exception e) {
			_log.error(e.getMessage(), e);
			return null;
		}
	}

	
	private void createTotalRow(HSSFSheet sheet, int rowIndex, String value1, String value2,
			String value3, String mapkey, Map<String, Integer> tiers, float totalTier,  HSSFWorkbook workbook) {
		HSSFRow row = sheet.createRow(rowIndex + 1);
		HSSFCell cellTier = row.createCell(6);
		HSSFCell cellTotal = row.createCell(7);
		HSSFCell cellRate = row.createCell(8);
		
		cellTier.setCellValue(value1);
		cellTotal.setCellValue(value2);
		cellRate.setCellValue(value3);
		
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		cellTier.setCellStyle(style);
		cellTotal.setCellStyle(style);
		cellRate.setCellStyle(style);
		
		
		if (tiers.containsKey(mapkey)) {
			cellTotal.setCellValue(tiers.get(mapkey).intValue());
			
			HSSFCellStyle style2 = workbook.createCellStyle();
			style2.cloneStyleFrom(style);
			style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellTotal.setCellStyle(style2);
			
			
			String value = Integer.valueOf(Math.round((tiers.get(mapkey).intValue() / totalTier) * 100))
					.toString().concat("%");
			cellRate.setCellValue(value);
			
			HSSFCellStyle style3 = workbook.createCellStyle();
			style3.cloneStyleFrom(style);
			style3.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			cellRate.setCellStyle(style3);
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
			
			int def = sheet.getDefaultColumnWidth()*365;  
			
			int iCurWidth =sheet.getColumnWidth(iCol);
			sheet.setColumnWidth(iCol, def);
			
			HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255, (short) iCol, iLine, (short) iCol, iLine);
			anchor.setAnchorType(5);
			patriarch.createPicture(anchor,work.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG)).resize(1);
			sheet.setColumnWidth(iCol, iCurWidth);
		}catch(Exception e){
			e.printStackTrace();
			_log.info("图片"+fileName+"不存在!");
		}
	}

	/**
	 * 更新入库预订日
	 * @param req
	 * @param conn
	 * @throws Exception 
	 */
	public String updateMaterialPartial(HttpServletRequest req, LoginData logindata, SqlSessionManager conn) throws Exception {

		String content = "";

		// 发送人设定为登录用户
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		content = user.getName() + " 修改了" + req.getParameter("sorc_no") + "的\n" + content;
		if (content.length() > 180) {
//			longContent = content + "，请确认详细状况！";
			content = content.substring(0, 180) + "……，请确认详细状况！";
		} else {
			content += "，请确认详细状况！";
		}
		// 信息推送
		PostMessageMapper pmMapper = conn.getMapper(PostMessageMapper.class);
		PostMessageEntity pmEntity = new PostMessageEntity();
		pmEntity.setSender_id(user.getOperator_id());
		pmEntity.setContent(content);
		pmEntity.setLevel(1);
		pmEntity.setReason(PostMessageService.ARRIVAL_PLAN_DATE_CHANGED);

		pmMapper.createPostMessage(pmEntity);

		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
		String lastInsertID = commonMapper.getLastInsertID();
		pmEntity.setPost_message_id(lastInsertID);

		// TODO
		List<String> systemmers = new ArrayList<String>();
		systemmers.add("00000000129");
		systemmers.add("00000000010");
		systemmers.add("00000000011");
		systemmers.add("00000000065");
		systemmers.add("00000000070");
		systemmers.add("00000000063");

		for (String systemmer : systemmers) {
			if (systemmer.equals(user.getOperator_id())) continue;
			pmEntity.setReceiver_id(systemmer);
			pmMapper.createPostMessageSendation(pmEntity);
		}

		return null;
	}
	
	/**
	 *  取得所有没有投线，也没有建立过维修对象零件订购单的维修对象
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<MaterialPartialForm> searchNotOrderMaterail(ActionForm form,SqlSession conn){
		MaterialPartialEntity entity = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		List<MaterialPartialEntity> list = dao.searchNotOrderMaterail();
		
		List<MaterialPartialForm> respList = new ArrayList<MaterialPartialForm>();
		
		if(list!=null && list.size()>0){
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialPartialForm.class);
		}
		
		return respList;
	}
	
	/**
	 * 新建维修对象订购单
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void insertMaterialPartial(ActionForm form,SqlSessionManager conn)throws Exception{
		MaterialPartialEntity entity = new MaterialPartialEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		
		dao.insertMaterialPartial(entity);
	}
	
	/**
	 * 根据维修对象ID查询维修对象所有订购单
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public List<MaterialPartialForm> searchMaterialPartailById(String material_id,SqlSession conn){
		MaterialPartialMapper dao = conn.getMapper(MaterialPartialMapper.class);
		
		List<MaterialPartialEntity>  lResultBean = dao.searchMaterialPartialById(material_id);
		
		// 建立页面返回表单
		List<MaterialPartialForm> lResultForm = new ArrayList<MaterialPartialForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, MaterialPartialForm.class);

		return lResultForm;
	}
	
	
	public String reportMaterialPartial(HttpServletRequest req) throws Exception{
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "零件订购表一览.xls";
		String cacheName ="保零件订购表一览" + new Date().getTime() + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		
		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<MaterialPartialForm> list=(List)req.getSession().getAttribute("resultFormList");
		OutputStream out = null;
		InputStream in = null;
		JSON json = new JSON();
		try{
			in = new FileInputStream(cachePath);//读取文件 
			HSSFWorkbook work=new HSSFWorkbook(in);//创建xls文件
			HSSFSheet sheet=work.getSheetAt(0);//取得第一个Sheet
			HSSFRow row = null;
			HSSFCell cell = null;
			int index=0;
			
			HSSFFont font=work.createFont();
			font.setFontHeightInPoints((short)9);
			font.setFontName("微软雅黑");
			
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
			styleAlignLeft.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignLeft.setWrapText(true); 
			styleAlignLeft.setFont(font);
			
			for(int i=0;i<list.size();i++){
				MaterialPartialForm materialPartialForm = list.get(i);
				index++;
				row = sheet.createRow(index);
				
				cell = row.createCell(0);//序号
				cell.setCellValue(index);
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(1);//修理单号
				cell.setCellValue(materialPartialForm.getSorc_no());
				cell.setCellStyle(styleAlignLeft);
				
				cell = row.createCell(2);//订购次数
				cell.setCellValue(materialPartialForm.getOccur_times()+"次");
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(3);//型号
				cell.setCellValue(materialPartialForm.getModel_name());
				cell.setCellStyle(styleAlignLeft);
				
				cell = row.createCell(4);//机身号
				cell.setCellValue(materialPartialForm.getSerial_no());
				cell.setCellStyle(styleAlignLeft);
				
				cell = row.createCell(5);//等级
				cell.setCellValue(CodeListUtils.getValue("material_level", materialPartialForm.getLevel()));
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(6);//纳期
				cell.setCellValue(materialPartialForm.getScheduled_date());
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(7);//零件订购日
				cell.setCellValue(materialPartialForm.getOrder_date());
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(8);//零件到货日
				cell.setCellValue(materialPartialForm.getArrival_date());
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(9);//入库预定日
				if("9999/12/31".equals(materialPartialForm.getArrival_plan_date())){
					cell.setCellValue("未定");
				}else{
					cell.setCellValue(materialPartialForm.getArrival_plan_date());
				}
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(10);//订单状态
				cell.setCellValue(CodeListUtils.getValue("material_partial_bo_flg", materialPartialForm.getBo_flg()));
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(11);//零件缺品详细
				if(!CommonStringUtil.isEmpty(materialPartialForm.getBo_contents())){
					List<String> boContentslist =new ArrayList<String>();
					Map<String,String> boContentsMap = json.parse(materialPartialForm.getBo_contents());
					for(String key:boContentsMap.keySet()){
						if("00000000012".equals(key) || "00000000060".equals(key)){
							boContentslist.add("分解:");
						}else if("00000000013".equals(key)){
							boContentslist.add("NS:");
						}else if("00000000014".equals(key) || "00000000061".equals(key)){
							boContentslist.add("总组:");
						}else if("00000000050".equals(key) || "00000000070".equals(key)){
							boContentslist.add("外科硬镜/周边设备修理:");
						}
						boContentslist.add(boContentsMap.get(key));
					}
					cell.setCellValue(CommonStringUtil.joinBy("\n", boContentslist.toArray(new String[boContentslist.size()])));
				}
				cell.setCellStyle(styleAlignLeft);
				
				cell = row.createCell(12);//加急
				String scheduledExpedited= materialPartialForm.getScheduled_expedited();
				if("1".equals(scheduledExpedited)){
					cell.setCellValue("加急");
				}else if("2".equals(scheduledExpedited)){
					cell.setCellValue("直送快速");
				}
				cell.setCellStyle(styleAlignCenter);
				
				cell = row.createCell(13);//发生工位
				cell.setCellValue(materialPartialForm.getBo_position());
				cell.setCellStyle(styleAlignLeft);
				
				cell = row.createCell(14);//进展工位
				cell.setCellValue(materialPartialForm.getProcess_name());
				cell.setCellStyle(styleAlignLeft);
			}
			
			out= new FileOutputStream(cachePath);
			work.write(out);
		}catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}finally{
			if(in!=null){
				in.close();
			}
			if(out!=null){
				out.close();
			}
		}
		return cacheName;
	}
	
}
