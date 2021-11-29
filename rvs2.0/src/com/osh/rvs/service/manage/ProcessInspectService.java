package com.osh.rvs.service.manage;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import com.osh.rvs.bean.manage.ProcessInspectAchievementEntity;
import com.osh.rvs.bean.manage.ProcessInspectSearchEntity;
import com.osh.rvs.bean.manage.ProcessInspectSummaryEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.manage.ProcessInspectAchievementForm;
import com.osh.rvs.form.manage.ProcessInspectForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.manage.ProcessInspectConfirmMapper;
import com.osh.rvs.mapper.manage.ProcessInspectMapper;
import com.osh.rvs.mapper.master.LineMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.service.OperatorService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class ProcessInspectService {

	private static Logger logger = Logger.getLogger(ProcessInspectService.class);

	/**
	 * 取得线长以上人员(含兼任)
	 * @param conn
	 * @return
	 */
	public String getInspectors(Integer department, SqlSession conn) {

		List<String[]> lst = new ArrayList<String[]>();
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		int privacy_id = RvsConsts.PRIVACY_LINE;// 线长
		List<OperatorNamedEntity> list = dao.getOperatorWithPrivacy(privacy_id, department);

		lst = OperatorService.getSetReferChooser(list, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);
		return pReferChooser;
	}

	/**
	 * 检索处理
	 * @param form
	 * @param department
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<ProcessInspectForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {

		List<ProcessInspectForm> ret = new ArrayList<>();

		ProcessInspectSearchEntity bean = new ProcessInspectSearchEntity();

		BeanUtil.copyToBean(form, bean, CopyOptions.COPYOPTIONS_NOEMPTY);

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		try {
			List<ProcessInspectSearchEntity> entitys = dao.search(bean);

			BeanUtil.copyToFormList(entitys, ret, CopyOptions.COPYOPTIONS_NOEMPTY, ProcessInspectForm.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
			MsgInfo arg0 = new MsgInfo();
			arg0.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.search.timeout"));
			errors.add(arg0);
		}

		return ret;
	}

	public Integer countAchievementType(String processInspectKey, SqlSession conn, List<MsgInfo> errors) {

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		return dao.countAchievementType(processInspectKey);
	}

	public ProcessInspectForm findSummaryByKey(String processInspectKey, SqlSession conn, List<MsgInfo> errors) {

		DecimalFormat df = new DecimalFormat("###.#");

		ProcessInspectForm res = new ProcessInspectForm();

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		ProcessInspectSummaryEntity entity = dao.findSummaryByKey(processInspectKey);

		BeanUtil.copyToBean(entity, res, CopyOptions.COPYOPTIONS_NOEMPTY);

		// res.setFile_type(entity.getFile_type());

		res.setPerform_option_name(CodeListUtils.getValue("inspect_perform_option", String.valueOf(entity.getPerform_option())));

		res.setFiling_date(DateUtil.toString(entity.getFiling_date(), DateUtil.DATE_PATTERN));

		res.setInspect_date(DateUtil.toString(entity.getInspect_date(), DateUtil.DATE_PATTERN));

		if (entity.getStandard_seconds() != null) {
			res.setStandard_seconds(df.format(entity.getStandard_seconds()));
		}

		if (entity.getProcess_seconds() != null) {
			res.setProcess_seconds(df.format(entity.getProcess_seconds()));
		}

		return res;
	}

	public Map<String, List<ProcessInspectAchievementForm>> findAchievementByKey(String processInspectKey, SqlSession conn, List<MsgInfo> errors) {

		Map<String, List<ProcessInspectAchievementForm>> res = new HashMap<>();

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		List<ProcessInspectAchievementEntity> entitys = dao.findAchievementByKey(processInspectKey);

		for (ProcessInspectAchievementEntity entity : entitys) {

			ProcessInspectAchievementForm detail = new ProcessInspectAchievementForm();

			detail.setProcess_inspect_key(entity.getProcess_inspect_key());
			detail.setProcess_name(entity.getProcess_name());
			detail.setLine_seq(entity.getLine_seq());
			detail.setInspect_item(entity.getInspect_item());
			detail.setNeed_check(entity.getNeed_check());
			detail.setInspect_content(entity.getInspect_content());
			detail.setRowspan(entity.getRowspan());
			if (entity.getNeed_check() != 0) {
				if (isEmpty(entity.getUnqualified_content())) {
					detail.setUnqualified_content("无");
				} else {
					detail.setUnqualified_content(entity.getUnqualified_content());
				}
				detail.setUnqualified_treatment(entity.getUnqualified_treatment());
				detail.setUnqualified_treat_date(DateUtil.toString(entity.getUnqualified_treat_date(), DateUtil.DATE_PATTERN));
			}

			if (res.containsKey(detail.getProcess_name())) {
				res.get(detail.getProcess_name()).add(detail);
			} else {
				List<ProcessInspectAchievementForm> details = new ArrayList<>();
				details.add(detail);

				res.put(detail.getProcess_name(), details);
			}
		}


		return res;
	}

	/**
	 * 读取汇总文件
	 *
	 * @param tempfilename
	 * @param conn
	 * @param errors
	 * @return
	 */
	public ProcessInspectForm readSummaryFile(String tempfilename, SqlSession conn, List<MsgInfo> errors) {
		InputStream in = null;

		ProcessInspectForm form = new ProcessInspectForm();
		try {
			in = new FileInputStream(tempfilename);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet sheet = work.getSheetAt(0);// 获取Sheet

			LineMapper lineDao = conn.getMapper(LineMapper.class);
			OperatorMapper operDao = conn.getMapper(OperatorMapper.class);

			List<CellRangeAddress> ranges = getCombineCells(sheet);

			for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {

				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {

					int maxCellNo = row.getLastCellNum();
					for (int idx = 0; idx < maxCellNo; idx++) {
						String cellValue = getCellStringValue(row.getCell(idx));
//						System.out.println("[" + iRow + "," + idx + "]:" + cellValue);
						// 归档日期
						if (cellValue.startsWith("日期") && form.getFiling_date() == null) {
							String val = cellValue.replaceAll("日期：", "").trim();
							if (!isEmpty(val)) {
								form.setFiling_date(val.replaceAll("-", "/"));
							} else {
								String nextCellValue = getCellStringValue(row.getCell(idx + 1));
								if (!isEmpty(nextCellValue)) {
									form.setFiling_date(nextCellValue.replaceAll("-", "/"));
								}
							}
						}
						// 工程名
						if (cellValue.startsWith("工程名") && form.getLine_name() == null) {
							form.setLine_name(cellValue.replaceAll("工程名", "").trim());

							// 取工程ID
							form.setLine_id(searchLineId(lineDao, form.getLine_name()));
						}
						// 操作者
						if (cellValue.startsWith("操作者") && form.getOperator_name() == null) {
							form.setOperator_name(getCellStringValue(row.getCell(idx + 1)));

							// 取操作者ID
							form.setOperator_id(searchOperatorId(operDao, form.getOperator_name()));
						}
						// 监察者
						if (cellValue.startsWith("监查者") && form.getInspector_name() == null) {
							form.setInspector_name(getCellStringValue(row.getCell(idx + 1)));

							// 取操作者ID
							form.setInspector_id(searchOperatorId(operDao, form.getInspector_name()));
						}
						// 监察日
						if (cellValue.startsWith("监查日") && form.getInspect_date() == null) {
							String val = getCellStringValue(row.getCell(idx + 1));
							if (val != null) {
								form.setInspect_date(val.replaceAll("-", "/"));
							}
						}
						// 型号
						if (cellValue.startsWith("型号:") && form.getModel_name() == null) {
							form.setModel_name(cellValue.replaceAll("型号:", "").trim());
							form.setModel_id(ReverseResolution.getModelByName(form.getModel_name(), conn));
						}
						// 机身号
						if (cellValue.startsWith("机身号码:") && form.getSerial_no() == null) {
							form.setSerial_no(cellValue.replaceAll("机身号码:", "").trim());
						}
						// 作业时间
						if (cellValue.startsWith("作业") && cellValue.endsWith("时间") && form.getProcess_seconds() == null) {
							String val = getCellStringValue(row.getCell(idx + 1));
							val = val.replaceAll("分钟", "");
							form.setProcess_seconds(val);
						}
						// 标准时间
						if (cellValue.startsWith("标准") && cellValue.endsWith("时间") && form.getStandard_seconds() == null) {
							String val = getCellStringValue(row.getCell(idx + 1));
							val = val.replaceAll("分钟", "");
							form.setStandard_seconds(val);
						}
						// 监查情况
						if (cellValue.startsWith("监查情况") && form.getSituation() == null) {

							CellRangeAddress range = checkCombineCell(ranges, iRow, idx, true);
							if (range != null) {
								iRow++;
								StringBuilder sb = new StringBuilder();
								while (iRow < range.getLastRow()) {
									row = sheet.getRow(iRow++);

									sb.append(getCellStringValue(row.getCell(idx+1)));
									sb.append("\n");
								}
								form.setSituation(sb.toString());
							}
						}
						// 实施对策
						if (cellValue.startsWith("实施对策") && form.getCountermeasures() == null) {
							CellRangeAddress range = checkCombineCell(ranges, iRow, idx, true);
							if (range != null) {
								iRow++;
								StringBuilder sb = new StringBuilder();
								while (iRow < range.getLastRow()) {
									row = sheet.getRow(iRow++);

									sb.append(getCellStringValue(row.getCell(idx+1)));
									sb.append("\n");
								}
								form.setCountermeasures(sb.toString());
							}
						}
						// 结果
						if (cellValue.startsWith("结果") && form.getConclusion() == null) {
							CellRangeAddress range = checkCombineCell(ranges, iRow, idx, true);
							if (range != null) {
								iRow++;
								StringBuilder sb = new StringBuilder();
								while (iRow < range.getLastRow()) {
									row = sheet.getRow(iRow++);

									sb.append(getCellStringValue(row.getCell(idx+1)));
									sb.append("\n");
								}
								form.setConclusion(sb.toString());
							}
						}
					}
				}
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
		}

		return form;
	}

	/**
	 * 读取汇总文件
	 *
	 * @param tempfilename
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<ProcessInspectAchievementEntity> readAchievementFile(String tempfilename, ProcessInspectForm data, SqlSession conn, List<MsgInfo> errors) {

		List<ProcessInspectAchievementEntity> entitys = new ArrayList<>();

		final String title = "作业监查实绩表";
		final String inspectItemColName = "监查项目";
		String processName = "";

		InputStream in = null;

		try {
			in = new FileInputStream(tempfilename);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet sheet = work.getSheetAt(0);// 获取Sheet

			boolean isTargetFile = false;
			int recordRow = 0;
			int recordCol = 0;
			processName = sheet.getSheetName();

			List<CellRangeAddress> ranges = getCombineCells(sheet);

			for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {

				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {

					int maxCellNo = row.getLastCellNum();
					for (int idx = 0; idx < maxCellNo; idx++) {

						String cellValue = getCellStringValue(row.getCell(idx));
						if (cellValue.equals(title)) {
							isTargetFile = true;
						}
						if (!isTargetFile) {
							continue;
						}

						if (cellValue.equals(inspectItemColName)) {
							recordRow = iRow + 1;
							recordCol = idx;
							break;
						}
					}
				}
			}

			if (!isTargetFile) {
				return null;
			}

			for (int iRow = recordRow; iRow <= sheet.getLastRowNum(); iRow++) {
				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {
					ProcessInspectAchievementEntity entity = new ProcessInspectAchievementEntity();

					String cellValue = "";
					int iCol = recordCol;

					// key
					entity.setProcess_inspect_key(data.getProcess_inspect_key());

					// 作业名
					entity.setProcess_name(processName);

					data.setProcess_name(processName);

					// 项目行数
					entity.setLine_seq(iRow - recordRow + 1);

					// 监查项目
					cellValue = getCellStringValue(row.getCell(iCol++));
					if (isEmpty(cellValue)) {
						continue;
					}
					entity.setInspect_item(cellValue);

					// 监查
					cellValue = getCellStringValue(row.getCell(iCol++));
					if (!isEmpty(cellValue)) {
						entity.setNeed_check(1);
					} else {
						entity.setNeed_check(0);
					}

					// 监查内容
					CellRangeAddress range = checkCombineCell(ranges, iRow, iCol, false);
					if (range == null) {
						entity.setRowspan(1);
					} else {
						entity.setRowspan(0);
					}

					cellValue = getCellStringValue(row.getCell(iCol++));
					entity.setInspect_content(cellValue);

					// 不合格内容
					cellValue = getCellStringValue(row.getCell(iCol++));
					if (!isEmpty(cellValue) && !cellValue.trim().equals("无")) {
						entity.setUnqualified_content(cellValue);
					}

					// 确认印
					iCol++;

					// 不合格处理内容
					cellValue = getCellStringValue(row.getCell(iCol++));
					if (!isEmpty(cellValue)) {
						entity.setUnqualified_treatment(cellValue);
					}

					// 不合格内容完成日
					if (row.getCell(iCol).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
						cellValue = getCellStringValue(row.getCell(iCol));
						if (!isEmpty(cellValue)) {
							Date cellDateValue = HSSFDateUtil.getJavaDate(row.getCell(iCol).getNumericCellValue());
							entity.setUnqualified_treat_date(cellDateValue);
						}
					}
					iCol++;

					entitys.add(entity);
				}
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
		}

		return entitys;
	}

	/**
	 * 插入汇总
	 * @param form
	 * @param conn
	 * @param errors
	 * @return
	 * @throws Exception
	 */
	public String createSummary(ProcessInspectForm form, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		CommonMapper commonDao = conn.getMapper(CommonMapper.class);

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		checkSummary(form, errors);

		if (errors.size() > 0) {
			return null;
		}

		ProcessInspectSummaryEntity entity = new ProcessInspectSummaryEntity();

		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity.setSituation(formatLineBreak(entity.getSituation()));
		entity.setCountermeasures(formatLineBreak(entity.getCountermeasures()));
		entity.setConclusion(formatLineBreak(entity.getConclusion()));

		dao.insertSummary(entity);

		return CommonStringUtil.fillChar(commonDao.getLastInsertID(), '0', 11, true);
	}

	private String formatLineBreak(String text) {
		if (isEmpty(text)) {
			return text;
		}
		text = text.replaceAll("[＜<]br[>＞]", "\n").replaceAll("\n*$", "");
		return text;
	}

	/**
	 * 插入实绩表，Excel时
	 * @param entitys
	 * @param conn
	 * @param errors
	 * @return
	 * @throws Exception
	 */
	public void createAchievement(List<ProcessInspectAchievementEntity> entitys, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		if (entitys != null && entitys.size() > 0) {
			dao.deleteAchievementByName(entitys.get(0).getProcess_inspect_key(), entitys.get(0).getProcess_name());

			for (ProcessInspectAchievementEntity entity : entitys) {

				dao.insertAchievement(entity);
			}
		}
	}

	/**
	 * 插入实绩表，非Excel时
	 * @param processInspectKey
	 * @param processName
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void createAchievement(String processInspectKey, String processName, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);

		ProcessInspectAchievementEntity entity = new ProcessInspectAchievementEntity();

		entity.setProcess_inspect_key(processInspectKey);
		entity.setProcess_name(processName);
		entity.setLine_seq(0);
		entity.setNeed_check(0);
		entity.setRowspan(1);

		dao.deleteAchievementByName(processInspectKey, processName);

		dao.insertAchievement(entity);
	}

	public void removeAll(String processInspectKey, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {

		ProcessInspectMapper dao = conn.getMapper(ProcessInspectMapper.class);
		ProcessInspectConfirmMapper confirmDao = conn.getMapper(ProcessInspectConfirmMapper.class);

		dao.deleteSummary(processInspectKey);

		dao.deleteAchievementByKey(processInspectKey);
		
		confirmDao.deleteConfirmByKey(processInspectKey);

		String dirPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\process_inspect\\" + processInspectKey;
		File dir = new File(dirPath);

		if (dir.isDirectory() == false) return;

		File[] list = dir.listFiles();
		for (File f : list) {
			f.delete();
		}

		dir.delete();

		File zip = new File(dirPath.concat(".zip"));
		if (zip.exists()) {
			zip.delete();
		}
	}

	private boolean checkSummary(ProcessInspectForm bean, List<MsgInfo> errors) throws Exception {

		// 上传文件
		FormFile file = bean.getUploadSummaryFile();
		if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
		}

		// 实施选项
		if (isEmpty(bean.getPerform_option())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("perform_option");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "实施选项"));
			errors.add(error);
		}

		// 工程
		if (isEmpty(bean.getLine_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("line_id");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "工程"));
			errors.add(error);
		}

		// 归档日期
		if (isEmpty(bean.getFiling_date())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("filing_date");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "归档日期"));
			errors.add(error);
		}

		// 操作者
		if (isEmpty(bean.getOperator_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("operator_name");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "操作者"));
			errors.add(error);
		}

		// 监察者
		if (isEmpty(bean.getInspector_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("inspector_name");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "监察者"));
			errors.add(error);
		}

		// 监察日
		if (isEmpty(bean.getInspect_date())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("inspect_date");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "监察日"));
			errors.add(error);
		}

		return true;
	}

	public String saveSummaryFile2Local(ActionForm form, List<MsgInfo> errors, boolean isTemp) {

		final String summaryFileName = "QE0701-3 作业监查汇报";
		//
		ProcessInspectForm upfileForm = (ProcessInspectForm) form;
		// 取得上传的文件
		FormFile file = upfileForm.getUploadSummaryFile();
		FileOutputStream fileOutput;

		if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
			return "";
		}
		Date today = new Date();
		String tempfilename;
		if (isTemp) {
			tempfilename = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
		} else {
			tempfilename = PathConsts.BASE_PATH + PathConsts.REPORT + "\\process_inspect\\" + upfileForm.getProcess_inspect_key();
		}

		File fMonthPath = new File(tempfilename);
		if (!fMonthPath.exists()) {
			fMonthPath.mkdirs();
		}
		fMonthPath = null;

		if (isTemp) {
			tempfilename += "\\" + today.getTime() + file.getFileName();
		} else {
			if (file.getFileName().endsWith(".pdf")) {
				tempfilename += "\\" + summaryFileName + ".pdf";
			} else if (file.getFileName().endsWith(".xlsx")) {
				tempfilename += "\\" + summaryFileName + ".xlsx";
			} else {
				tempfilename += "\\" + summaryFileName + ".xls";
			}
		}

		logger.info("FileName:" + tempfilename);
		try {
			// if (file.getFileName()
			fileOutput = new FileOutputStream(tempfilename);
			fileOutput.write(file.getFileData());
			fileOutput.flush();
			fileOutput.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			logger.error("IO:" + e.getMessage());
		}
		return tempfilename;
	}


	public String saveAchievementFile2Local(ProcessInspectForm form, List<MsgInfo> errors, boolean isTemp) {

		ProcessInspectForm upfileForm = (ProcessInspectForm) form;
		// 取得上传的文件
		FormFile file = upfileForm.getUploadAchievementFile();
		FileOutputStream fileOutput;

		if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
			return "";
		}

		Date today = new Date();
		String tempfilename;
		if (isTemp) {
			tempfilename = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");
		} else {
			tempfilename = PathConsts.BASE_PATH + PathConsts.REPORT + "\\process_inspect\\" + upfileForm.getProcess_inspect_key();
		}

		File fMonthPath = new File(tempfilename);
		if (!fMonthPath.exists()) {
			fMonthPath.mkdirs();
		}
		fMonthPath = null;

		if (isTemp) {
			tempfilename += "\\" + today.getTime() + file.getFileName();
		} else {
			final String summaryFileName = String.format("QE0701-2 作业检查实绩表(%s)", form.getProcess_name());

			if (file.getFileName().endsWith(".pdf")) {
				tempfilename += "\\" + summaryFileName + ".pdf";
			} else if (file.getFileName().endsWith(".xlsx")) {
				tempfilename += "\\" + summaryFileName + ".xlsx";
			} else {
				tempfilename += "\\" + summaryFileName + ".xls";
			}
		}

		logger.info("FileName:" + tempfilename);
		try {
			// if (file.getFileName()
			fileOutput = new FileOutputStream(tempfilename);
			fileOutput.write(file.getFileData());
			fileOutput.flush();
			fileOutput.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			logger.error("IO:" + e.getMessage());
		}
		return tempfilename;
	}

	private String searchOperatorId(OperatorMapper operDao, String operatorName) {
		// 取操作者ID
		OperatorEntity operator = new OperatorEntity();
		operator.setName(operatorName);
		List<OperatorNamedEntity> entitys = operDao.searchOperator(operator);
		if (entitys != null && entitys.size() > 0) {
			return entitys.get(0).getOperator_id();
		}

		return null;
	}

	private String searchLineId(LineMapper lineDao, String lineName) {
		LineEntity line = new LineEntity();
		line.setName(lineName);
		List<LineEntity> entities = lineDao.searchLine(line);
		String lineId = null;
		for (LineEntity entity : entities) {
			if (lineId == null){
				lineId = entity.getLine_id();
			} else {
				if (entity.getLine_id().compareTo(lineId) < 0) {
					lineId = entity.getLine_id();
				}
			}
		}

		return lineId;
	}

	/**
	 * 根据单元格不同属性返回字符串
	 *
	 * @param cell
	 *            Excel单元格
	 * @return String 单元格数据内容
	 */
	private String getCellStringValue(HSSFCell cell) {
		if (cell == null) {
	           return "";
	    }
		String strCell = "";
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			strCell = cell.getStringCellValue();
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				strCell = DateUtil.toString(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()), DateUtil.DATE_PATTERN);
			} else {
				boolean isDateFormat = false;
				short dfCode = cell.getCellStyle().getDataFormat();
				switch (dfCode) {
				case 0xe:
				case 0xf:
				case 0x10:
				case 0x11:
				case 0x16:
				case 0x3a:
				case 0xb9:
				case 0xba:
				case 0xbb:
					isDateFormat = true;
					break;
				}
				if (!isDateFormat) {
					String fText = HSSFDataFormat.getBuiltinFormat(dfCode);
					if (fText != null && fText.indexOf("日") >= 0) {
						isDateFormat = true;
					}
				}
				if (!isDateFormat) {
					String fText = cell.getCellStyle().getDataFormatString();
					if (fText != null && fText.indexOf("日") >= 0) {
						isDateFormat = true;
					}
				}
				if (isDateFormat) {
					strCell = DateUtil.toString(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()), DateUtil.DATE_PATTERN);
				} else {
					strCell = String.valueOf((int) cell.getNumericCellValue());
				}
			}
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			strCell = String.valueOf(cell.getBooleanCellValue());
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			strCell = "";
			break;
		default:
			strCell = "";
			break;
		}

		if (strCell.equals("") || strCell == null) {
            return "";
        }

		return strCell;
	}

	private CellRangeAddress checkCombineCell(List<CellRangeAddress> ranges, int row, int column, boolean includeOwner) {
		for (CellRangeAddress range : ranges) {
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();

//			System.out.println(row + "," + column + ":(" + firstRow + "," + firstColumn + ")" + "(" + lastRow + "," + lastColumn + ")");

			if (includeOwner) {
				if (row >= firstRow && row <= lastRow) {
	                if (column >= firstColumn && column <= lastColumn) {
	                	return range;
	                }
				}
			} else {
				if (row > firstRow && row <= lastRow) {
	                if (column >= firstColumn && column <= lastColumn) {
	                	return range;
	                }
				}
			}
		}

		return null;
	}

	private List<CellRangeAddress> getCombineCells(HSSFSheet sheet) {
		List<CellRangeAddress> res = new ArrayList<>();

		int cnt = sheet.getNumMergedRegions();
		for (int i = 0; i < cnt; i++) {
			res.add(sheet.getMergedRegion(i));
		}

		return res;
	}

	/**
	 * 文件流输出
	 * @param res 输出目标相应
	 * @param contentType 输出上下文类型
	 * @param fileName 输出文件名
	 * @param filePath 数据源文件
	 * @throws Exception
	 */
	public void outputFile(HttpServletResponse res, String contentType, String fileName, String filePath) throws Exception {
		res.setHeader("Content-Disposition","attachment;filename=\""+fileName + "\"");
		res.setContentType(contentType);
		File file = new File(filePath);
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();

		OutputStream os = new BufferedOutputStream(res.getOutputStream());
		os.write(buffer);
		os.flush();
		os.close();
	}
	
	
	/**
	 * 删除作业监察实绩
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void deleteAchievement(ActionForm form,SqlSessionManager conn) throws Exception {
		ProcessInspectMapper processInspectMapper = conn.getMapper(ProcessInspectMapper.class);
		ProcessInspectConfirmMapper processInspectConfirmMapper = conn.getMapper(ProcessInspectConfirmMapper.class);
		
		//拷贝数据
		ProcessInspectAchievementEntity entity = new ProcessInspectAchievementEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//监察作业KEY
		String processInspectKey = entity.getProcess_inspect_key();
		//作业名称
		String processName = entity.getProcess_name();
		
		//删除作业监察实绩
		processInspectMapper.deleteAchievementByName(processInspectKey, processName);
		//删除作业确认
		processInspectConfirmMapper.deleteConfirmByName(processInspectKey, processName);
		
		//删除文件
		String dirPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\process_inspect\\" + processInspectKey;
		File dir = new File(dirPath);
		
		//判断目录是否存在
		if(dir.isDirectory()){
			final String achievementFileName = String.format("QE0701-2 作业检查实绩表(%s)",processName);
			
			//获取所有文件
			File[] list = dir.listFiles();
			for (File f : list) {
				String fileName = f.getName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));

				if(fileName.equals(achievementFileName)){
					f.delete();
					logger.info("Delete File:" + f.getAbsolutePath());
				}
			}
		}
	}
}