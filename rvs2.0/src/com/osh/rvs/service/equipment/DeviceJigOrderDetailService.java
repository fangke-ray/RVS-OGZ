package com.osh.rvs.service.equipment;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.equipment.DeviceJigOrderDetailEntity;
import com.osh.rvs.bean.equipment.DeviceSpareAdjustEntity;
import com.osh.rvs.bean.equipment.DeviceSpareEntity;
import com.osh.rvs.common.CopyByPoi;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.equipment.DeviceJigOrderDetailForm;
import com.osh.rvs.mapper.equipment.DeviceJigOrderDetailMapper;
import com.osh.rvs.mapper.equipment.DeviceSpareAdjustMapper;
import com.osh.rvs.mapper.equipment.DeviceSpareMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 设备工具治具订单明细
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderDetailService {
	private Logger log = Logger.getLogger(getClass());
	
	/**数据新建标记**/
	private final String MODIFY_FLG_ADD = "add";
	
	/**数据删除标记**/
	private final String MODIFY_FLG_REMOVE = "remove";
	
	/**数据更新标记**/
	private final String MODIFY_FLG_CHANGE = "change";
	
	/**数据不变标记**/
	private final String MODIFY_FLG_UNCHANGED = "unchanged";
	
	/**OK**/
	private final String OK = "1";
	
	/**NG**/
	private final String NG = "0";

	/**
	 * 检索
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<DeviceJigOrderDetailForm> search(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 检索
		List<DeviceJigOrderDetailEntity> list = dao.search(entity);

		List<DeviceJigOrderDetailForm> respList = new ArrayList<DeviceJigOrderDetailForm>();

		// 拷贝数据
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderDetailForm.class);

		return respList;
	}

	/**
	 * 明细
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<DeviceJigOrderDetailForm> searchDetail(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 明细
		List<DeviceJigOrderDetailEntity> list = dao.searchDetail(entity);

		List<DeviceJigOrderDetailForm> respList = new ArrayList<DeviceJigOrderDetailForm>();

		// 拷贝数据
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderDetailForm.class);

		return respList;
	}

	public void update(HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		// 设备管理(设备管理画面)
		boolean isTechnology = false;
		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)) {
			isTechnology = true;
		}
		
		// 经理
		boolean isManager = false;
		if(privacies.contains(RvsConsts.PRIVACY_PROCESSING)){
			isManager = true;
		}

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		List<DeviceJigOrderDetailForm> list = new AutofillArrayList<DeviceJigOrderDetailForm>(DeviceJigOrderDetailForm.class);
		Map<String, String[]> parameters = req.getParameterMap();

		// 整理提交数据
		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("device_jig_order_detail".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);
					if ("confirm_flg_name".equals(column)) {
						list.get(icounts).setConfirm_flg_name(value[0]);
					} else if ("order_key".equals(column)) {
						list.get(icounts).setOrder_key(value[0]);
					} else if ("object_type".equals(column)) {
						list.get(icounts).setObject_type(value[0]);
					} else if ("object_type_name".equals(column)) {
						list.get(icounts).setObject_type_name(value[0]);
					} else if ("device_type_id".equals(column)) {
						list.get(icounts).setDevice_type_id(value[0]);
					} else if ("device_type_name".equals(column)) {
						list.get(icounts).setDevice_type_name(value[0]);
					} else if ("model_name".equals(column)) {
						list.get(icounts).setModel_name(value[0]);
					} else if ("system_code".equals(column)) {
						list.get(icounts).setSystem_code(value[0]);
					} else if ("name".equals(column)) {
						list.get(icounts).setName(value[0]);
					} else if ("order_from".equals(column)) {
						list.get(icounts).setOrder_from(value[0]);
					} else if ("quantity".equals(column)) {
						list.get(icounts).setQuantity(value[0]);
					} else if ("nesssary_reason".equals(column)) {
						list.get(icounts).setNesssary_reason(value[0]);
					} else if ("applicator_id".equals(column)) {
						list.get(icounts).setApplicator_id(value[0]);
					} else if ("applicator_operator_name".equals(column)) {
						list.get(icounts).setApplicator_operator_name(value[0]);
					}else if ("manage_comfirm_flg".equals(column)) {
						list.get(icounts).setManage_comfirm_flg(value[0]);
					}else if("applicate_date".equals(column)){
						list.get(icounts).setApplicate_date(value[0]);
					}
				}
			}
		}

		// 验证新建和修改的数据
		for (int i = 0; i < list.size(); i++) {
			DeviceJigOrderDetailForm form = list.get(i);
			String flg = form.getConfirm_flg_name();
			// 对象类别
			String objectType = form.getObject_type();
			
			String applicateDate = form.getApplicate_date();

			if (MODIFY_FLG_ADD.equals(flg) || MODIFY_FLG_CHANGE.equals(flg)) {
				Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
				v.add("order_key", v.required());
				v.add("object_type", v.required("对象类别"));

				if ("2".equals(objectType)) {// 治具
					v.delete("device_type_id");
				} else {
					v.add("device_type_id", v.required("品名"));
				}

				v.add("model_name", v.required("型号/规格"));
				v.add("name", v.required("名称"));
				
				if(CommonStringUtil.isEmpty(applicateDate)){
					v.add("quantity", v.required("数量"));
				}
				v.add("applicator_id", v.required("申请人"));

				if (isTechnology) {// 有PRIVACY_TECHNOLOGY权限者更新时，每行的"受注方"要作为必填项目检验
					v.add("order_from", v.required("受注方"));
				}
				List<MsgInfo> errs = v.validate();
				for (int j = 0; j < errs.size(); j++) {
					errs.get(j).setLineno("第" + (i + 1) + "行");
				}
				errors.addAll(errs);
			}else if(MODIFY_FLG_UNCHANGED.equals(flg)){
				Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
				if (isTechnology) {// 有PRIVACY_TECHNOLOGY权限者更新时，每行的"受注方"要作为必填项目检验
					v.add("order_from", v.required("受注方"));
				}
				
				List<MsgInfo> errs = v.validate();
				for (int j = 0; j < errs.size(); j++) {
					errs.get(j).setLineno("第" + (i + 1) + "行");
				}
				errors.addAll(errs);
			}
		}

		if (errors.size() > 0)
			return;

		// 数据重复check
		Map<String, String> keyMap = new HashMap<String, String>();
		for (int i = 0; i < list.size(); i++) {
			String rowNum = String.valueOf(i + 1);

			DeviceJigOrderDetailForm form = list.get(i);
			// 对象类别
			String objectType = form.getObject_type();

			// 品名ID
			String deviceTypeId = form.getDevice_type_id();

			// 型号/规格
			String modelName = form.getModel_name();

			// 申请人
			String applicatorId = form.getApplicator_id();

			String key = objectType + "/" + deviceTypeId + "/" + modelName + "/" + applicatorId;

			if (keyMap.containsKey(key)) {
				String prevRowNum = keyMap.get(key);
				prevRowNum = prevRowNum + "," + rowNum;
				keyMap.put(key, prevRowNum);
			} else {
				keyMap.put(key, rowNum);
			}
		}

		for (String key : keyMap.keySet()) {
			// 行号集合
			String[] arrRowNum = keyMap.get(key).split(",");

			if (arrRowNum.length > 1) {
				String msg = "";
				for (String rowNum : arrRowNum) {
					if (CommonStringUtil.isEmpty(msg)) {
						msg += "第" + rowNum + "行";
					} else {
						msg += ",第" + rowNum + "行";
					}
				}

				msg += "【对象类别】,【品名】,【型号/规格],【申请人】列数据重复。";

				MsgInfo error = new MsgInfo();
				error.setErrmsg(msg);
				errors.add(error);
			}
		}

		if (errors.size() > 0)
			return;

		// 修改标记
		int iModify = 0;
		
		// 经理确认标记
		int iManageConfirm = 0;

		for (DeviceJigOrderDetailForm form : list) {
			// 数据修改标记
			String modifyFlg = form.getConfirm_flg_name();
			
			// 经理确认标记
			String manageComfirmFlg = form.getManage_comfirm_flg();

			DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
			// 复制表单数据
			BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

			if (MODIFY_FLG_ADD.equals(modifyFlg)) {
				if (entity.getObject_type() == 2) {// 对象类别”为“治具”时，“品名”提交为0
					entity.setDevice_type_id("0");
				}
				
				if(OK.equals(manageComfirmFlg)){// OK
					// 申请日期为当前日期
					entity.setApplicate_date(Calendar.getInstance().getTime());
					// 新建设备工具治具订单明细
					dao.insert(entity);
					iManageConfirm++;
				} else {
					// 新建设备工具治具订单明细
					dao.insert(entity);
				}
				iModify++;
			} else if (MODIFY_FLG_REMOVE.equals(modifyFlg) || NG.equals(manageComfirmFlg)) {
				// 手动删除或者经理确认NG
				// 删除明细
				dao.delete(entity);
				iModify++;
			} else if (MODIFY_FLG_CHANGE.equals(modifyFlg)) {
				// 更新明细
				dao.update(entity);
				iModify++;
			} else if("".equals(modifyFlg)){
				// 不可编辑数据确认
				if(OK.equals(manageComfirmFlg)){// OK
					// 申请日期为当前日期
					entity.setApplicate_date(Calendar.getInstance().getTime());
					dao.updateApplicateDate(entity);
					
					iManageConfirm++;
				} else if(NG.equals(manageComfirmFlg)){
					dao.delete(entity);
					iModify++;
				}
			}
		}
		
		
		String orderNo = req.getParameter("order_no");

		//经理确认
		if(iManageConfirm > 0 && iModify == 0){
			//推送给经理设备管理员
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {
				String inUrl = "http://localhost:8080/rvspush/trigger/device_jig_order_applicate/" + orderNo + "/" + user.getOperator_id()+"/deviceManager";
				HttpGet request = new HttpGet(inUrl);
				log.info("finger:" + request.getURI());
				httpclient.execute(request, null);
			} catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		}
		
		if (!isManager && iModify > 0){
			// 推送给经理。
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {
				String inUrl = "http://localhost:8080/rvspush/trigger/device_jig_order_applicate/" + orderNo + "/" + user.getOperator_id()+"/manager";
				HttpGet request = new HttpGet(inUrl);
				log.info("finger:" + request.getURI());
				httpclient.execute(request, null);
			} catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		}
	}

	/**
	 * 查询所有未报价的[对象类别],[品名],[型号/规格]
	 * 
	 * @param conn
	 * @return
	 */
	public List<DeviceJigOrderDetailForm> searchInvoice(SqlSession conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		// 检索
		List<DeviceJigOrderDetailEntity> list = dao.searchInvoice();

		List<DeviceJigOrderDetailForm> respList = new ArrayList<DeviceJigOrderDetailForm>();

		// 拷贝数据
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderDetailForm.class);

		return respList;
	}

	/**
	 * 更新询价
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateInvoice(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新询价ID
		dao.updateInvoiceId(entity);
	}

	/**
	 * 查询未报价订单明细
	 * 
	 * @param conn
	 * @return
	 */
	public List<DeviceJigOrderDetailForm> searchUnQuotation(SqlSession conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		// 检索
		List<DeviceJigOrderDetailEntity> list = dao.searchUnQuotation();

		List<DeviceJigOrderDetailForm> respList = new ArrayList<DeviceJigOrderDetailForm>();

		// 拷贝数据
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceJigOrderDetailForm.class);

		return respList;
	}

	/**
	 * 更新报价
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateQuotation(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 更新询价ID
		dao.updateQuotationId(entity);
	}

	/**
	 * 更新确认结果
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateConfirm(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		entity.setRecept_date(new Date());

		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.updateConfirm(entity);
	}

	public DeviceJigOrderDetailForm getOrderDetail(ActionForm form, SqlSession conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity = dao.getOrderDetail(entity);

		DeviceJigOrderDetailForm respForm = new DeviceJigOrderDetailForm();
		BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);

		return respForm;
	}

	/**
	 * 验收
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateInlineRecept(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.updateInlineRecept(entity);
	}

	/**
	 * 预算
	 * 
	 * @param form
	 * @param conn
	 */
	public void updateBudget(ActionForm form, SqlSessionManager conn) {
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.updateBudget(entity);
	}

	/**
	 * 加入备品
	 * 
	 * @param form
	 * @param req
	 * @param conn
	 */
	public void addSpare(DeviceJigOrderDetailForm form, HttpServletRequest req, SqlSessionManager conn) {
		// 数据连接
		DeviceSpareMapper deviceSpareMapper = conn.getMapper(DeviceSpareMapper.class);
		DeviceSpareAdjustMapper deviceSpareAdjustMapper = conn.getMapper(DeviceSpareAdjustMapper.class);
		DeviceJigOrderDetailMapper deviceJigOrderDetailMapper = conn.getMapper(DeviceJigOrderDetailMapper.class);

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		DeviceSpareEntity deviceSpareEntity = new DeviceSpareEntity();
		BeanUtil.copyToBean(form, deviceSpareEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		DeviceSpareEntity dbEntity = deviceSpareMapper.getDeviceSpare(deviceSpareEntity);
		// 当前有效库存
		Integer availableInventory = dbEntity.getAvailable_inventory();

		// 确认数量
		Integer confirmQuantity = Integer.valueOf(form.getConfirm_quantity());
		
		//订单号
		String orderNO = form.getOrder_no();

		// 当前有效库存 + 确认数量
		availableInventory = availableInventory + confirmQuantity;

		deviceSpareEntity.setAvailable_inventory(availableInventory);
		// ①更新当前有效库存
		deviceSpareMapper.updateAvailableInventory(deviceSpareEntity);

		DeviceSpareAdjustEntity deviceSpareAdjustEntity = new DeviceSpareAdjustEntity();
		// 设备品名 ID
		deviceSpareAdjustEntity.setDevice_type_id(form.getDevice_type_id());
		// 型号
		deviceSpareAdjustEntity.setModel_name(form.getModel_name());
		// 备品种类
		deviceSpareAdjustEntity.setDevice_spare_type(Integer.valueOf(form.getDevice_spare_type()));
		// 调整日时
		deviceSpareAdjustEntity.setAdjust_time(Calendar.getInstance().getTime());
		// 理由(入库)
		deviceSpareAdjustEntity.setReason_type(11);
		// 调整量
		deviceSpareAdjustEntity.setAdjust_inventory(confirmQuantity);
		// 调整负责人
		deviceSpareAdjustEntity.setOperator_id(user.getOperator_id());
		// 备注来源
		deviceSpareAdjustEntity.setComment("订购单<order_no>" + orderNO + "</order_no>到货后入库。");
		
		// ②新建设备工具备品调整记录
		deviceSpareAdjustMapper.insert(deviceSpareAdjustEntity);

		DeviceJigOrderDetailEntity deviceJigOrderDetailEntity = new DeviceJigOrderDetailEntity();
		BeanUtil.copyToBean(form, deviceJigOrderDetailEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// ③更新确认数量
		deviceJigOrderDetailMapper.updateConfirmQuantity(deviceJigOrderDetailEntity);
	}
	
	/**
	 * 发票登记
	 * @param form
	 * @param conn
	 */
	public void updateTicket(ActionForm form,SqlSessionManager conn){
		// 数据连接
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.updateTicket(entity);
	}
	
	public String downloadGInvoice(ActionForm form,SqlSession conn) throws Exception{
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);
		
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "总务G本地询价模板.xlsx";;
		String cacheName = "总务G本地询模板" + new Date().getTime() + ".xlsx";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		
		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		List<DeviceJigOrderDetailEntity> list = dao.searchDetailUnQuotation(entity);
		int length = list.size();
		
		InputStream in = null;
		OutputStream out = null;
		
		try{
			if(length > 0){
				try {
					FileUtils.copyFile(new File(path), new File(cachePath));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				in = new FileInputStream(cachePath);//读取文件 
				XSSFWorkbook work = new XSSFWorkbook(in);//创建xls文件
				XSSFSheet sheet = work.getSheetAt(0);//取得第一个Sheet
				
				XSSFRow row = null;
				
				if(length > 1){
					/** 拷贝行**/
					// 需要拷贝的行数
					int copyRows = length - 1;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					XSSFRow fromRow = null;
					XSSFRow toRow = null;
					CellRangeAddress cellRangeAddress = null;
					int dex = 0;
					int firstRow = 0;
					int lastRow = 0;
					int firstCol = 0;
					int lastCol = 0;
					
					//被复制行索引
					int [] srcRowIndex = {5,6};
					
					//生成行
					for(int j = 1;j <= copyRows; j++){
						int lastRowNum = sheet.getLastRowNum() + 1;//最后一行索引+1
						
						//复制普通单元格
						for(int index = 0;index < srcRowIndex.length; index++){
							dex = lastRowNum + index ;
							fromRow = sheet.getRow(srcRowIndex[index]);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow,true);
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {  
							cellRangeAddress = sheet.getMergedRegion(i);
							
							firstRow = cellRangeAddress.getFirstRow();
							if(firstRow == 5){
								lastRow = cellRangeAddress.getLastRow();
								firstCol = cellRangeAddress.getFirstColumn();
								lastCol = cellRangeAddress.getLastColumn();
								
								cellRangeAddress = new CellRangeAddress(firstRow + (j*2), lastRow + (j*2), firstCol, lastCol);
								sheet.addMergedRegion(cellRangeAddress);
							}
						}
					}
				}
				
				/**往Excel填充数据**/
				// 开始行索引
				int rowIndex = 5;
				for(int i = 0;i < length; i++){
					entity = list.get(i);
					row = sheet.getRow(rowIndex);
					
					//编号
					row.getCell(0).setCellValue(i+1);
					
					//日期
					row.getCell(1).setCellValue(entity.getApplicate_date());
					
					//申请者
					row.getCell(3).setCellValue(entity.getApplicator_operator_name());
					
					//物品品名
					row.getCell(4).setCellValue(entity.getName());
					
					//品牌/型号/规格
					row.getCell(5).setCellValue(entity.getModel_name());
					
					//数量
					row.getCell(6).setCellValue(entity.getQuantity());
					
					//理由
					row.getCell(9).setCellValue(entity.getNesssary_reason());
					
					rowIndex +=2;
				}
				
				out = new FileOutputStream(cachePath);
				work.write(out);
			}
		} catch(Exception e){
			throw e;
		} finally{
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
		
		return cacheName;
	}

	public String downloadOSHInvoice(ActionForm form, SqlSession conn) throws Exception{
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);
		
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "OSH购买询价模板.xlsx";;
		String cacheName = "OSH购买询价" + new Date().getTime() + ".xlsx";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		
		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		List<DeviceJigOrderDetailEntity> list = dao.searchDetailUnQuotation(entity);
		int length = list.size();
		
		InputStream in = null;
		OutputStream out = null;
		
		try{
			if(length > 0){
				try {
					FileUtils.copyFile(new File(path), new File(cachePath));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				in = new FileInputStream(cachePath);//读取文件 
				XSSFWorkbook work = new XSSFWorkbook(in);//创建xls文件
				XSSFSheet sheet = work.getSheetAt(0);//取得第一个Sheet

				//分页
				sheet.setRowBreak(sheet.getLastRowNum());
				
				XSSFRow row = null;
				
				//日期
				sheet.getRow(14).getCell(1).setCellValue("日　期："+DateUtil.toString(Calendar.getInstance().getTime(), DateUtil.DATE_PATTERN));
				
				//每页显示数量
				int pageTotal = 33;
				
				List<Integer> rowIndexList = new LinkedList<Integer>();
				for(int i = 21; i <=53;i++){
					rowIndexList.add(i);
				}
				
				int differNum= length - pageTotal;
				
				if(differNum > 0){
					//需要复制的页数(不包含第一页)
					int pageNum = differNum % pageTotal == 0 ? differNum / pageTotal : differNum / pageTotal + 1;
					
					//每页间隔
					int margin = 0;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					//图形
					XSSFDrawing drawing = sheet.createDrawingPatriarch();
					List<XSSFShape> shapeList = drawing.getShapes();
					int shapeNums = shapeList.size();
					
					XSSFRow fromRow = null;
					XSSFRow toRow = null;
					CellRangeAddress cellRangeAddress = null;
					XSSFShape shape = null;
					XSSFClientAnchor anchor = null;
					XSSFSimpleShape simpleShape = null;
					
					int startRowNum = 0;
					int endRowNum = 0;
					int dex = 0;
					int firstRow = 0;
					int lastRow = 0;
					int firstCol = 0;
					int lastCol = 0;
					
					for(int j = 0;j < pageNum; j++){
						int lastRowNum = sheet.getLastRowNum() + 1;//最后一行索引+1

						//复制普通单元格
						for(int index = 0;index <= 59; index++){
							dex = lastRowNum + index + margin;
							fromRow = sheet.getRow(index);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow, true);
							
							if(index >= 21 && index <= 53){
								// 总额
								toRow.getCell(6).setCellFormula("E" + (dex + 1) + "*F" + (dex + 1));
								rowIndexList.add(dex);
							}
							
							if(index == 21){
								startRowNum = dex + 1;
							}
							if(index == 53){
								endRowNum = dex + 1;
							}
							if(index == 54){
								// 合计
								toRow.getCell(6).setCellFormula("SUM(G" + startRowNum + ":G" + endRowNum + ")");
							}
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {
							cellRangeAddress = sheet.getMergedRegion(i);
							firstRow = cellRangeAddress.getFirstRow() + lastRowNum + margin;
							lastRow = cellRangeAddress.getLastRow() + lastRowNum + margin;
							firstCol = cellRangeAddress.getFirstColumn();
							lastCol = cellRangeAddress.getLastColumn();
							
							cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
							sheet.addMergedRegion(cellRangeAddress);
						}
						
						//复制图行
						for (int i = 0; i < shapeNums; i++) {
							shape = shapeList.get(i);
							anchor = (XSSFClientAnchor)shape.getAnchor();
							int anchorType = anchor.getAnchorType();
							
							if (shape instanceof XSSFSimpleShape){//简单图形
								simpleShape = (XSSFSimpleShape)shape;
								CTShape cTShape = simpleShape.getCTShape();
								
								int dx1 = simpleShape.getAnchor().getDx1();
								int dy1 = simpleShape.getAnchor().getDy1();
								int dx2 = simpleShape.getAnchor().getDx2();
								int dy2 = simpleShape.getAnchor().getDy2();
								int row1 = anchor.getRow1();
								short col1 = anchor.getCol1();
								int row2 = anchor.getRow2();
								short col2 = anchor.getCol2();
								anchor = new XSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1+lastRowNum+margin, col2, row2+lastRowNum+margin);
								anchor.setAnchorType(anchorType);
								
								simpleShape = drawing.createSimpleShape(anchor);
								simpleShape.getCTShape().setSpPr(cTShape.getSpPr());
								simpleShape.getCTShape().setNvSpPr(cTShape.getNvSpPr());
								// 文本
								if(cTShape.getTxBody() != null){
									simpleShape.getCTShape().setTxBody(cTShape.getTxBody());
								}
							}
						}
						//分页
						sheet.setRowBreak(sheet.getLastRowNum());
					}
				}

				/**往Excel填充数据**/
				for(int i = 0;i < length; i++){
					entity = list.get(i);
					row = sheet.getRow(rowIndexList.get(i));
					
					//No
					row.getCell(1).setCellValue(i+1);
					
					//品名
					row.getCell(2).setCellValue(entity.getName());
					
					//型号
					row.getCell(3).setCellValue(entity.getModel_name());
					
					//数量
					row.getCell(4).setCellValue(entity.getQuantity());
				}
				
				//重新设置打印区域
				work.setPrintArea(0, 1, 7, 0, sheet.getLastRowNum());
				
				// 自动计算公式
				work.setForceFormulaRecalculation(true);
				
				out = new FileOutputStream(cachePath);
				work.write(out);
			}
		} catch(Exception e){
			throw e;
		} finally{
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
		
		return cacheName;
	}

	public String downloadOSHOrder(ActionForm form, SqlSession conn) throws Exception {
		DeviceJigOrderDetailMapper dao = conn.getMapper(DeviceJigOrderDetailMapper.class);

		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "OSH订购单模板.xlsx";;
		String cacheName = "OSH订购单" + new Date().getTime() + ".xlsx";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		
		DeviceJigOrderDetailEntity entity = new DeviceJigOrderDetailEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		entity.setOrder_from(5);
		
		List<DeviceJigOrderDetailEntity> list = dao.searchOrderUnComfirm(entity);
		int length = list.size();
		
		InputStream in = null;
		OutputStream out = null;
		
		try{
			if(length > 0){
				try {
					FileUtils.copyFile(new File(path), new File(cachePath));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				in = new FileInputStream(cachePath);//读取文件 
				XSSFWorkbook work = new XSSFWorkbook(in);//创建xls文件
				XSSFSheet sheet = work.getSheetAt(0);//取得第一个Sheet
				
				//分页
				sheet.setRowBreak(sheet.getLastRowNum());
				
				XSSFRow row = null;
				
				//订货单No．
				sheet.getRow(0).getCell(7).setCellValue(entity.getOrder_no());
				
				//日期
				sheet.getRow(12).getCell(1).setCellValue("日　期：" + DateUtil.toString(Calendar.getInstance().getTime(), DateUtil.DATE_PATTERN));
				
				//每页显示数量
				int pageTotal = 20;
				
				//数据填充行索引集合
				List<Integer> rowIndexList = new LinkedList<Integer>();
				for(int i = 21; i <= 40;i++){
					rowIndexList.add(i);
				}
				
				int differNum = length - pageTotal;
				
				if(differNum > 0){
					//需要复制的页数(不包含第一页)
					int pageNum = differNum % pageTotal == 0 ? differNum / pageTotal : differNum / pageTotal + 1;
					
					//每页间隔
					int margin = 0;
					
					//当前sheet合并单元格总数
					int sheetMergerCount =  sheet.getNumMergedRegions();
					
					//当前sheet图形总数
					XSSFDrawing drawing = sheet.createDrawingPatriarch();
					List<XSSFShape> shapeList = drawing.getShapes();
					int shapeNums = shapeList.size();
					
					XSSFRow fromRow = null;
					XSSFRow toRow = null;
					CellRangeAddress cellRangeAddress = null;
					XSSFShape shape = null;
					XSSFClientAnchor anchor = null;
					XSSFSimpleShape simpleShape = null;
					int startRowNum = 0;
					int endRowNum = 0;
					int dex = 0;
					int firstRow = 0;
					int lastRow = 0;
					int firstCol = 0;
					int lastCol = 0;
					
					for(int j = 0;j < pageNum; j++){
						int lastRowNum = sheet.getLastRowNum() + 1;//最后一行索引+1
						
						//复制普通单元格
						for(int index = 0;index <= 46; index++){
							dex = lastRowNum + index + margin;
							fromRow = sheet.getRow(index);
							toRow = sheet.createRow(dex);
							CopyByPoi.copyRow(fromRow, toRow, true);
							
							if(index >= 21 && index <= 40){
								// 总额
								toRow.getCell(6).setCellFormula("E" + (dex + 1) + "*F" + (dex + 1));
								rowIndexList.add(dex);
							}
							
							if(index == 21){
								startRowNum = dex + 1;
							}
							if(index == 40){
								endRowNum = dex + 1;
							}
							if(index == 41){
								// 合计
								toRow.getCell(6).setCellFormula("SUM(G" + startRowNum + ":G" + endRowNum + ")");
							}
						}
						
						//复制合并单元格
						for (int i = 0; i < sheetMergerCount; i++) {
							cellRangeAddress = sheet.getMergedRegion(i);
							firstRow = cellRangeAddress.getFirstRow() + lastRowNum + margin;
							lastRow = cellRangeAddress.getLastRow() + +lastRowNum + margin;
							firstCol = cellRangeAddress.getFirstColumn();
							lastCol = cellRangeAddress.getLastColumn();

							cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
							sheet.addMergedRegion(cellRangeAddress);
						}
						
						//复制图行
						for (int i = 0; i < shapeNums; i++) {
							shape = shapeList.get(i);
							anchor = (XSSFClientAnchor)shape.getAnchor();
							int anchorType = anchor.getAnchorType();
							
							if (shape instanceof XSSFSimpleShape){ //简单图形
								simpleShape = (XSSFSimpleShape)shape;
								CTShape cTShape = simpleShape.getCTShape();
								
								int dx1 = simpleShape.getAnchor().getDx1();
								int dy1 = simpleShape.getAnchor().getDy1();
								int dx2 = simpleShape.getAnchor().getDx2();
								int dy2 = simpleShape.getAnchor().getDy2();
								int row1 = anchor.getRow1() + lastRowNum + margin;
								short col1 = anchor.getCol1();
								int row2 = anchor.getRow2() + lastRowNum + margin;
								short col2 = anchor.getCol2();
								anchor = new XSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
								anchor.setAnchorType(anchorType);
								
								simpleShape = drawing.createSimpleShape(anchor);
								simpleShape.getCTShape().setSpPr(cTShape.getSpPr());
								simpleShape.getCTShape().setNvSpPr(cTShape.getNvSpPr());
								// 文本
								if(cTShape.getTxBody() != null){
									simpleShape.getCTShape().setTxBody(cTShape.getTxBody());
								}
							}
						}
						//分页
						sheet.setRowBreak(sheet.getLastRowNum());
					}
				}
				
				/**往Excel填充数据**/
				for(int i = 0;i < length; i++){
					entity = list.get(i);
					row = sheet.getRow(rowIndexList.get(i));
					
					//No
					row.getCell(1).setCellValue(i+1);
					
					//品名
					row.getCell(2).setCellValue(entity.getName());
					
					//型号
					row.getCell(3).setCellValue(entity.getModel_name());
					
					//数量
					row.getCell(4).setCellValue(entity.getQuantity());
					
					//单价
					if(entity.getOrder_price() != null){
						row.getCell(5).setCellValue(entity.getOrder_price().doubleValue());
					}
				}
				
				//重新设置打印区域
				work.setPrintArea(0, 1, 7, 0, sheet.getLastRowNum());
				
				// 自动计算公式
				work.setForceFormulaRecalculation(true);
				
				out = new FileOutputStream(cachePath);
				work.write(out);
			}
		}catch(Exception e){
			throw e;
		} finally{
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
		
		return cacheName;
	}
}
