package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.DeviceTypeEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.DevicesTypeForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.DevicesTypeMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class DevicesTypeService {

	public static final int SPECIALIZED_FOR_DISINFECT_DEVICE = 3; //消毒设备
	public static final int SPECIALIZED_FOR_STERILIZE_DEVICE = 4;//灭菌设备

	/**
	 * 设备工具品名 详细
	 * 
	 * @param toolsCheckEntity
	 * @param conn
	 * @return
	 */
	public List<DevicesTypeForm> searchDevicesType(DeviceTypeEntity devicesTypeEntity, SqlSession conn) {

		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
		List<DevicesTypeForm> devicesTypeForms = new ArrayList<DevicesTypeForm>();

		List<DeviceTypeEntity> devicesTypeEntities = dao.searchDeviceType(devicesTypeEntity);

		for(DeviceTypeEntity entity : devicesTypeEntities) {
			DevicesTypeForm devicesTypeForm = new DevicesTypeForm();
			BeanUtil.copyToForm(entity, devicesTypeForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			if (entity.getClassification() != null) {
				devicesTypeForm.setHazardous_cautions(getClassificationText(entity.getClassification()));
			}
			devicesTypeForms.add(devicesTypeForm);
		}

		return devicesTypeForms;
	}

	/**
	 * 插入设备工具品名
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void insertDevicesType(ActionForm form, SqlSessionManager conn,HttpSession session,List<MsgInfo> errors) throws Exception {

		DevicesTypeForm devicesTypeForm = (DevicesTypeForm) form;
		DeviceTypeEntity devicesTypeEntity = new DeviceTypeEntity();

		BeanUtil.copyToBean(devicesTypeForm, devicesTypeEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesTypeEntity.setUpdated_by(user.getOperator_id());
		
		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
		
		dao.insertDevicesType(devicesTypeEntity);
	}

	/**
	 * 删除设备工具品名
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void deleteDevicesType(ActionForm form, SqlSessionManager conn,HttpSession session,List<MsgInfo> errors) throws Exception {

		DevicesTypeForm devicesTypeForm = (DevicesTypeForm) form;
		DeviceTypeEntity devicesTypeEntity = new DeviceTypeEntity();

		BeanUtil.copyToBean(devicesTypeForm, devicesTypeEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesTypeEntity.setUpdated_by(user.getOperator_id());
		
		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
		dao.deleteDevicesType(devicesTypeEntity);
	}

	/**
	 * 更新设备工具品名
	 * 
	 * @param form
	 * @param conn
	 * @param errors
	 * @throws Exception
	 */
	public void updateDevicesType(ActionForm form, SqlSessionManager conn,HttpSession session,List<MsgInfo> errors) throws Exception {

		DevicesTypeForm devicesTypeForm = (DevicesTypeForm) form;
		DeviceTypeEntity devicesTypeEntity = new DeviceTypeEntity();

		BeanUtil.copyToBean(devicesTypeForm, devicesTypeEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		devicesTypeEntity.setUpdated_by(user.getOperator_id());
		
		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
		dao.updateDevicesType(devicesTypeEntity);
	}

	/**
	 * 使用设备工具品名下拉列表
	 * 
	 * @param conn
	 * @return
	 */
	public String getDevicesTypeReferChooser(SqlSession conn) {
		// 取得权限下拉框信息
		List<String[]> dList = new ArrayList<String[]>();

		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
		List<DeviceTypeEntity> list = dao.getAllDeviceName();

		// 建立页面返回表单
		List<DevicesTypeForm> ldf = new ArrayList<DevicesTypeForm>();
		if (list != null && list.size() > 0) {
			// 数据对象复制到表单
			BeanUtil.copyToFormList(list, ldf, null, DevicesTypeForm.class);
			for (DevicesTypeForm form : ldf) {
				String[] dline = new String[2];
				dline[0] = form.getDevices_type_id();
				dline[1] = form.getName();
				dList.add(dline);
			}
			String pReferChooser = CodeListUtils.getReferChooser(dList);
			return pReferChooser;
		} else {
			return "";
		}
	}

	/**
	 * 危险归类文字取得
	 */
	private Map<String, String> classificationText = new HashMap<String, String>(); // Not static
	private String getClassificationText(Integer classification) {
		String codeString = Integer.toBinaryString(classification);
		String ret = "";
		int length = codeString.length();
		for (int i = 0; i < length; i++) {
			if (codeString.charAt(i) == '1') {
				String cd = "" + (length - i - 1);
				if (!classificationText.containsKey(cd)) {
					classificationText.put(cd, CodeListUtils.getValue("device_hazardous_classification", cd) + " ");
				}
				ret = classificationText.get(cd) + ret;
			}
		}
		return ret;
	}

	/**
	 * 插入设备危险标示
	 * @param device_type_id
	 * @param hazardous_cautions
	 * @param conn
	 */
	public void insertHazardousCautions(String device_type_id,
			String hazardous_cautions, SqlSessionManager conn) {
		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);
	
		if (device_type_id == null) {
			if (hazardous_cautions != null) {
				CommonMapper cMapper = conn.getMapper(CommonMapper.class);
				device_type_id = cMapper.getLastInsertID();
			}
		} else {
			dao.removeHazardousCautionById(device_type_id);
		}

		if (hazardous_cautions != null) {
			String[] hazardousCautionArray = hazardous_cautions.split(",");
			DeviceTypeEntity devicesTypeEntity = new DeviceTypeEntity();
			devicesTypeEntity.setDevice_type_id(device_type_id);
			for (String hazardousCaution : hazardousCautionArray) {
				devicesTypeEntity.setClassification(Integer.parseInt(hazardousCaution));
				dao.insertHazardousCaution(devicesTypeEntity);
			}
		}
	}
}
