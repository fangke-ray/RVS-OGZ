package com.osh.rvs.service.partial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.partial.PartialWarehouseEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseDnForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.mapper.partial.PartialWarehouseMapper;
import com.osh.rvs.service.UploadService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseService {
	public List<PartialWarehouseForm> search(ActionForm form, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<PartialWarehouseEntity> list = dao.search(entity);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseForm.class);
		}

		return respList;
	}

	/**
	 * 新建零件入库单
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void insert(ActionForm form, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 新建零件入库单
		dao.insert(entity);
	}

	/**
	 * 删除零件入库单
	 *
	 * @param key 零件入库单 KEY
	 * @param conn 数据库连接
	 */
	public void delete(String key, SqlSessionManager conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		// 删除零件入库单
		dao.delete(key);
	}

	/**
	 * 更新入库进展
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 */
	public void updateStep(ActionForm form, SqlSession conn) throws Exception {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新入库进展
		dao.updateStep(entity);
	}

	/**
	 * 根据key查询零件入库单信息
	 *
	 * @param key 零件入库单 KEY
	 * @param conn 数据库连接
	 * @return respForm 零件入库单,如果存零件入库单则返回零件入库单，如果不存零件入库单则返回NULL
	 */
	public PartialWarehouseForm getByKey(String key, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		// 查询零件入库单信息
		PartialWarehouseEntity entity = dao.getByKey(key);

		PartialWarehouseForm respForm = null;

		if (entity != null) {
			respForm = new PartialWarehouseForm();
			// 复制模型数据到表单
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}

		return respForm;
	}

	/**
	 * 查询最大零件入库单号
	 *
	 * @param warehouse_no
	 * @param conn
	 * @return
	 */
	public Integer getMaxWarehouseNo(String warehouse_no, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		String maxNo = dao.getMaxWarehouseNo(warehouse_no);

		if (!CommonStringUtil.isEmpty(maxNo)) {
			return Integer.valueOf(maxNo);
		} else {
			return null;
		}
	}

	/**
	 * 查询当前入库进展信息
	 *
	 * @param form 表单
	 * @param conn 数据库连接
	 * @return respList
	 */
	public List<PartialWarehouseForm> searchPartialWarehouseByStep(String step, SqlSession conn) {
		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		// 查询当前入库进展信息
		List<PartialWarehouseEntity> list = dao.searchPartialWarehouseByStep(step);
		if (list != null && list.size() > 0) {
			// 复制模型数据到表单
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, PartialWarehouseForm.class);
		}

		return respList;
	}

	/**
	 * 查询当前入库进展信息 （表格内容：序号/入库单日期/DN 编号/零件编号/零件名称/入库单数量/核对数量/核对日期/核对人员）
	 *
	 * @param conn 数据库连接
	 */
	public String createUnmatchReport(ActionForm form, SqlSession conn) throws Exception {
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "入库单核对不一致一览表模板.xlsx";
		String cacheName = "入库单核对不一致一览" + new Date().getTime() + ".xlsx";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" + cacheName;
		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 数据库连接对象
		PartialWarehouseMapper dao = conn.getMapper(PartialWarehouseMapper.class);

		PartialWarehouseEntity entity = new PartialWarehouseEntity();
		// 复制表单数据到数据模型
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<PartialWarehouseEntity> list = dao.searchUnmatch(entity);

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(cachePath);// 读取文件
			XSSFWorkbook work = new XSSFWorkbook(in);// 创建xls文件
			XSSFSheet sheet = work.getSheetAt(0);// 创建第一个Sheet

			XSSFRow row = null;
			XSSFCell cell = null;

			XSSFDataFormat dataFormat = work.createDataFormat();

			XSSFFont font = work.createFont();
			font.setFontHeightInPoints((short) 10);
			font.setFontName("微软雅黑");

			/* 基本样式 */
			XSSFCellStyle baseStyle = work.createCellStyle();
			baseStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			baseStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			baseStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
			baseStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			baseStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			baseStyle.setFont(font);

			/* 设置单元格内容居中显示 */
			XSSFCellStyle alignCenterStyle = work.createCellStyle();
			alignCenterStyle.cloneStyleFrom(baseStyle);
			alignCenterStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

			/* 设置单元格内容右显示 */
			XSSFCellStyle alignRightStyle = work.createCellStyle();
			alignRightStyle.cloneStyleFrom(baseStyle);
			alignRightStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT);

			/* 设置单元格内容左显示 */
			XSSFCellStyle alignLeftStyle = work.createCellStyle();
			alignLeftStyle.cloneStyleFrom(baseStyle);
			alignLeftStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);

			XSSFCellStyle dateStyle = work.createCellStyle();
			dateStyle.cloneStyleFrom(alignCenterStyle);
			dateStyle.setDataFormat(dataFormat.getFormat("yyyy/mm/dd"));

			for (int i = 0; i < list.size(); i++) {
				entity = list.get(i);
				// 核对数量
				Integer collationQuantity = entity.getCollation_quantity();

				Integer seq = entity.getSeq();

				row = sheet.createRow(i + 1);

				// 序号
				CellUtil.createCell(row, 0, String.valueOf(i + 1), alignCenterStyle);

				//入库单编号
				CellUtil.createCell(row, 1, entity.getWarehouse_no(), alignLeftStyle);

				// 入库单日期
				cell = row.createCell(2);
				cell.setCellValue(entity.getWarehouse_date());
				cell.setCellStyle(dateStyle);

				// DN 编号
				if(seq == 0){
					CellUtil.createCell(row, 3, "DN 编号以外零件", alignLeftStyle);
				}else{
					CellUtil.createCell(row, 3, entity.getDn_no(), alignLeftStyle);
				}


				// 零件编号
				CellUtil.createCell(row, 4, entity.getCode(), alignLeftStyle);

				// 零件名称
				CellUtil.createCell(row, 5, entity.getPartial_name(), alignLeftStyle);

				// 入库单数量
				if (seq == 0) {
					CellUtil.createCell(row, 6, "没有", alignLeftStyle);
				} else {
					CellUtil.createCell(row, 6, entity.getQuantity().toString(), alignRightStyle);
				}

				// 核对数量
				CellUtil.createCell(row, 7, collationQuantity.toString(), alignRightStyle);

				// 核对日期
				cell = row.createCell(8);
				cell.setCellValue(entity.getFinish_date_start());
				cell.setCellStyle(dateStyle);

				// 核对人员
				CellUtil.createCell(row, 9, entity.getOperator_name(), alignLeftStyle);
			}

			out = new FileOutputStream(cachePath);
			work.write(out);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}

		return cacheName;
	}

	/**
	 * 补充入库单
	 * @param conn
	 * @param errors
	 */
	@SuppressWarnings("unchecked")
	public void supply(ActionForm form,HttpServletRequest request,SqlSessionManager conn,List<MsgInfo> errors) throws Exception{
		HttpSession session = request.getSession();

		FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(request, conn);

		String key = factProductionFeatureForm.getPartial_warehouse_key();

		PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
		PartialWarehouseDnSerice partialWarehouseDnSerice = new PartialWarehouseDnSerice();
		PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
		UploadService uService = new UploadService();

		List<PartialWarehouseDnForm> warehouseDnList = partialWarehouseDnSerice.searchByKey(key, conn);
		Integer seq = warehouseDnList.size();

		for(PartialWarehouseDnForm partialWarehouseDnForm:warehouseDnList){
			if("0".equals(partialWarehouseDnForm.getSeq())){
				seq = seq -1;
				break;
			}
		}

		List<String> tempFileNames = uService.getFiles2Local(form, errors);

		if (errors.size() != 0)
			return;

		for (String tempfilename : tempFileNames) {
			if (!tempfilename.endsWith(".xlsx")) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidType");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidType"));
				errors.add(error);
				return;
			}
		}

		PartialReceptService partialReceptService = new PartialReceptService();

		partialReceptService.readFile(tempFileNames, request, conn, errors, true, seq);

		if(errors.size() == 0){
			// 零件入库DN编号
			warehouseDnList = (List<PartialWarehouseDnForm>)session.getAttribute("warehouseDnList");
			// 零件入库明细
			List<PartialWarehouseDetailForm> detailList = (List<PartialWarehouseDetailForm>)session.getAttribute("detailList");
			for (int i = 0; i < warehouseDnList.size(); i++) {
				PartialWarehouseDnForm partialWarehouseDnForm = warehouseDnList.get(i);
				// KEY
				partialWarehouseDnForm.setKey(key);
				// 新建零件入库DN编号
				partialWarehouseDnSerice.insert(partialWarehouseDnForm, conn);
			}

			for (int i = 0; i < detailList.size(); i++) {
				PartialWarehouseDetailForm partialWarehouseDetailForm = detailList.get(i);
				// KEY
				partialWarehouseDetailForm.setKey(key);
				// 新建零件入库明细
				partialWarehouseDetailService.insert(partialWarehouseDetailForm, conn);
			}

			PartialWarehouseForm partialWarehouseForm = partialWarehouseService.getByKey(key, conn);
			if("0".equals(partialWarehouseForm.getStep())){
				partialWarehouseForm.setStep("1");
				partialWarehouseService.updateStep(partialWarehouseForm, conn);
			}

			session.removeAttribute("warehouseDnList");
			session.removeAttribute("detailList");
		}


	}

}
