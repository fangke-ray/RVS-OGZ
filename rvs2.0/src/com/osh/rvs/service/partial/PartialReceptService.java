package com.osh.rvs.service.partial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.bean.partial.PartialWarehouseDetailEntity;
import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.manage.UserDefineCodesMapper;
import com.osh.rvs.mapper.master.PartialMapper;
import com.osh.rvs.mapper.partial.PartialWarehouseDetailMapper;
import com.osh.rvs.service.PartialBussinessStandardService;
import com.osh.rvs.service.UploadService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

/**
 * 零件收货
 *
 * @author liuxb
 *
 */
public class PartialReceptService {
	/** yyyy/MM/dd **/
	private final String DATE_EXPRESSION = "\\d{4}/\\d{2}/\\d{2}";
	/** yyyy-MM-dd **/
	private final String ISO_DATE_EXPRESSION = "\\d{4}-\\d{2}-\\d{2}";

	private final PartialBussinessStandardService partialBussinessStandardService = new PartialBussinessStandardService();

	public void upload(ActionForm form, HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors) {
		UploadService uService = new UploadService();
		String tempfilename = uService.getFile2Local(form, errors);

		if (errors.size() != 0)
			return;

		if (!tempfilename.endsWith(".xlsx")) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.invalidType");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidType"));
			errors.add(error);
			return;
		}

		readFile(tempfilename, req, conn, errors);

	}

	/**
	 * 解析文件
	 *
	 * @param filename
	 * @param conn
	 * @param errors
	 */
	private void readFile(String filename, HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors) {
		FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();
		PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
		PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
		PartialMapper partialDao = conn.getMapper(PartialMapper.class);
		CommonMapper commonDao = conn.getMapper(CommonMapper.class);

		InputStream in = null;
		String lastInsertID = null;
		try {
			// 零件入库单
			PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();

			// 零件入库明细
			List<PartialWarehouseDetailForm> detailList = new ArrayList<PartialWarehouseDetailForm>();

			Map<String, String> checkPartailRepeat = new HashMap<String, String>();

			in = new FileInputStream(filename);// 读取文件

			XSSFWorkbook work = new XSSFWorkbook(in);// 创建Excel

			XSSFSheet sheet = work.getSheetAt(0);// 获取Sheet

			XSSFRow row = null;
			XSSFCell cell = null;

			validateExcel(sheet, partialWarehouseForm, errors);
			if (errors.size() > 0)
				return;

			// DN 编号
			String dnNo = partialWarehouseForm.getDn_no();

			PartialWarehouseForm tempWarehouseForm = partialWarehouseService.getPartialWarehouseByDnNo(dnNo, conn);
			if (tempWarehouseForm != null) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("dbaccess.recordDuplicated");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "DN编号[" + dnNo + "]"));
				errors.add(error);
				return;
			}

			// 遍历文件
			for (int iRow = 5; iRow <= sheet.getLastRowNum(); iRow++) {
				PartialWarehouseDetailForm partialWarehouseDetailForm = new PartialWarehouseDetailForm();
				row = sheet.getRow(iRow);

				// 行不存在，读取下一行
				if (row == null)
					continue;

				// 第一列
				cell = row.getCell(0);

				// 单元格不存在，停止读取
				if (cell == null)
					break;

				// 零件编号
				String code = CopyByPoi.getStringCellValue(cell);

				// 零件编号没有填写,停止读取
				if (CommonStringUtil.isEmpty(code))
					break;

				if (code.contains("箱数"))
					break;

				// 查询零件信息
				List<PartialEntity> partialList = partialDao.getPartialByCode(code);
				if (partialList == null || partialList.size() == 0) {
					// 零件不存在
					MsgInfo error = new MsgInfo();
					error.setErrcode("dbaccess.recordNotExist");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "零件编码[" + code + "]"));
					errors.add(error);
					break;
				}

				if (checkPartailRepeat.containsKey(code)) {
					// 零件重复
					MsgInfo error = new MsgInfo();
					error.setErrmsg("零件编码[" + code + "]重复");
					errors.add(error);
					break;
				} else {
					checkPartailRepeat.put(code, code);
				}

				// 第三列
				cell = row.getCell(2);

				// 单元格不存在，停止读取
				if (cell == null) {
					MsgInfo error = new MsgInfo();
					error.setErrcode("validator.required");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "零件编号[" + code + "]数量"));
					errors.add(error);
					break;
				}

				// 数量
				String strQuantity = CopyByPoi.getStringCellValue(cell);

				// 去前后空格
				strQuantity = CommonStringUtil.trim(strQuantity);

				// 数量没有填写,停止读取
				if (CommonStringUtil.isEmpty(strQuantity)) {
					MsgInfo error = new MsgInfo();
					error.setErrcode("validator.required");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "零件编号[" + code + "]数量"));
					errors.add(error);
					break;
				} else if (strQuantity.length() > 5) {// 长度大于5
					MsgInfo error = new MsgInfo();
					error.setErrcode("validator.invalidParam.invalidMaxLengthValue");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMaxLengthValue", "零件编号[" + code + "]的数量", "5"));
					errors.add(error);
					break;
				} else if (!UploadService.isNum(strQuantity)) {// 数量不是数字
					MsgInfo error = new MsgInfo();
					error.setErrcode("validator.invalidParam.invalidIntegerValue");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidIntegerValue", "零件编号[" + code + "]的数量"));
					errors.add(error);
					break;
				} else if (Integer.valueOf(strQuantity) <= 0) {// 数字小于1
					MsgInfo error = new MsgInfo();
					error.setErrcode("validator.invalidParam.invalidMoreThanZero");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMoreThanZero", "零件编号[" + code + "]的数量"));
					errors.add(error);
					break;
				}

				PartialEntity partialEntity = partialList.get(0);
				// 零件ID
				partialWarehouseDetailForm.setPartial_id(partialEntity.getPartial_id());

				// 数量
				partialWarehouseDetailForm.setQuantity(strQuantity);

				// 核对数量(零件发放默认为0)
				partialWarehouseDetailForm.setCollation_quantity("0");

				detailList.add(partialWarehouseDetailForm);
			}

			if (errors.size() > 0)
				return;

			if (detailList.size() == 0) {
				errors.add(getFormatError());
				return;
			}

			// 新建零件入库单
			partialWarehouseService.insert(partialWarehouseForm, conn);

			lastInsertID = commonDao.getLastInsertID();

			for (int i = 0; i < detailList.size(); i++) {
				PartialWarehouseDetailForm partialWarehouseDetailForm = detailList.get(i);
				// KEY
				partialWarehouseDetailForm.setKey(lastInsertID);
				// 新建零件入库明细
				partialWarehouseDetailService.insert(partialWarehouseDetailForm, conn);
			}

			// 查询未结束作业的记录
			FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
			factProductionFeatureForm.setPartial_warehouse_key(lastInsertID);
			factProductionFeatureService.updateKey(factProductionFeatureForm, conn);

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
		}
	}

	/**
	 * 验证Excel
	 *
	 * @param sheet
	 * @param entity
	 * @param errors
	 * @return 总计所在行索引
	 */
	private void validateExcel(XSSFSheet sheet, PartialWarehouseForm partialWarehouseForm, List<MsgInfo> errors) {
		XSSFRow row = null;
		XSSFCell cell = null;

		// 第二行
		row = sheet.getRow(1);
		if (row == null || row.getCell(1) == null) {
			errors.add(getFormatError());
			return;
		}

		// 第三行
		row = sheet.getRow(2);
		if (row == null || row.getCell(1) == null) {
			errors.add(getFormatError());
			return;
		}

		// 第五行
		row = sheet.getRow(4);
		if (row == null || row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null) {
			errors.add(getFormatError());
			return;
		}

		if (!"零件编号".equals(CopyByPoi.getStringCellValue(row.getCell(0)))) {
			errors.add(getFormatError());
			return;
		}

		if (!"零件名称".equals(CopyByPoi.getStringCellValue(row.getCell(1)))) {
			errors.add(getFormatError());
			return;
		}

		if (!"数量".equals(CopyByPoi.getStringCellValue(row.getCell(2)))) {
			errors.add(getFormatError());
			return;
		}

		// 日期
		cell = sheet.getRow(1).getCell(1);
		String strWarehouseDate = null;
		// 单元格类型为【数字】并且是数字类型下的【日期】类型
		if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
			strWarehouseDate = DateUtil.toString(cell.getDateCellValue(), DateUtil.DATE_PATTERN);
		} else {
			strWarehouseDate = CopyByPoi.getStringCellValue(cell);
		}

		// DN编号
		cell = sheet.getRow(2).getCell(1);
		String dnNo = CopyByPoi.getStringCellValue(cell);

		partialWarehouseForm.setWarehouse_date(strWarehouseDate);
		partialWarehouseForm.setDn_no(dnNo);

		Validators v = BeanUtil.createBeanValidators(partialWarehouseForm, BeanUtil.CHECK_TYPE_ALL);
		v.delete("key");
		v.delete("step");
		errors.addAll(v.validate());

		// 日期格式验证
		if (!CommonStringUtil.isEmpty(strWarehouseDate) && !strWarehouseDate.matches(DATE_EXPRESSION) && !strWarehouseDate.matches(ISO_DATE_EXPRESSION)) {// 日期形式不匹配
			MsgInfo error = new MsgInfo();
			error.setErrcode("validator.invalidParam.invalidDateValue");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidDateValue", "日期", DateUtil.DATE_PATTERN + "或者" + DateUtil.ISO_DATE_PATTERN));
			errors.add(error);
		}

		// 收货步骤为0
		partialWarehouseForm.setStep("0");
	}

	/**
	 * 作业标准时间
	 *
	 * @param list
	 * @param conn
	 * @return
	 */
	public String getStandardTime(String key, SqlSession conn) {
		Map<Integer, BigDecimal> receptMap = partialBussinessStandardService.getReceptStandardTime(conn);
		Map<Integer, BigDecimal> collectCaseMap = partialBussinessStandardService.getCollectCaseStandardTime(conn);

		PartialWarehouseDetailMapper partialWarehouseDetailMapper = conn.getMapper(PartialWarehouseDetailMapper.class);

		PartialWarehouseDetailEntity entity = new PartialWarehouseDetailEntity();
		entity.setKey(key);
		List<PartialWarehouseDetailEntity> list = partialWarehouseDetailMapper.countQuantityOfSpecKind(entity);

		// 总时间
		BigDecimal totalTime = new BigDecimal("0");

		for (int i = 0; i < list.size(); i++) {
			Integer specKind = list.get(i).getSpec_kind();

			// 箱数
			Integer quantity = list.get(i).getQuantity();

			// 标准工时
			BigDecimal time = receptMap.get(specKind);
			time = time.multiply(new BigDecimal(quantity));

			totalTime = totalTime.add(time);
		}

		list = partialWarehouseDetailMapper.searchByKey(key);
		for (int i = 0; i < list.size(); i++) {
			Integer specKind = list.get(i).getSpec_kind();

			//数量
			Integer quantity = list.get(i).getQuantity();

			// 标准工时
			BigDecimal time = collectCaseMap.get(specKind);
			time = time.multiply(new BigDecimal(quantity));

			totalTime = totalTime.add(time);
		}

		UserDefineCodesMapper dao = conn.getMapper(UserDefineCodesMapper.class);

		// 收货搬运移动标准工时
		String value = dao.searchUserDefineCodesValueByCode("PARTIAL_RECEPT_MOVE_COST");
		BigDecimal bdPartialReceptMoveCost = null;
		try {
			bdPartialReceptMoveCost = new BigDecimal(value);
		} catch (Exception e) {
			bdPartialReceptMoveCost = new BigDecimal(12);
		}
		totalTime = totalTime.add(bdPartialReceptMoveCost);

		// 向上取整
		totalTime = totalTime.setScale(0, RoundingMode.UP);
		return totalTime.toString();
	}

	/**
	 * 作业经过时间
	 *
	 * @param time
	 * @return
	 */
	public String getSpentTimes(String time) {
		Calendar cal = Calendar.getInstance();

		// 相差毫秒数
		long millisecond = cal.getTimeInMillis() - DateUtil.toDate(time, DateUtil.DATE_TIME_PATTERN).getTime();

		BigDecimal diff = new BigDecimal(millisecond);
		// 1分钟
		BigDecimal oneMinute = new BigDecimal(60000);

		BigDecimal spent = diff.divide(oneMinute, RoundingMode.UP);

		return spent.toString();
	}

	private MsgInfo getFormatError() {
		MsgInfo error = new MsgInfo();
		error.setErrcode("file.invalidFormat");
		error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
		return error;
	}

}
