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
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.master.PartialBussinessStandardEntity;
import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseDnForm;
import com.osh.rvs.mapper.manage.UserDefineCodesMapper;
import com.osh.rvs.mapper.master.PartialMapper;
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

	public void jsinit(FactProductionFeatureForm form, HttpServletRequest req, Map<String, Object> callbackResponse, SqlSession conn) {
		HttpSession session = req.getSession();
		// 零件入库DN编号
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDnForm> warehouseDnList = (List<PartialWarehouseDnForm>) session.getAttribute("warehouseDnList");

		// 零件入库明细
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDetailForm> detailList = (List<PartialWarehouseDetailForm>) session.getAttribute("detailList");

		if (detailList != null) {
			// 零件出入库工时标准
			Map<String, PartialBussinessStandardEntity> standardMap = partialBussinessStandardService.getStandardTime(conn);

			// 各个规格种别总数量
			Map<String, Integer> specKindMap = new TreeMap<String, Integer>();

			for (PartialWarehouseDetailForm temp : detailList) {
				// 数量
				Integer quantity = Integer.valueOf(temp.getQuantity());

				// 规格种别
				String specKind = temp.getSpec_kind();

				if (specKindMap.containsKey(specKind)) {
					specKindMap.put(specKind, specKindMap.get(specKind) + quantity);
				} else {
					specKindMap.put(specKind, quantity);
				}
			}

			// 总时间
			BigDecimal totalTime = new BigDecimal("0");

			// 统计各个规格种别总数量
			List<PartialWarehouseDetailForm> counttQuantityList = new ArrayList<PartialWarehouseDetailForm>();

			for (String specKind : specKindMap.keySet()) {
				PartialWarehouseDetailForm partialWarehouseDetailForm = new PartialWarehouseDetailForm();
				partialWarehouseDetailForm.setSpec_kind(specKind);

				if (standardMap.containsKey(specKind)) {
					// 总数量
					Integer totalQuantity = specKindMap.get(specKind);

					// 拆盒标准工时
					BigDecimal collectCaseTime = standardMap.get(specKind).getCollect_case();

					// 收货标准工时
					BigDecimal receptTime = standardMap.get(specKind).getRecept();

					collectCaseTime = collectCaseTime.multiply(new BigDecimal(totalQuantity));
					totalTime = totalTime.add(collectCaseTime);

					// 装箱数量
					BigDecimal boxCount = new BigDecimal(standardMap.get(specKind).getBox_count());

					if (boxCount.compareTo(BigDecimal.ZERO) < 0) {// 装箱数量<0,
						partialWarehouseDetailForm.setQuantity("1");

						totalTime = totalTime.add(receptTime);
					} else {
						// 总数量
						BigDecimal quantity = new BigDecimal(totalQuantity.toString());
						quantity = quantity.divide(boxCount, RoundingMode.UP);// 向上取整，不足一箱按一箱计算
						partialWarehouseDetailForm.setQuantity(quantity.toString());

						receptTime = receptTime.multiply(quantity);
						totalTime = totalTime.add(receptTime);
					}

					counttQuantityList.add(partialWarehouseDetailForm);
				}
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

			// 零件入库DN编号
			callbackResponse.put("partialWarehouseDnList", warehouseDnList);

			// 零件入库明细
			callbackResponse.put("partialWarehouseDetailList", detailList);

			// 各个规格种别总数量
			callbackResponse.put("counttQuantityList", counttQuantityList);

			// 作业标准时间
			callbackResponse.put("leagal_overline", totalTime.toString());
		}

	}

	public void upload(ActionForm form, HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		UploadService uService = new UploadService();

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

		// 解析文件
		readFile(tempFileNames, req, conn, errors, false, 0);

	}

	/**
	 * 解析文件
	 *
	 * @param filename
	 * @param conn
	 * @param errors
	 */
	public void readFile(List<String> tempFileNames, HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors, boolean supply, int supplySeq) throws Exception {
		PartialWarehouseDnSerice partialWarehouseDnSerice = new PartialWarehouseDnSerice();
		PartialMapper partialDao = conn.getMapper(PartialMapper.class);

		HttpSession session = req.getSession();

		InputStream in = null;
		try {
			XSSFWorkbook work = null;
			XSSFSheet sheet = null;
			String dnNo = "";

			Map<String, String> dnNoMap = new HashMap<String, String>();

			// 零件入库DN编号
			@SuppressWarnings("unchecked")
			List<PartialWarehouseDnForm> warehouseDnList = (List<PartialWarehouseDnForm>) session.getAttribute("warehouseDnList");
			if (warehouseDnList == null) {
				warehouseDnList = new ArrayList<PartialWarehouseDnForm>();
			} else {
				for (PartialWarehouseDnForm partialWarehouseDnForm : warehouseDnList) {
					dnNo = partialWarehouseDnForm.getDn_no().toUpperCase();
					dnNoMap.put(dnNo, dnNo);
				}
			}

			int alreadySeq = warehouseDnList.size();

			// 零件入库明细
			@SuppressWarnings("unchecked")
			List<PartialWarehouseDetailForm> detailList = (List<PartialWarehouseDetailForm>) session.getAttribute("detailList");
			if (detailList == null) {
				detailList = new ArrayList<PartialWarehouseDetailForm>();
			}

			for (int i = 0; i < tempFileNames.size(); i++) {
				int seq = 0;

				if (supply) {
					seq = supplySeq + alreadySeq + i + 1;
				} else {
					seq = alreadySeq + i + 1;
				}

				in = new FileInputStream(tempFileNames.get(i));// 读取文件

				work = new XSSFWorkbook(in);// 创建Excel

				sheet = work.getSheetAt(0);// 获取Sheet

				// 验证Excel
				PartialWarehouseDnForm partialWarehouseDnForm = this.validateExcel(sheet, seq, errors);

				if (errors.size() > 0)
					return;

				// DN 编号
				dnNo = partialWarehouseDnForm.getDn_no();

				// DN 编号转成大写
				dnNo = dnNo.toUpperCase();

				PartialWarehouseDnForm tempForm = partialWarehouseDnSerice.getPartialWarehouseDnByDnNo(dnNo, conn);

				// 存在DN 编号
				if (tempForm != null) {
					// 编号重复
					MsgInfo error = new MsgInfo();
					error.setErrcode("dbaccess.recordDuplicated");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "DN编号[" + dnNo + "]"));
					errors.add(error);
					return;
				}

				// 判断上传的文件中是否存在DN 编号重复
				if (dnNoMap.containsKey(dnNo)) {
					// 编号重复
					MsgInfo error = new MsgInfo();
					error.setErrmsg("上传的文件中，DN编号[" + dnNo + "]重复，请确认。");
					errors.add(error);
					return;
				} else {
					dnNoMap.put(dnNo, dnNo);
				}

				// 零件入库明细
				List<PartialWarehouseDetailForm> list = this.setPartialWarehouseDetail(sheet, partialWarehouseDnForm, seq, partialDao, errors);

				if (errors.size() > 0)
					return;

				// 没有数据
				if (list.size() == 0) {
					errors.add(getFormatError());
					return;
				}

				warehouseDnList.add(partialWarehouseDnForm);
				detailList.addAll(list);
			}

			// 没有数据
			if (detailList.size() == 0) {
				errors.add(getFormatError());
				return;
			}

			session.setAttribute("warehouseDnList", warehouseDnList);
			session.setAttribute("detailList", detailList);

		} catch (Exception e) {
			throw e;
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
	 * 零件入库明细
	 *
	 * @param sheet
	 * @param seq 序号
	 * @param partialDao
	 * @param errors
	 * @return
	 */
	private List<PartialWarehouseDetailForm> setPartialWarehouseDetail(XSSFSheet sheet, PartialWarehouseDnForm partialWarehouseDnForm, int seq, PartialMapper partialDao, List<MsgInfo> errors) {
		// 检查零件是否重复
		Map<String, String> checkPartailRepeat = new HashMap<String, String>();

		List<PartialWarehouseDetailForm> list = new ArrayList<PartialWarehouseDetailForm>();

		XSSFRow row = null;
		XSSFCell cell = null;

		String message = "";

		String dnNo = partialWarehouseDnForm.getDn_no();

		String warehouseDate = partialWarehouseDnForm.getWarehouse_date();

		// 遍历文件
		for (int iRow = 5; iRow <= sheet.getLastRowNum(); iRow++) {
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
				message = "DN编号为[" + dnNo + "]验收确认单的零件编号为[" + code + "]";

				// 零件不存在
				MsgInfo error = new MsgInfo();
				error.setErrcode("dbaccess.recordNotExist");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", message));
				errors.add(error);
				continue;
			}

			// 零件编码转换成大写
			code = code.toUpperCase();

			if (checkPartailRepeat.containsKey(code)) {
				message = "DN编号为[" + dnNo + "]验收确认单的零件编号为[" + code + "]重复";

				// 零件重复
				MsgInfo error = new MsgInfo();
				error.setErrmsg(message);
				errors.add(error);
				continue;
			} else {
				checkPartailRepeat.put(code, code);
			}

			// 第三列
			cell = row.getCell(2);

			// 单元格不存在，停止读取
			if (cell == null) {
				message = "DN编号为[" + dnNo + "]验收确认单的零件编号为[" + code + "]的数量";
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.required");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", message));
				errors.add(error);
				break;
			}

			// 数量
			String strQuantity = CopyByPoi.getStringCellValue(cell);

			// 去前后空格
			strQuantity = CommonStringUtil.trim(strQuantity);

			message = "DN编号为[" + dnNo + "]验收确认单的零件编号为[" + code + "]的数量";

			// 数量没有填写,停止读取
			if (CommonStringUtil.isEmpty(strQuantity)) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.required");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", message));
				errors.add(error);
				break;
			} else if (strQuantity.length() > 5) {// 长度大于5
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.invalidParam.invalidMaxLengthValue");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMaxLengthValue", message, "5"));
				errors.add(error);
				break;
			} else if (!UploadService.isNum(strQuantity)) {// 数量不是数字
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.invalidParam.invalidIntegerValue");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidIntegerValue", message));
				errors.add(error);
				break;
			} else if (Integer.valueOf(strQuantity) <= 0) {// 数字小于1
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.invalidParam.invalidMoreThanZero");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidMoreThanZero", message));
				errors.add(error);
				break;
			}

			PartialEntity partialEntity = partialList.get(0);

			PartialWarehouseDetailForm form = new PartialWarehouseDetailForm();
			// 序号
			form.setSeq(String.valueOf(seq));
			// 零件 ID
			form.setPartial_id(partialEntity.getPartial_id());
			// 零件名称
			form.setPartial_name(partialEntity.getName());
			// 零件编号
			form.setCode(code);
			// 规格种别
			form.setSpec_kind(partialEntity.getSpec_kind().toString());
			// 数量
			form.setQuantity(strQuantity);
			// 核对数量(零件发放默认为0)
			form.setCollation_quantity("0");

			form.setDn_no(dnNo);
			form.setWarehouse_date(warehouseDate);

			list.add(form);
		}

		return list;
	}

	/**
	 * 验证Excel
	 *
	 * @param sheet
	 * @param seq 序号
	 * @param errors
	 * @return
	 */
	private PartialWarehouseDnForm validateExcel(XSSFSheet sheet, Integer seq, List<MsgInfo> errors) {
		PartialWarehouseDnForm partialWarehouseDnForm = new PartialWarehouseDnForm();

		XSSFRow row = null;
		XSSFCell cell = null;

		// 第二行
		row = sheet.getRow(1);
		if (row == null || row.getCell(1) == null) {
			errors.add(getFormatError());
			return null;
		}

		// 第三行
		row = sheet.getRow(2);
		if (row == null || row.getCell(1) == null) {
			errors.add(getFormatError());
			return null;
		}

		// 第五行
		row = sheet.getRow(4);
		if (row == null || row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null) {
			errors.add(getFormatError());
			return null;
		}

		if (!"零件编号".equals(CopyByPoi.getStringCellValue(row.getCell(0)))) {
			errors.add(getFormatError());
			return null;
		}

		if (!"零件名称".equals(CopyByPoi.getStringCellValue(row.getCell(1)))) {
			errors.add(getFormatError());
			return null;
		}

		if (!"数量".equals(CopyByPoi.getStringCellValue(row.getCell(2)))) {
			errors.add(getFormatError());
			return null;
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

		partialWarehouseDnForm.setWarehouse_date(strWarehouseDate);
		partialWarehouseDnForm.setDn_no(dnNo);

		Validators v = BeanUtil.createBeanValidators(partialWarehouseDnForm, BeanUtil.CHECK_TYPE_ALL);
		v.delete("key");
		v.delete("seq");
		errors.addAll(v.validate());

		// 日期格式验证
		if (!CommonStringUtil.isEmpty(strWarehouseDate) && !strWarehouseDate.matches(DATE_EXPRESSION) && !strWarehouseDate.matches(ISO_DATE_EXPRESSION)) {// 日期形式不匹配
			MsgInfo error = new MsgInfo();
			error.setErrcode("validator.invalidParam.invalidDateValue");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidDateValue", "日期", DateUtil.DATE_PATTERN + "或者" + DateUtil.ISO_DATE_PATTERN));
			errors.add(error);
		}

		// 序号
		partialWarehouseDnForm.setSeq(String.valueOf(seq));

		return partialWarehouseDnForm;
	}

	/**
	 * 作业经过时间
	 *
	 * @param time
	 * @return
	 */
	public String getSpentTimes(String time) {
		Calendar cal = Calendar.getInstance();
		// 毫秒
		cal.set(Calendar.MILLISECOND, 0);

		// 相差毫秒数
		long millisecond = cal.getTimeInMillis() - DateUtil.toDate(time, DateUtil.DATE_TIME_PATTERN).getTime();
		if (millisecond < 0)
			millisecond = 0;

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
