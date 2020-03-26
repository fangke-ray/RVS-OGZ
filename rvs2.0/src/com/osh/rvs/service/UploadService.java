package com.osh.rvs.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.bean.partial.MaterialPartialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.XlsUtil;
import com.osh.rvs.form.UploadForm;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.master.PartialMapper;
import com.osh.rvs.mapper.partial.MaterialPartialMapper;
import com.osh.rvs.mapper.qf.AcceptanceMapper;
import com.osh.rvs.service.inline.ForSolutionAreaService;
import com.osh.rvs.service.qf.MaterialFactService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.Converter;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.copy.IntegerConverter;
import framework.huiqing.common.util.message.ApplicationMessage;

public class UploadService {

//	private static final int SUMMARY_FILE_COLS = 39;
	private static Logger log = Logger.getLogger(UploadService.class);

	/**
	 * 读取受理文件
	 * 
	 * @param tempfilename
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<MaterialForm> readFile(String tempfilename, SqlSession conn, List<MsgInfo> errors) {
		InputStream in = null;
		List<MaterialForm> retList = new ArrayList<MaterialForm>();
		try {
			in = new FileInputStream(tempfilename);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet sheet = work.getSheetAt(0);// 获取Sheet

			AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
			List<MaterialEntity> agreedDateList = dao.getAllAgreedDate();

			for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {
				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {
					MaterialForm lineform = new MaterialForm();

					String sorc_no = getCellStringValue(row.getCell(0));
					if (CommonStringUtil.isEmpty(sorc_no)) continue;

					lineform.setSorc_no(sorc_no);

					// 根据解析文件中的用户同意日期，对应数据库中未投线的维修对象，更新其客户同意日
					Date d_agreed_date = getCellDateValue(row.getCell(8));
					boolean matchSorcFlg = false;
					for (MaterialEntity agreedDateEntity : agreedDateList) {
						String db_sorc_no = agreedDateEntity.getSorc_no();
						Date db_agreed_date = agreedDateEntity.getAgreed_date();

						if (sorc_no.equals(db_sorc_no)) {
							boolean isAgreeChanged = false;
							if (d_agreed_date != null && db_agreed_date == null) {
								isAgreeChanged = true;
							} else if (d_agreed_date == null && db_agreed_date != null) {
								isAgreeChanged = true;
							} else if (d_agreed_date == null && db_agreed_date == null) {
								isAgreeChanged = false;
							} else if (d_agreed_date.compareTo(db_agreed_date) != 0) {
								isAgreeChanged = true;
							}
							if (isAgreeChanged) {
								MaterialEntity updEntity = new MaterialEntity();
								updEntity.setSorc_no(sorc_no);
								updEntity.setAgreed_date(d_agreed_date);

								Integer level = agreedDateEntity.getLevel();
								if (level == null) {
									String sapLevel = getCellStringValue(row.getCell(13)); // OSH报价等级
									sapLevel = reverLevel(sapLevel);
									Converter<Integer> ic = IntegerConverter.getInstance();
									level = ic.getAsObject(sapLevel);
								}
								updEntity.setLevel(level);

								// 计算同意日->预定纳期
								Date dSchedulePlan = RvsUtils.getTimeLimit(d_agreed_date, 
										level, null, conn, false)[0];
								updEntity.setScheduled_date(dSchedulePlan);
								dao.updateAgreedDate(updEntity);
							}
							matchSorcFlg = true;
							break;
						}
					}
					if (matchSorcFlg) {
						continue;
					}

					// 检查维修对象是否存在无论在修
					MaterialEntity entity = new MaterialEntity();
					entity.setSorc_no(sorc_no);
					// 存在
					String hitMaterailId = dao.checkSorcNoUsed(entity);
					if (hitMaterailId != null) {
						continue;
					}

					String status = getCellStringValue(row.getCell(3)); // 修理品物流状态
					if (status.contains("返品")) {
						continue;
					}

					String model_name = getCellStringValue(row.getCell(5)); // 型号
					lineform.setModel_name(model_name);
					if (CommonStringUtil.isEmpty(model_name)) {
						continue;
					}
					String model_id = ReverseResolution.getModelByName(model_name, conn);
					if (CommonStringUtil.isEmpty(model_id)) {
						MsgInfo info = new MsgInfo();
						info.setErrcode("model.notExist");
						info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("model.notExist", model_name));
						errors.add(info);
						continue;
					}
					lineform.setModel_id(model_id);

					String serial_no = getCellStringValue(row.getCell(6)); // 机身编号
					if (CommonStringUtil.isEmpty(serial_no)) {
						MsgInfo info = new MsgInfo();
						info.setErrcode("validator.required");
						info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "第" + iRow
								+ "行机身号"));
						errors.add(info);
						continue;
					}
					lineform.setSerial_no(serial_no);

					String customer_name = getCellStringValue(row.getCell(7));
					if (customer_name != null) customer_name = customer_name.trim();
					lineform.setCustomer_name(customer_name); // 医院名称
					if (customer_name.startsWith("OCM")) {
						lineform.setService_repair_flg("3"); // 备品
					}

					lineform.setAgreed_date(DateUtil.toString(d_agreed_date, DateUtil.DATE_PATTERN)); // 用户同意日期
					String ocm_rank = getCellStringValue(row.getCell(10)); // OCM报价等级
					if (CommonStringUtil.isEmpty(ocm_rank)) {
						ocm_rank = getCellStringValue(row.getCell(9));
					}
					lineform.setOcm_rank(reverOcmRank(ocm_rank));

					String selectable = getCellStringValue(row.getCell(11)); // 选择性修理
					if (!CommonStringUtil.isEmpty(selectable.trim())) {
						lineform.setSelectable("1");
					}
					String level = getCellStringValue(row.getCell(13)); // OSH报价等级
					if (CommonStringUtil.isEmpty(level)) {
						level = getCellStringValue(row.getCell(12));
					}
					lineform.setLevel(reverLevel(level));
					lineform.setArea(reverArea(getCellStringValue(row.getCell(14)))); // 销售大区
					lineform.setBound_out_ocm(reverDirectArea(getCellStringValue(row.getCell(15)))); // 省
					String ocm_deliver_date = DateUtil.toString(getCellDateValue(row.getCell(16)), DateUtil.DATE_PATTERN);
					lineform.setOcm_deliver_date(ocm_deliver_date); // OCM出库日期
					String osh_deliver_date = DateUtil.toString(getCellDateValue(row.getCell(17)), DateUtil.DATE_PATTERN);
					lineform.setOsh_deliver_date(osh_deliver_date); // OSH发送日期
					if (CommonStringUtil.isEmpty(ocm_deliver_date) && CommonStringUtil.isEmpty(osh_deliver_date)) {
						continue;
					}
					lineform.setOcm(reverOcm(getCellStringValue(row.getCell(36)))); // 销售组织

					lineform.setMaterial_id("Line" + iRow);
					lineform.setFix_type("1");

					// 导入数据一览
					retList.add(lineform);
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
		return retList;
	}

	@SuppressWarnings("unused")
	private String parseNumJavaFormat(String getValue) {
		return parseNumFormat(getValue, 2);
	}
	private String parseNumFormat(String getValue, int fix) {
		if (getValue.matches("^\\d+$")) {
			int diff = Integer.parseInt(getValue) - fix;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 1900);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DATE, 1);

			cal.add(Calendar.DATE, diff);
			return DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN);
		} else {
			return getValue;
		}
	}
	/**
	 * Excel 日期格式 数值型转换
	 * 
	 * @param getValue
	 * @return
	 */
	private String parseNumFormat(String getValue) {
		return parseNumFormat(getValue, 1);
	}

	/**
	 * 读取客户同意日/Unrepaired
	 * 
	 * @param tempfilename
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<MaterialForm> readAgreed(String tempfilename, SqlSession conn, List<MsgInfo> errors) {
		ActiveXComponent xl = new ActiveXComponent("Excel.Application");
		// 不可见
		xl.setProperty("Visible", new Variant(false));
		// 不弹信息
		xl.setProperty("DisplayAlerts", new Variant(false));

		List<MaterialForm> retList = new ArrayList<MaterialForm>();

		try {
			log.info("Xls=StartG " + tempfilename);
			ComThread.InitSTA();

			Dispatch workbooks = xl.getProperty("Workbooks").toDispatch();
			Dispatch workbook = Dispatch.invoke(workbooks, "Open", Dispatch.Method, new Object[] { tempfilename, // .replaceAll("/",
																													// "\\\\")
					new Variant(false), new Variant(false) }, new int[1]).toDispatch();

			Dispatch sheets = Dispatch.call(workbook, "Worksheets").toDispatch();
			Dispatch sheet;
			try {
				sheet = Dispatch.call(sheets, "Item", "Sheet1").toDispatch();
			} catch (Exception e) {
				sheet = Dispatch.get(workbook, "ActiveSheet").toDispatch();
			}
			Dispatch.call(sheet, "Select");

			Dispatch usedRange = Dispatch.call(sheet, "UsedRange").toDispatch();
			Dispatch rows = Dispatch.get(usedRange, "Rows").toDispatch();
			int rowSize = Dispatch.get(rows, "Count").getInt();

			Dispatch columns = Dispatch.get(usedRange, "Columns").toDispatch();
			int columnSize = Dispatch.get(columns, "Count").getInt();

			log.info("rowSize= " + rowSize);
			log.info("columnSize= " + columnSize);

			if (columnSize < 16) {
			} else { // 34 流水线
				MaterialFactService mfService = new MaterialFactService();

				for (int i = 2; i <= rowSize; i++) {
					MaterialForm lineform = new MaterialForm();
					boolean changed = false;
					String sSorc_no = GetValue(sheet, "A" + i);
					if (CommonStringUtil.isEmpty(sSorc_no)) {
						// 可投线必须有SORC NO.
						continue;
					}
					lineform.setSorc_no(sSorc_no);

					String sAgreed_date = parseNumFormat(GetValue(sheet, "AC" + i)); // Z + 1 // AA + 2
					if (!CommonStringUtil.isEmpty(sAgreed_date)) {
						// 只需要有同意日的
						mfService.updateAgreedDateBySorc(sSorc_no,
								DateUtil.toDate(sAgreed_date, DateUtil.DATE_PATTERN), conn);
						lineform.setAgreed_date(sAgreed_date);
						changed = true;
						// 未修理返送的同意日
						String sUnrepair = parseNumFormat(GetValue(sheet, "G" + i));
						if (sUnrepair.toLowerCase().startsWith("unrepair")) {
							mfService.updateUnrepairBySorc(sSorc_no, null, conn);
						}
					}

					if (changed)
						retList.add(lineform);
				}
			}

			Variant f = new Variant(false);
			Dispatch.call(workbook, "Close", f);
		} catch (Exception e) {
			log.error("Exception= ", e);
			return null;
		} finally {
			xl.invoke("Quit", new Variant[] {});
			ComThread.Release();
		}
		return retList;
	}


	private static Map<String, String> reverOcmMap = new HashMap<String, String>();

	private String reverOcm(String gValue) {
		if (!reverOcmMap.containsKey(gValue)) {
			reverOcmMap.put(gValue, CodeListUtils.getKeyByValue("material_ocm", gValue, ""));
		}
		return reverOcmMap.get(gValue);
	}

	private static Map<String, String> reverLevelMap = new HashMap<String, String>();

	private String reverLevel(String gValue) {
		if (gValue == null) return null;
		gValue = gValue.replaceAll("W$", "");
		if (!reverLevelMap.containsKey(gValue)) {
			reverLevelMap.put(gValue, CodeListUtils.getKeyByValue("material_level", gValue, ""));
		}
		return reverLevelMap.get(gValue);
	}

	private static Map<String, String> reverOcmRankMap = new HashMap<String, String>();

	private String reverOcmRank(String gValue) {
		if (!reverOcmRankMap.containsKey(gValue)) {
			String code = CodeListUtils.getKeyByValue("material_ocm_direct_rank", gValue, "");
			if ("".equals(code)) {
				code = CodeListUtils.getKeyByValue("material_level", gValue, "");
			}
			reverOcmRankMap.put(gValue, code);
		}
		return reverOcmRankMap.get(gValue);
	}

	private static Map<String, String> reverAreaMap = new HashMap<String, String>();

	private String reverArea(String gValue) {
		if (gValue != null && gValue.length() > 2) gValue = gValue.substring(0, 2);
		if (!reverAreaMap.containsKey(gValue)) {
			reverAreaMap.put(gValue, CodeListUtils.getKeyByValue("material_large_area", gValue, ""));
		}
		return reverAreaMap.get(gValue);
	}

	private static Map<String, String> reverDirectAreaMap = new HashMap<String, String>();

	private String reverDirectArea(String gValue) {
		if (!reverDirectAreaMap.containsKey(gValue)) {
			String gCode = CodeListUtils.getKeyByValue("material_direct_area", gValue, null);
			if (gCode == null) {
				gCode = CodeListUtils.getKeyByValue("material_direct_area_extra", gValue, "");
				if (gCode.length() > 2) {
					gCode = gCode.substring(0, 2);
				}
			}
			reverDirectAreaMap.put(gValue, gCode);
		}
		return reverDirectAreaMap.get(gValue);
	}

	// 读取值
	private static String GetValue(Dispatch sheet, String position) {
		Variant cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { position }, new int[1]);

		String value = Dispatch.get(cell.toDispatch(), "Value").toString();

		// TODO IsNumeric
		value = value.replaceAll("(\\d*)\\.0$", "$1");
		// TODO IsDate
		if (value.indexOf(" CST ") >= 0) {
			Date javaDate = Dispatch.get(cell.toDispatch(), "Value").getJavaDate();
			value = DateUtil.toString(javaDate, DateUtil.DATE_PATTERN);
		}
		if ("null".equals(value))
			value = "";
		return value;
	}

	public String readFileName(String uploadMonth, String filepath) {
		String filename = PathConsts.BASE_PATH + PathConsts.REPORT + "\\" + filepath + "\\" + uploadMonth
				+ "\\confirm\\";

		File fMonthPath = new File(filename);
		if (!fMonthPath.exists()) {
			fMonthPath.mkdirs();
		}
		fMonthPath = null;

		return filename;
	}

	/**
	 * 获取单元格数据
	 * 
	 * @param cell
	 *            Excel单元格
	 * @return String 单元格数据内容
	 */
	@SuppressWarnings("unused")
	private String getStringCellValue(HSSFCell cell) {
		String strCell = "";
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			strCell = cell.getStringCellValue();
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			strCell = String.valueOf(cell.getBooleanCellValue());
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			Double dValue = cell.getNumericCellValue();
			strCell = "" + dValue.intValue();
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			strCell = "";
			break;
		default:
			strCell = "";
			break;
		}
		return strCell;
	}

	/**
	 * 按行读取零件签收文件
	 * 
	 * @param fileName
	 * @param checkT 
	 * @return list
	 */
	public Map<String, Map<String, Integer>> readFileByLines(String fileName, Map<String, Map<String, Integer>> materialMap, boolean checkT, SqlSession conn, List<MsgInfo> errors) {
		File file = new File(fileName);
		String code = RvsUtils.getFileCode(fileName);// 取得文件编码

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
			String tempString = null;
			String currentSorcNO = "";
			String curMaterialId = "";
			String curPartialId = "";
			String curoccurTimes="";
			Map<String, Integer> partialMap = null;
			int idx = 0;
			while ((tempString = reader.readLine()) != null) {
				if (idx == 0) {// 去除表头
					idx++;
					continue;
				}

				String[] arr = tempString.split("\t");// 制表格分割

				if (arr.length < 8)
					continue;

				String cell1value = arr[0];
				if (cell1value == null)
					continue;
				String[] sorcAndOccurTimes = getOccurTimes(trimSorc(trimQuote(cell1value)));

				if (sorcAndOccurTimes == null)
					continue;

				if (checkT && !trimQuote(cell1value).toUpperCase().matches(".*-T[0-9]{0,}$")) { // {0,1}
					continue;
				}

				String occur_times = sorcAndOccurTimes[1];// 订购次数

				String sorc_no = trimSorc(sorcAndOccurTimes[0]);
				if (!(currentSorcNO.equals(sorc_no) && curoccurTimes.equals(occur_times))) {// 维修对象ID
					currentSorcNO = sorc_no;
					curoccurTimes=occur_times;
					MaterialEntity entity = new MaterialEntity();
					MaterialMapper dao = conn.getMapper(MaterialMapper.class);
					entity.setSorc_no(currentSorcNO);
					entity.setBreak_back_flg(0); // 无返还
					entity.setFind_history("1"); // 未出货

					List<String> listTemp = dao.searchMaterialIds(entity);

					if (listTemp.size() == 0) {
						// buffer.append("维修对象" + currentSorcNO + "不存在或已经出货\t");
						continue;
					}
					String material_id = listTemp.get(0);
					partialMap = new HashMap<String, Integer>();
					curMaterialId = material_id;
					materialMap.put(curMaterialId + curoccurTimes, partialMap);
				}

				String quantity = replaceStr(arr[7]);// 订购数量
				if (CommonStringUtil.isEmpty(quantity)) {
					continue;
				}
				Integer iQuantity = Integer.parseInt(quantity);

				String partialCode = replaceStr(arr[3]);// 零件品名
				PartialEntity pEntity = new PartialEntity();
				pEntity.setCode(partialCode);
				PartialMapper pDao = conn.getMapper(PartialMapper.class);
				List<String> pList = pDao.checkPartial(pEntity);
				if (pList.size() == 0) {
					buffer.append("零件" + partialCode + "不存在\t");
					continue;
				}
				String partial_id = pList.get(0);
				if (!curPartialId.equals(partial_id)) {
					curPartialId = partial_id;
				}

				if (partialMap.containsKey(curPartialId)) {
					Integer beforeQuantity = partialMap.get(curPartialId);
					iQuantity = iQuantity + beforeQuantity;
					partialMap.put(curPartialId, iQuantity);
					continue;
				}
				partialMap.put(curPartialId, iQuantity);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(buffer.toString().isEmpty()){
			}else{
				MsgInfo info=new MsgInfo();
				info.setErrmsg(buffer.toString());
				errors.add(info);
			}
			log.info("========" + buffer.toString());
		}
		return materialMap;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public String replaceStr(String str) {
		String tempStr = "";
		if (!CommonStringUtil.isEmpty(str)) {
			tempStr = str.replace("\"", "");
		}
		return tempStr;
	}

	/**
	 * 获取Sorc_no和订购次数
	 * 
	 * @param str
	 * @return
	 */
	public String[] getOccurTimes(String str) {
		String tempStr = "";
		String[] tempStrs = null;
		if (!CommonStringUtil.isEmpty(str)) {
			if(str.contains("\"")){		//含有引号
				tempStr = str.replace("\"", "");
				if (tempStr.contains("/")) {
					tempStrs = tempStr.split("/");
				} else {
					tempStrs = new String[2];
					tempStrs[0] = tempStr;
					tempStrs[1] = "1";
				}
			}else{
				if (str.contains("/")) {
					tempStrs = str.split("/");
				} else {
					tempStrs = new String[2];
					tempStrs[0] = str;
					tempStrs[1] = "1";
				}
			}
		}
		return tempStrs;
	}

	/**
	 * 获取Sorc_no
	 * 
	 * @param srcSorc
	 * @return
	 */
	public static String trimSorc(String srcSorc) {
		String retSorc = srcSorc.replaceAll("-.{1}[0-9]{0,}$", ""); // {0,1}
		if (srcSorc.equals(retSorc)) {
			return retSorc;
		} else {
			return trimSorc(retSorc);
		}
	}
	
	public static String trimQuote(String srcSorc) {
		return srcSorc.replace("\"", "");
	}	




	/**
	 * 根据单元格不同属性返回字符串
	 * 
	 * @param cell
	 *            Excel单元格
	 * @return String 单元格数据内容
	 */
	public static String getCellStringValue(HSSFCell cell) {
		if (cell == null) {
	           return "";
	    }
		String strCell = "";
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			strCell = cell.getStringCellValue();
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			strCell = String.valueOf((int) cell.getNumericCellValue());
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

	/**
	 * 根据单元格不同属性返回日期
	 * 
	 * @param cell
	 *            Excel单元格
	 * @return String 单元格数据内容
	 */
	private static Date getCellDateValue(HSSFCell cell) {
		if (cell == null) {
	           return null;
	    }
		Date dtCell = null;
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING:
			dtCell = DateUtil.toDate(cell.getStringCellValue(), DateUtil.DATE_PATTERN);
			break;
		case HSSFCell.CELL_TYPE_NUMERIC:
			dtCell = cell.getDateCellValue();
			break;
		default:
			dtCell = null;
			break;
		}
		
		return dtCell;
	}

	/**
	 * 读取BO零件入库预定日文件
	 * @param fileName
	 * @param conn
	 * @param errors
	 */
	public Map<String, Map<String, Date>> readBoExcel(String fileName, SqlSessionManager conn, List<MsgInfo> errors){
		InputStream in = null;
		Map<String, Map<String, Date>> materialMap = new HashMap<String, Map<String, Date>>();
		StringBuffer buffer=new StringBuffer();
		try {
			in = new FileInputStream(fileName);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet sheet = work.getSheetAt(0);// 获取Sheet
			Map<String, Date> partialMap = null;

			Set<String> notRxitsSorcs = new HashSet<String>();
			for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {
				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {
					if(row.getLastCellNum()<9)
						continue;

					// 入库预定日列
					Date strDate=getCellDateValue(row.getCell(6));
					if(strDate == null) {
						break; // continue;
					}
					
					String[] sorcAndOccurTimes = getOccurTimes(getCellStringValue(row.getCell(0)));
					if (sorcAndOccurTimes == null)
						continue;
					
					String occur_times = sorcAndOccurTimes[1];// 订购次数
					String sorc_no = trimSorc(sorcAndOccurTimes[0]);//修理单号
					sorc_no = sorc_no.toUpperCase();

					MaterialEntity entity = new MaterialEntity();
					MaterialMapper dao = conn.getMapper(MaterialMapper.class);
					entity.setSorc_no(sorc_no);
					entity.setBreak_back_flg(0); // 无返还
					entity.setFind_history("1"); // 未出货

					if (notRxitsSorcs.contains(sorc_no)) {
						continue;
					}
					
					List<String> listTemp = dao.searchMaterialIds(entity);
					if(listTemp.size()==0){
						notRxitsSorcs.add(sorc_no);//放入Set集合中
//							buffer.append("维修对象"+currentSorcNO+"不存在\n");
						log.info("维修对象 "+sorc_no+" 不存在");
						continue;
					}
					String material_id = listTemp.get(0);

					partialMap = materialMap.get(material_id + occur_times);

					if (partialMap == null) {
						partialMap = new HashMap<String, Date>();
						materialMap.put(material_id + occur_times, partialMap);
						partialMap = materialMap.get(material_id + occur_times);
					}

					String partialCode =getCellStringValue(row.getCell(8));// 零件品名
					if(CommonStringUtil.isEmpty(partialCode)){
						continue;
					}
					PartialEntity pEntity = new PartialEntity();
					pEntity.setCode(partialCode);
					PartialMapper pDao = conn.getMapper(PartialMapper.class);
					List<String> pList = pDao.checkPartial(pEntity);
					if (pList.size() == 0) {
						buffer.append("零件 "+partialCode+" 不存在\n");
						log.info("零件 "+partialCode+" 不存在");
						continue;
					}
					String partial_id = pList.get(0);

					log.info(sorc_no + "/" + occur_times + " c " + partial_id + ":" + strDate);
					partialMap.put(partial_id, strDate);
				}
			}
			
			if(buffer.toString().isEmpty()){
			}else{
				MsgInfo info=new MsgInfo();
				info.setErrmsg(buffer.toString());
				errors.add(info);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return materialMap;
	}
	
	
	/**
	 * 判断是否是数字
	 * @param str
	 * @return
	 */
	public static boolean isNum(String str){
		 Pattern pattern = Pattern.compile("[0-9]+");
		 Matcher isNum = pattern.matcher(str);
	     if( !isNum.matches() ){
              return false;
         }
         return true;
	}
	
	public String getFile2Local(ActionForm form, List<MsgInfo> errors) {
		//
		UploadForm upfileForm = (UploadForm) form;
		// 取得上传的文件
		FormFile file = upfileForm.getFile();
		FileOutputStream fileOutput;

		if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
			return "";
		}
		Date today = new Date();
		String tempfilename = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");

		File fMonthPath = new File(tempfilename);
		if (!fMonthPath.exists()) {
			fMonthPath.mkdirs();
		}
		fMonthPath = null;

		tempfilename += "\\" + today.getTime() + file.getFileName();

		log.info("FileName:" + tempfilename);
		try {
			// if (file.getFileName()
			fileOutput = new FileOutputStream(tempfilename);
			fileOutput.write(file.getFileData());
			fileOutput.flush();
			fileOutput.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			log.error("IO:" + e.getMessage());
		}
		return tempfilename;
	}

	public List<String> getFiles2Local(ActionForm form, List<MsgInfo> errors) {
		List<String> tempFileNames = new ArrayList<String>();

		UploadForm upfileForm = (UploadForm) form;
		// 取得上传的文件
		List<FormFile> files = upfileForm.getFiles();

		if(files.size() == 0){
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
			return null;
		}

		FileOutputStream fileOutput;

		for (FormFile file : files) {
			if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.notExist");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
				errors.add(error);
				return null;
			}

			Date today = new Date();
			String tempfilename = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM");

			File fMonthPath = new File(tempfilename);
			if (!fMonthPath.exists()) {
				fMonthPath.mkdirs();
			}
			fMonthPath = null;

			tempfilename += "\\" + today.getTime() + file.getFileName();

			log.info("FileName:" + tempfilename);
			try {
				fileOutput = new FileOutputStream(tempfilename);
				fileOutput.write(file.getFileData());
				fileOutput.flush();
				fileOutput.close();
			} catch (FileNotFoundException e) {
				log.error("FileNotFound:" + e.getMessage());
			} catch (IOException e) {
				log.error("IO:" + e.getMessage());
			}

			tempFileNames.add(tempfilename);
		}

		return tempFileNames;
	}

	public static String toXls2003(String path) {
		XlsUtil xlsUtil = null;
		try {
			String target = path.replaceAll("\\.xlsx", ".xls");
			xlsUtil = new XlsUtil(path);
			xlsUtil.SaveAsXls2003(target);
			return target;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return path;
		} finally {
			if (xlsUtil != null)
				xlsUtil.Release();
		}
	}

	/**
	 * 读取受理文件
	 * 
	 * @param tempfilename
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<MaterialPartialEntity> readPartOrderFile(String tempfilename, LoginData logindata, SqlSessionManager conn, List<MsgInfo> errors) {
		InputStream in = null;
		List<MaterialPartialEntity> retList = new ArrayList<MaterialPartialEntity>();
		ForSolutionAreaService fsoService = new ForSolutionAreaService();
		try {
			in = new FileInputStream(tempfilename);// 读取文件
			HSSFWorkbook work = new HSSFWorkbook(in);// 创建Excel
			HSSFSheet sheet = work.getSheetAt(0);// 获取Sheet

			MaterialPartialMapper mpMapper = conn.getMapper(MaterialPartialMapper.class);
			List<MaterialPartialEntity> orderPartList = mpMapper.getAllWorkingMaterialPartail();
			List<MaterialPartialEntity> insertEntities = new ArrayList<MaterialPartialEntity>();
			List<MaterialPartialEntity> updateEntities = new ArrayList<MaterialPartialEntity>();

			for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {

				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {

					// 通知单
					String sorc_no = getCellStringValue(row.getCell(0));
					if (CommonStringUtil.isEmpty(sorc_no)) {
						break;
					}

					String status = getCellStringValue(row.getCell(7)); // 总览状态2
					if (CommonStringUtil.isEmpty(status) || status.contains("返品")) {
						continue;
					}

					// 检查维修对象是否存在并且在修
					MaterialPartialEntity mEntity = checkMaterailForPartOrder(sorc_no,  orderPartList);
					if (mEntity == null) {
						continue;
					}

					boolean rvsInLined = (mEntity.getInline_time() != null);
//					if (sapInLined ^ rvsInLined) {
//						log.error("投线状态" + rvsInLined);
//					}

					// BO 内容
					String boContent = getCellStringValue(row.getCell(10)); // K列

					Integer boFlg = null;
					if (rvsInLined) {
						if (CommonStringUtil.isEmpty(boContent)) {
							boFlg = 0;
						} else {
							boFlg = 1;
						}
					} else {
						boFlg = 7;
					}

					Integer rvsBoFlg = mEntity.getBo_flg();

					// 预提状态
					if (boFlg == 7) {

						// 新建订单数据
						if (rvsBoFlg == null) {
							MaterialPartialEntity ist = new MaterialPartialEntity();
							ist.setMaterial_id(mEntity.getMaterial_id());
							ist.setOccur_times(1);
							ist.setBo_flg(7);
							if (!CommonStringUtil.isEmpty(boContent)) {
								Map<String, String> jsonBoContents= new HashMap<String, String>();
								jsonBoContents.put("00000000000", boContent);
								ist.setBo_contents(JSON.encode(jsonBoContents));
							}
							insertEntities.add(ist);
						} else {
							if (CommonStringUtil.isEmpty(boContent)) {
								// 零件BO解决
								if (mEntity.getBo_contents() != null) {
									MaterialPartialEntity upd = new MaterialPartialEntity();
									upd.setMaterial_id(mEntity.getMaterial_id());
									upd.setOccur_times(1);
									updateEntities.add(upd);
								}
							} else {
								// 总之更新
								MaterialPartialEntity upd = new MaterialPartialEntity();
								upd.setMaterial_id(mEntity.getMaterial_id());
								upd.setOccur_times(1);
								Map<String, String> jsonBoContents= new HashMap<String, String>();
								jsonBoContents.put("00000000000", boContent);
								upd.setBo_contents(JSON.encode(jsonBoContents));

								updateEntities.add(upd);
							}
						}
					// 有BO
					} else if (boFlg == 1) {
						// 总之更新
						MaterialPartialEntity upd = new MaterialPartialEntity();
						upd.setMaterial_id(mEntity.getMaterial_id());
						upd.setOccur_times(mEntity.getOccur_times());
						upd.setBo_flg(1);

						Map<String, String> jsonBoContents= new HashMap<String, String>();
						jsonBoContents.put("00000000000", boContent);
						upd.setBo_contents(JSON.encode(jsonBoContents));

						if (rvsBoFlg != 1) {
							// 进待处理区
							upd.setBo_contents_new(boContent);
							upd.setOver_state(1);
						}

						updateEntities.add(upd);
					// 无BO
					} else if (boFlg == 0) {
						if (rvsBoFlg == 1) {
							MaterialPartialEntity upd = new MaterialPartialEntity();
							upd.setMaterial_id(mEntity.getMaterial_id());
							upd.setOccur_times(mEntity.getOccur_times());

							Date orderDate = mEntity.getOrder_date();
							if (orderDate != null && DateUtil.compareDate(orderDate, new Date()) == 0) {
								upd.setBo_flg(0); // 当日为无BO
							} else {
								upd.setBo_flg(2); // BO 解除
							}
							upd.setArrival_plan_date(new Date());

							// 出待处理区
							upd.setOver_state(-1);

							updateEntities.add(upd);
						}
					}
				}
			}

			for (MaterialPartialEntity entity : insertEntities) {
				mpMapper.insertMaterialPartial(entity);
			}
			for (MaterialPartialEntity entity : updateEntities) {
				mpMapper.updateMaterialPartialFromFile(entity);
				Integer changeState = entity.getOver_state();
				if (changeState != null && changeState == 1) {
					fsoService.doBoFsaEnter(entity.getMaterial_id(), entity.getBo_contents_new(), conn);
				}
				if (changeState != null && changeState == -1) {
					fsoService.doBoFsaLeave(entity.getMaterial_id(), logindata, conn);
				}
			}
			retList.addAll(insertEntities);
			retList.addAll(updateEntities);

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

		return retList;
	}

	private MaterialPartialEntity checkMaterailForPartOrder(String omr_notifi_no,
			List<MaterialPartialEntity> orderPartList) {
		boolean hit = false;
		List<MaterialPartialEntity> ret = new ArrayList<MaterialPartialEntity> ();
		for (MaterialPartialEntity me : orderPartList){
			if (omr_notifi_no.equals(me.getOmr_notifi_no())) {
				hit = true;
				ret.add(me);
			} else {
				if (hit) break;
			}
		}

		if (ret.size() == 0) 
			return null;
		else
			return ret.get(ret.size() - 1);
	}
}