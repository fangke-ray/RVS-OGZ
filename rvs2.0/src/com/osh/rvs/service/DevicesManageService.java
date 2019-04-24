package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.DeviceTypeEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.XlsUtil;
import com.osh.rvs.form.equipment.DeviceJigOrderDetailForm;
import com.osh.rvs.form.master.DevicesManageForm;
import com.osh.rvs.form.master.DevicesTypeForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.service.equipment.DeviceJigOrderService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class DevicesManageService {
     PositionService positionService = new PositionService();
	/**
	 * 设备工具管理详细画面
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<DevicesManageForm> searchDevicesManage(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		BeanUtil.copyToBean(form, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<DevicesManageForm> devicesManageFroms = new ArrayList<DevicesManageForm>();

		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);

		List<DevicesManageEntity> devicesManageEntities = dao.searchDeviceManage(devicesManageEntity);

		BeanUtil.copyToFormList(devicesManageEntities, devicesManageFroms, CopyOptions.COPYOPTIONS_NOEMPTY,
				DevicesManageForm.class);

		return devicesManageFroms;
	}

	/**
	 * 设备工具分布一览
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<DevicesManageForm> searchDistribute(ActionForm form, SqlSession conn) {
		DevicesManageEntity entity = new DevicesManageEntity();
		// 复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		List<DevicesManageEntity> list = dao.searchDistribute(entity);

		List<DevicesManageForm> returnFormList = new ArrayList<DevicesManageForm>();

		if (list != null && list.size() > 0) {
			// 复制数据到表单
			BeanUtil.copyToFormList(list, returnFormList, CopyOptions.COPYOPTIONS_NOEMPTY, DevicesManageForm.class);
			return returnFormList;
		} else {
			return null;
		}
	}

	/**
	 * 修改设备管理详细
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 */
	public void updateDevicesManage(String compare_status,DevicesManageForm devicesManageForm, SqlSessionManager conn, HttpSession session,
			List<MsgInfo> errors) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();

		Calendar calendar = Calendar.getInstance();

		//状态选择使用中,有发放日期+发放时间
		int event = 0;
		String provide_date = "";
		//修改后的状态是使用中;如果选择后的状态是保管中，发放者和发放者变成空
		if ("false".equals(compare_status)) {
			if("1".equals(devicesManageForm.getStatus())){
				provide_date = DateUtil.toString(calendar.getTime(), "yyyy/MM/dd");
				devicesManageForm.setWaste_date(null);
				devicesManageForm.setProvide_date(provide_date);
				event = 1;
			}else if("4".equals(devicesManageForm.getStatus())){
				provide_date="";
				devicesManageForm.setWaste_date(null);
				devicesManageForm.setProvide_date(provide_date);
			}
		}
		
		//如果状态是废弃或者遗失--必须填写废弃日期
		if("2".equals(devicesManageForm.getStatus()) || "3".equals(devicesManageForm.getStatus())){
			if(CommonStringUtil.isEmpty(devicesManageForm.getWaste_date())){
				MsgInfo error = new MsgInfo();
				error.setComponentid("waste_date");
				error.setErrcode("validator.required");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "废弃日期",
						devicesManageForm.getWaste_date(), "废弃日期"));
				errors.add(error);
			} else {
				event = 2;
			}
		}
		
		BeanUtil.copyToBean(devicesManageForm, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesManageEntity.setUpdated_by(user.getOperator_id());

		if(errors.size()==0){
			DevicesManageEntity oldEntity = dao.getByKey(devicesManageEntity.getDevices_manage_id());
			String sOldManageCode = oldEntity.getManage_code();
			String sNewManageCode = devicesManageEntity.getManage_code();
			String sOldPosition = oldEntity.getPosition_id();
			String sNewPosition = devicesManageEntity.getPosition_id();
			if (event == 0) {
				if (CommonStringUtil.isEmpty(sOldManageCode)) {
					if (!CommonStringUtil.isEmpty(sNewManageCode)) {
						event = 3;
					}
				} else if (!sOldManageCode.equals(sNewManageCode)) {
					event = 3;
				}
			}
			if (event == 0) {
				if (CommonStringUtil.isEmpty(sOldPosition)) {
					if (!CommonStringUtil.isEmpty(sNewPosition)) {
						event = 3;
					}
				} else if (!sOldPosition.equals(sNewPosition)) {
					event = 3;
				}
			}

			// 登记设备管理变更记录
			if (event != 0) {
				DevicesManageEntity dme = new DevicesManageEntity();
				dme.setDevices_manage_id(devicesManageEntity.getDevices_manage_id());
				dme.setOld_manage_code(sOldManageCode);
				dme.setManage_code(sNewManageCode);
				dme.setOld_position_id(sOldPosition);
				dme.setPosition_id(sNewPosition);
				dme.setUpdated_by(devicesManageEntity.getUpdated_by());
				dme.setEvent(event);

				// 登记设备管理变更记录
				dao.insertDeviceManageRecord(dme);
			}
			dao.updateDevicesManage(devicesManageEntity);

			// 清除工位待点检品判断
			CheckResultPageService.todayCheckedMap.clear();
		}
	}

	/**
	 * 新建设备管理
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 */
	public void insertDevicesManage(DevicesManageForm devicesManageForm, SqlSessionManager conn, HttpSession session,
			List<MsgInfo> errors) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();

		int event = 0;

		Calendar calendar = Calendar.getInstance();
		//状态选择使用中,有发放日期+发放时间
		String provide_date = "";
		if ("1".equals(devicesManageForm.getStatus())) {
			provide_date = DateUtil.toString(calendar.getTime(), "yyyy/MM/dd");
			event = 1;
		}
		devicesManageForm.setProvide_date(provide_date);

		//如果状态是废弃或者遗失--必须填写废弃日期
		if("2".equals(devicesManageForm.getStatus()) || "3".equals(devicesManageForm.getStatus())){
			if(CommonStringUtil.isEmpty(devicesManageForm.getWaste_date())){
				MsgInfo error = new MsgInfo();
				error.setComponentid("waste_date");
				error.setErrcode("validator.required");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "废弃日期",
						devicesManageForm.getWaste_date(), "废弃日期"));
				errors.add(error);
				return;
			} else {
				event = 2;
			}
		}
		
		BeanUtil.copyToBean(devicesManageForm, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesManageEntity.setUpdated_by(user.getOperator_id());

		dao.insertDevicesManage(devicesManageEntity);

		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();

		// 登记设备管理变更记录
		if (event != 0) {
			CommonMapper cm = conn.getMapper(CommonMapper.class);
			String lastInsertID = cm.getLastInsertID();

			DevicesManageEntity dme = new DevicesManageEntity();
			dme.setDevices_manage_id(lastInsertID);
			dme.setOld_manage_code(null);
			dme.setManage_code(devicesManageEntity.getManage_code());
			dme.setOld_position_id(null);
			dme.setPosition_id(devicesManageEntity.getPosition_id());
			dme.setUpdated_by(devicesManageEntity.getUpdated_by());
			dme.setEvent(event);

			dao.insertDeviceManageRecord(dme);
		}
	}

	/* 验证管理编号不能重复 */
	public void customValidate(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		/* 数据复制 */
		BeanUtil.copyToBean(form, devicesManageEntity ,(new CopyOptions()).include("devices_manage_id", "manage_code"));

		List<String> resultBean = dao.searchManageCode(devicesManageEntity);
		if (resultBean != null && resultBean.size() > 0) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("manage_code");
			error.setErrcode("dbaccess.columnNotUnique");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "管理编号",
					devicesManageEntity.getManage_code(), "管理编号"));
			errors.add(error);
		}
	}
	
	/*验证选择状态是保管中时，工程和工位必须2选1*/
	public void validateStatus(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		/* 数据复制 */
		BeanUtil.copyToBean(form, devicesManageEntity,CopyOptions.COPYOPTIONS_NOEMPTY);

		//管理等级是A或者B
		if(devicesManageEntity.getManage_level()==1 ||devicesManageEntity.getManage_level()==2){
			//如果状态是使用中，则工程和工位必须2选1
			if ("1".equals(devicesManageEntity.getStatus()) && CommonStringUtil.isEmpty(devicesManageEntity.getPosition_id()) && CommonStringUtil.isEmpty(devicesManageEntity.getLine_id())){
					MsgInfo error = new MsgInfo();
					error.setComponentid("devices_manage_id");
					error.setErrcode("info.devices.chooseLinePosition");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.devices.chooseLinePosition", "",
							devicesManageEntity.getLine_id(), ""));
					errors.add(error);		
			} 		
		}
//		//如果状态是保管中，只能选择技术课
//		if("4".equals(devicesManageEntity.getStatus()) && !("00000000010".equals(devicesManageEntity.getSection_id()))){
//			MsgInfo error = new MsgInfo();
//			error.setComponentid("devices_manage_id");
//			error.setErrcode("info.devices.chooseSection");
//			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.devices.chooseSection", "",
//					devicesManageEntity.getLine_id(), ""));
//			errors.add(error);	
//		}
	}
	
	/**
	 * 获取最大的管理编号
	 * @param form
	 * @param conn
	 * @param request
	 * @return
	 */
	public String searchMaxManageCode(ActionForm form,SqlSession conn,HttpServletRequest request){
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		BeanUtil.copyToBean(form, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		String paramMC =devicesManageEntity.getManage_code();
		if(paramMC.matches("^.*\\-(\\w){1,2}$")){
			String fixMc = paramMC.replaceAll("\\-(\\w){1,2}$", "");
			devicesManageEntity.setManage_code(fixMc);
		}
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		List<String> manageCodes = dao.searchMaxManageCode(devicesManageEntity);
		int size=manageCodes.size();
		
		String manageCode="";
		String maxManageCode="";
		if(size>0){
			//取出最大的管理编号
			maxManageCode = manageCodes.get(0);
			if(maxManageCode.matches("^.*\\-(\\w){1,2}$")){
				//管理编号 = 前缀+后缀(A,B,C,D...)
				String[] part = maxManageCode.split("-");
				//管理编号前缀
				String mcPrefix   =maxManageCode.replaceAll("\\-(\\w){1,2}$", "");
				//管理编号"/"后缀(A,B,C,D...)
				String mcPostfix  = part[part.length - 1];
				Integer seq=XlsUtil.getExcelRowSeq(mcPostfix);
				seq++;
				String str=XlsUtil.getExcelColCode(seq);
				manageCode = mcPrefix+"-"+str;
			}else{
				manageCode =maxManageCode+"-A";
			}		
		}
		return manageCode;
	}

	/**
	 * 删除设备管理
	 * 
	 * @param form
	 * @param conn
	 * @param session
	 * @param errors
	 */
	public void deleteDevicesManage(ActionForm form, SqlSessionManager conn, HttpSession session, List<MsgInfo> errors) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		BeanUtil.copyToBean(form, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesManageEntity.setUpdated_by(user.getOperator_id());

		dao.deleteDevicesManage(devicesManageEntity);

		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();
	}

	// 取得设备管理人员
	public String getDevicesManageroptions(SqlSession conn) {

		List<String[]> lst = new ArrayList<String[]>();
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		int privacy_id = RvsConsts.PRIVACY_DT_MANAGE;// 设备管理
		List<OperatorNamedEntity> list = dao.getOperatorWithPrivacy(privacy_id);

		lst = OperatorService.getSetReferChooser(list, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);
		return pReferChooser;
	}
	
	//工位选择--工位在前，工位名称在后
	public String getOptionPtions(SqlSession conn) {
		List<String[]> lst = new ArrayList<String[]>();
		
		List<PositionEntity> allPosition = positionService.getAllPosition(conn);
		
		for (PositionEntity position: allPosition) {
			String[] p = new String[3];
			p[0] = position.getPosition_id();
			p[1] = position.getProcess_code();
			p[2] = position.getName();
			lst.add(p);
		}
		
		String pReferChooser = CodeListUtils.getReferChooser(lst);
		
		return pReferChooser;
	}

	public void exchange(DevicesManageForm devicesManageForm,
			String operator_id, SqlSessionManager conn, List<MsgInfo> errors) {
		DevicesManageEntity dme = new DevicesManageEntity();
		BeanUtil.copyToBean(devicesManageForm, dme, CopyOptions.COPYOPTIONS_NOEMPTY);
		dme.setUpdated_by(operator_id);
		DevicesManageMapper mapper = conn.getMapper(DevicesManageMapper.class);
		mapper.exchange(dme);
		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();
	}

	public void disband(DevicesManageForm devicesManageForm,
			String operator_id, SqlSessionManager conn, List<MsgInfo> errors) {
		DevicesManageEntity dme = new DevicesManageEntity();
		BeanUtil.copyToBean(devicesManageForm, dme, CopyOptions.COPYOPTIONS_NOEMPTY);
		dme.setUpdated_by(operator_id);
		DevicesManageMapper mapper = conn.getMapper(DevicesManageMapper.class);
		mapper.disband(dme);
		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();
	}
	
	//批量交付
	public void deliverDevicesManage(DevicesManageForm devicesManageForm,SqlSessionManager conn, HttpSession session, List<MsgInfo> errors,HttpServletRequest request) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		
		// 当前操作者ID
		LoginData user = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);
		//多选--key
		List<DevicesManageEntity> keys = this.getPostKeys(request.getParameterMap());
		MsgInfo error = new MsgInfo();
		if (keys.size()>0) {
			//判断交付条件是否改
			if("true".equals(devicesManageForm.getCompare_section_id()) && "true".equals(devicesManageForm.getCompare_line_id()) && "true".equals(devicesManageForm.getCompare_position_id())&&"true".equals(devicesManageForm.getCompare_manager_operator_id()) ){
				error.setComponentid("devices_manage_id");
				error.setErrmsg("交付条件未改变");
				errors.add(error);
			}else{
				for(DevicesManageEntity conditionEntity : keys){

					boolean isChanged = !CommonStringUtil.isEmpty(conditionEntity.getManage_code());
					if (!"true".equals(devicesManageForm.getCompare_section_id())) {
						conditionEntity.setSection_id(devicesManageForm.getSection_id());
						isChanged = true;
					}
					if (!"true".equals(devicesManageForm.getCompare_line_id())) {
						conditionEntity.setLine_id(devicesManageForm.getLine_id());
						isChanged = true;
					}
					if (!"true".equals(devicesManageForm.getCompare_position_id())) {
						conditionEntity.setPosition_id(devicesManageForm.getPosition_id());
						isChanged = true;
					}
					if (!"true".equals(devicesManageForm.getCompare_manager_operator_id())) {
						conditionEntity.setManager_operator_id(devicesManageForm.getManager_operator_id());
					}

					conditionEntity.setUpdated_by(user.getOperator_id());

					// 登记设备管理变更记录
					if (isChanged) {
						DevicesManageEntity oldEntity = dao.getByKey(conditionEntity.getDevices_manage_id());
						String sOldManageCode = oldEntity.getManage_code();
						String sOldPosition = oldEntity.getPosition_id();

						DevicesManageEntity dme = new DevicesManageEntity();
						dme.setDevices_manage_id(conditionEntity.getDevices_manage_id());
						dme.setOld_manage_code(sOldManageCode);
						dme.setManage_code(conditionEntity.getManage_code());
						dme.setOld_position_id(sOldPosition);
						dme.setPosition_id(devicesManageForm.getPosition_id());
						dme.setUpdated_by(user.getOperator_id());
						dme.setEvent(3);

						dao.insertDeviceManageRecord(dme);
					}

					dao.deliverDevicesManage(conditionEntity);

					// 清除工位待点检品判断
					CheckResultPageService.todayCheckedMap.clear();
				}
			}

		} else {
			error.setComponentid("devices_manage_id");
			error.setErrcode("validator.required.multidetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "要交付的设备工具",
					null, "要交付的设备工具"));
			errors.add(error);
		}
	}
	
	// 获取被选择的多个设备
	public List<DevicesManageEntity> getPostKeys(Map<String, String[]> parameters) {

		List<DevicesManageEntity> keyWithNewCodes = new AutofillArrayList<DevicesManageEntity>(DevicesManageEntity.class);
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据
		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("keys".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);

					if ("devices_manage_id".equals(column)) {
						keyWithNewCodes.get(icounts).setDevices_manage_id(value[0]);
					} else if ("manage_code".equals(column)) {
						keyWithNewCodes.get(icounts).setManage_code(value[0]);
					}
				}
			}
		}

		return keyWithNewCodes;
	}

	/**
	 * 替换新品--新建
	 * @param devicesManageForm
	 * @param conn
	 * @param session
	 * @param errors
	 */
	public void replaceDevicesManage(String compare_status ,String old_manage_code,DevicesManageForm devicesManageForm,
			SqlSessionManager conn, HttpSession session, List<MsgInfo> errors) {
		DevicesManageMapper dao = conn.getMapper(DevicesManageMapper.class);
		DevicesManageEntity devicesManageEntity = new DevicesManageEntity();
		BeanUtil.copyToBean(devicesManageForm, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		int event = 0;

		Calendar calendar = Calendar.getInstance();
		//状态选择使用中,发放日期是当前时间
		if ("1".equals(devicesManageForm.getStatus())) {//新品   状态:使用中
			String provide_date = DateUtil.toString(calendar.getTime(), "yyyy/MM/dd");//放时间设成当前时间
			devicesManageEntity.setProvide_date(DateUtil.toDate(provide_date, DateUtil.DATE_PATTERN));
			event = 1;
		}
		
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesManageEntity.setUpdated_by(user.getOperator_id());//发放者是登录人

		dao.replaceDevicesManage(devicesManageEntity);

		// 登记设备管理变更记录
		if (event != 0) {
			CommonMapper cm = conn.getMapper(CommonMapper.class);
			String lastInsertID = cm.getLastInsertID();

			DevicesManageEntity dme = new DevicesManageEntity();
			dme.setDevices_manage_id(lastInsertID);
			dme.setOld_manage_code(old_manage_code);
			dme.setManage_code(devicesManageEntity.getManage_code());
			dme.setOld_position_id(null);
			dme.setPosition_id(devicesManageEntity.getPosition_id());
			dme.setUpdated_by(devicesManageEntity.getUpdated_by());
			dme.setEvent(event);

			dao.insertDeviceManageRecord(dme);
		}
		//同时废弃掉旧品--选择是(1)--则进行废弃旧品操作
		
		devicesManageEntity = new DevicesManageEntity();
		BeanUtil.copyToBean(devicesManageForm, devicesManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		if("1".equals(devicesManageForm.getWaste_old_products())){
			devicesManageEntity.setUpdated_by(user.getOperator_id());//发放者是登录人
			dao.disband(devicesManageEntity);
		}

		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();
	}

	public List<DevicesTypeForm> getOfPositionHazardousCautionsAndSafetyGuide(
			String section_id, String position_id, SqlSession conn) {
		DevicesManageMapper mapper = conn.getMapper(DevicesManageMapper.class);

		List<DevicesTypeForm> result = new ArrayList<DevicesTypeForm>();

		DevicesManageEntity dmCond = new DevicesManageEntity();
		dmCond.setPosition_id(position_id);
		dmCond.setSection_id(section_id);
		List<DeviceTypeEntity> list = mapper.getDeviceTypeOfPosition(dmCond);

		DevicesTypeService dts = new DevicesTypeService();
		for(DeviceTypeEntity deviceType : list) {
			DevicesTypeForm devicesTypeForm = new DevicesTypeForm();
			boolean show = false;
			if (deviceType.getClassification() != null) {
				devicesTypeForm.setHazardous_cautions(dts.getClassificationText(deviceType.getClassification()));
				show = true;
			}
			if (DevicesTypeService.getSafetyGuideSet().contains(deviceType.getDevice_type_id())) {
				devicesTypeForm.setSafety_guide("1");
				show = true;
			}
			if (show) {
				BeanUtil.copyToForm(deviceType, devicesTypeForm, CopyOptions.COPYOPTIONS_NOEMPTY);
				result.add(devicesTypeForm);
			}
		}

		return result;
	}

	public void setAsManageCode(ActionForm form,  HttpServletRequest request, 
			SqlSessionManager conn) {
		DeviceJigOrderService doService = new DeviceJigOrderService();
		DevicesManageForm deviceForm = (DevicesManageForm) form;
		String orderKey_objectType = deviceForm.getOrder_key();
		String[] splt = orderKey_objectType.split("_");

		LoginData user = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);

		doService.setAsManageCode(splt[0], splt[1].charAt(0) - '0', 
				deviceForm.getDevices_type_id(), deviceForm.getModel_name(), 
				deviceForm.getCompare_manager_operator_id(), 1, 
				user.getOperator_id(), deviceForm.getManage_code(), conn);
	}

	/**
	 * 判断存在同类订购品
	 * @param searchedOrder
	 * @return
	 */
	public List<Map<String, String>> checkExistsOrder(
			List<DeviceJigOrderDetailForm> searchedOrder) {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		if (searchedOrder.size() > 0) {
			for (DeviceJigOrderDetailForm djodForm : searchedOrder) {
				if (djodForm.getConfirm_quantity() == null || !djodForm.getConfirm_quantity().equals(djodForm.getQuantity())) {
					Map<String, String> hit = new HashMap<String, String>();
					hit.put("order_key", djodForm.getOrder_key());
					hit.put("order_no", djodForm.getOrder_no());
					hit.put("applicator_id", djodForm.getApplicator_id());
					hit.put("object_type", djodForm.getObject_type());
					hit.put("operator_name", djodForm.getApplicator_operator_name());
					ret.add(hit);
				}
			}
		}
		return ret;
	}
}
