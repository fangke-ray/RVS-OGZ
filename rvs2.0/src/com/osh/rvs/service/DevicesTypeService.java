package com.osh.rvs.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.DeviceTypeEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.DevicesTypeForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.DevicesTypeMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;

public class DevicesTypeService {

	public static final int SPECIALIZED_FOR_DISINFECT_DEVICE = 3; //消毒设备
	public static final int SPECIALIZED_FOR_STERILIZE_DEVICE = 4;//灭菌设备

	private static Set<String> safetyGuideSet = null;

	/**
	 * 设备工具品名 查询
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
			if (getSafetyGuideSet().contains(entity.getDevice_type_id())) {
				devicesTypeForm.setSafety_guide("1");
			}
			devicesTypeForms.add(devicesTypeForm);
		}

		return devicesTypeForms;
	}

	/**
	 * 设备工具品名 详细
	 * 
	 * @param id
	 * @param conn
	 * @return
	 */
	public DeviceTypeEntity getDeviceTypeById(String id, SqlSession conn) {

		DevicesTypeMapper dao = conn.getMapper(DevicesTypeMapper.class);

		DeviceTypeEntity devicesTypeEntity = new DeviceTypeEntity();
		devicesTypeEntity.setDevice_type_id(id);
		List<DeviceTypeEntity> devicesTypeEntities = dao.searchDeviceType(devicesTypeEntity);
		if (devicesTypeEntities.size() > 0) {
			return devicesTypeEntities.get(0);
		}

		return null;
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

		// 清除工位待点检品判断
		CheckResultPageService.todayCheckedMap.clear();
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
	public String getClassificationText(Integer classification) {
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

	/**
	 * 设定安全操作守则
	 * @param manage_id
	 * @param photo_file_name
	 */
	public void copyPhoto(String type_id, String photo_file_name) {
		// 把图片拷贝到目标文件夹下
		String today = DateUtil.toString(new Date(), "yyyyMM");
		String tempFilePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + today + "\\" + photo_file_name;
		String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\safety_guide\\" + type_id;
		File confFile = new File(tempFilePath);
		if (confFile.exists()) {
			FileUtils.copyFile(tempFilePath, targetPath, true);
		}
		safetyGuideSet.add(type_id);
	}

	public void removePhoto(String type_id) {
		// 把目标文件夹下图片删除
		String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\safety_guide\\" + type_id;
		File confFile = new File(targetPath);
		if (confFile.exists()) {
			confFile.delete();
		}
		safetyGuideSet.remove(type_id);
	}

	public static Set<String> getSafetyGuideSet() {
		if (safetyGuideSet == null) {
			safetyGuideSet = new HashSet<String>();
			File path = new File(PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\safety_guide\\");
			if (path.exists()) {
				for (File file : path.listFiles()) {
					if (file.isFile()) safetyGuideSet.add(file.getName());
				}
			} else {
				path.mkdirs();
			}
		}
		return safetyGuideSet;
	}
}
